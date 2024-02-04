package com.example.onlinecinema;

import static android.app.Service.START_STICKY;

import static com.example.onlinecinema.AdsManager.createInter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private TextView textViewTimer;
    private ViewPager viewPager;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    private ImageView sliderMenuButton;
    private final long DELAY_MS = 3000; // Delay in milliseconds before switching to the next image
    private final long PERIOD_MS = 5000;
    FirebaseDatabase database;
    private Timer timer2;
    private int currentPage = 0;
    private static final String REMAINING_TIME_KEY = "remaining_time";
    private Handler handler;
    private TimerService.TimerBinder timerBinder;
    private boolean isServiceBound = false;
    private ViewPager2 viewPager2;
    private ImageSliderAdapter adapter;
    private List<ImageModel> images;

    DatabaseReference userref;

    private Runnable timerRunnable; String filmname;
    private long totalTimeMillis;
   private SharedPreferences sharedPreferences;
   Toolbar toolbar;
    DatabaseReference reference;
    AdsManager adsManager;
    FirebaseAuth auth;
    ArrayList<UserModel> models;
    String uid;
    CountDownTimer countDownTimer;
    // Create a ServiceConnection to interact with the TimerService



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerLayout = findViewById(R.id.nav_drawer);
        navigationView = findViewById(R.id.Nav_view);
       tabLayout = findViewById(R.id.tabLayout);
       createInter(this);
       adsManager = new AdsManager();
        viewPager = findViewById(R.id.viewPager);
        handler = new Handler();
        toolbar = findViewById(R.id.toolbar);
        navigationView.bringToFront();

        ActionBarDrawerToggle barDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(barDrawerToggle);
        barDrawerToggle.getDrawerArrowDrawable().setColor(Color.WHITE);
        barDrawerToggle.syncState();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);


        // Setup the toolbar as the action bar
        viewPager2 = findViewById(R.id.imageSlider);
        images = new ArrayList<>();
        // Find the TextView for the timer
        textViewTimer = findViewById(R.id.textViewTimer);

        // Register a BroadcastReceiver to receive timer updates from the TimerService
        LocalBroadcastManager.getInstance(this).registerReceiver(timerUpdateReceiver, new IntentFilter("TIMER_UPDATE"));
        // Set a listener for the navigation drawer item clicks (if needed)
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle the item click here (if needed)

                int id = item.getItemId();

                if (id == R.id.BTH) {
                    adsManager.showInter();
                    startActivity(new Intent(MainActivity.this, BehindtheScene.class));
                    Log.d("NavClick","Clicked About");
                } else if (id == R.id.songs) {
                    adsManager.showInter();
                   startActivity(new Intent(MainActivity.this,Songs.class));
                    Log.d("NavClick","Clicked About");
//                    Intent intent = new Intent(MainActivity.this, a.class);
//                    startActivity(intent);
//                    finish();

                } else if (id == R.id.share) {
                    String packagename = getPackageName();
                    Intent shareintent = new Intent();
                    shareintent.setAction(Intent.ACTION_SEND);
                    shareintent.putExtra(Intent.EXTRA_TEXT, "https://bit.ly/onlinefreefilms");
                    shareintent.setType("text/plain");
                    startActivity(Intent.createChooser(shareintent, "share Via"));
                    // when u rate your app

                } else if (id == R.id.contact) {
                    startActivity(new Intent(MainActivity.this,ContactUs.class));

                } else if (id == R.id.rateus){
                        openPlayStoreForRating();
                    // when u share your app



                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        startService(new Intent(this, TimerService.class));

        long remainingTimeMillis = SharedPreferencesHelper.getRemainingTime(this);

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("users");
        FirebaseUser currentuser = auth.getCurrentUser();
        if (currentuser!= null){
            uid = auth.getCurrentUser().getUid();
            models = new ArrayList<>();
            userref = FirebaseDatabase.getInstance().getReference("users");
            Query query = reference.orderByChild("uid").equalTo(uid);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        UserModel a = snapshot.getValue(UserModel.class);
                        models.add(a);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }




        // Hide the status bar and make the activity full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        MyPagerAdapter adapter = new MyPagerAdapter(getSupportFragmentManager());
        // Set the adapter on the ViewPager
        viewPager.setAdapter(adapter);
        // Connect the TabLayout with the ViewPager
        tabLayout.setupWithViewPager(viewPager);
    }

    private void openPlayStoreForRating() {
        String packageName = getPackageName(); // Get the package name of your app
        Uri uri = Uri.parse("market://details?id=" + packageName);
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

        // Try to open Play Store app, fallback to web browser if Play Store is not available
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            Uri webUri = Uri.parse("https://play.google.com/store/apps/details?id=" + packageName);
            Intent goToWebMarket = new Intent(Intent.ACTION_VIEW, webUri);
            startActivity(goToWebMarket);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, TimerService.class);
        bindService(intent, timerServiceConnection, Context.BIND_AUTO_CREATE);
        // Register the BroadcastReceiver to receive timer updates from the TimerService
        LocalBroadcastManager.getInstance(this).registerReceiver(timerUpdateReceiver, new IntentFilter("TIMER_UPDATE"));
        // Start the periodic timer updates

    }

    // FragmentPagerAdapter class
    private class MyPagerAdapter extends FragmentPagerAdapter {

        private final String[] tabTitles = {"Pashto\nFilms", "Punjabi\nFilms", "Urdu\nFilms","Film\nTrailers"};

        public MyPagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int position) {
            // Return the fragment at the specified position
            switch (position) {
                case 0:
                    return new PashtoFragment();
                case 1:
                    return new PunjabiFragment();
                case 2:
                    return new PashtoSongFrament();
                case 3:
                    return new TrailerFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Return the total number of tabs/fragments
            return tabTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Return the title for each tab
            return tabTitles[position];
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        images.clear();

        // Step 2: Create a list of ImageModel objects

        //   images.add(new ImageModel("https://healthynfity.com/wp-content/uploads/2023/07/health-and-fit-780x470.jpeg"));

        // Step 3: Create an instance of the adapter and set it to the ViewPager2


        // Step 4: Start the auto slideshow
        startAutoSlider();
        database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("images");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot snapshot1 : snapshot.getChildren()){
                        ImageModel a= snapshot1.getValue(ImageModel.class);
                        images.add(a);
                    }
                }
                adapter = new ImageSliderAdapter(images,MainActivity.this);
                viewPager2.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Bind to the TimerService
        // Register the BroadcastReceiver to receive updates from TimerService
    //    LocalBroadcastManager.getInstance(this).registerReceiver(timerUpdateReceiver, new IntentFilter("TIMER_UPDATE"));
        // Bind to the TimerService
        // Bind to the TimerService
        // Bind to the TimerService
        Intent intent = new Intent(this, TimerService.class);
        bindService(intent, timerServiceConnection, Context.BIND_AUTO_CREATE);
        // Register the BroadcastReceiver to receive timer updates from the TimerService
        LocalBroadcastManager.getInstance(this).registerReceiver(timerUpdateReceiver, new IntentFilter("TIMER_UPDATE"));
        // Start the periodic timer updates

        // Start the periodic timer updates
        //timerHandler.postDelayed(timerUpdateRunnable, 0);
    }




    @Override
    protected void onPause() {
        super.onPause();

        // Unregister the BroadcastReceiver to avoid leaks
        LocalBroadcastManager.getInstance(this).unregisterReceiver(timerUpdateReceiver);

        if (isServiceBound) {
            unbindService(timerServiceConnection);
            isServiceBound = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Unbind from the TimerService
        if (isServiceBound) {
            unbindService(timerServiceConnection);
            isServiceBound = false;
        }

    }

    @Override
    public void onBackPressed() {
        // Close the navigation drawer if it's open and the back button is pressed
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void startAutoSlider() {
        final Handler handler = new Handler();

        final Runnable runnable = new Runnable() {
            public void run() {
                if (currentPage == images.size()) {
                    currentPage = 0;
                }
                viewPager2.setCurrentItem(currentPage++, true);
            }
        };

        timer2 = new Timer();
        timer2.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(runnable);
            }
        }, DELAY_MS, PERIOD_MS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unregister the BroadcastReceiver to avoid leaks
        LocalBroadcastManager.getInstance(this).unregisterReceiver(timerUpdateReceiver);

        if (isServiceBound) {
            unbindService(timerServiceConnection);
            isServiceBound = false;
        }
    }
    // Create a BroadcastReceiver to update the timer on the MainActivity
    private BroadcastReceiver timerUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get the updated remaining time from the broadcast
            String formattedTime = intent.getStringExtra("remaining_time");
            // Update the TextView with the new remaining time
            textViewTimer.setVisibility(View.VISIBLE);
            textViewTimer.setText(formattedTime);
        }
    };

    private ServiceConnection timerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            // Cast the IBinder to TimerBinder and get the TimerService instance
            timerBinder = (TimerService.TimerBinder) iBinder;
            isServiceBound = true;

            // Get the remaining time from the TimerService and update the TextView
            long remainingTimeMillis = timerBinder.getRemainingTimeMillis();
            updateTimerTextView(remainingTimeMillis);

            // Start the CountDownTimer if there's remaining time
            if (remainingTimeMillis > 0) {
                startCountDownTimer(remainingTimeMillis);
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isServiceBound = false;
        }
    };
    // Method to update the timer TextView with the remaining time
    private void updateTimerTextView(long remainingTimeMillis) {
        int remainingSeconds = (int) (remainingTimeMillis / 1000);
        int remainingHours = remainingSeconds / 3600;
        int remainingMinutes = (remainingSeconds % 3600) / 60;
        remainingSeconds = remainingSeconds % 60;
        String formattedTime;
        if (remainingHours <= 0 && remainingMinutes <= 0 && remainingSeconds <= 0) {
            // Timer has finished or been stopped
            formattedTime = "00:00:00";
            textViewTimer.setVisibility(View.INVISIBLE); // Hide the text view
        } else {
            // Timer is still running
            formattedTime = String.format("%02d:%02d:%02d", remainingHours, remainingMinutes, remainingSeconds);
            textViewTimer.setVisibility(View.VISIBLE); // Show the text view
        }
        textViewTimer.setText(formattedTime);
    }
    // Runnable to update the timer view periodically

    private void startCountDownTimer(long remainingTimeMillis) {
        countDownTimer = new CountDownTimer(remainingTimeMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                updateTimerTextView(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                // CountDownTimer finished, perform any required action
                // (if needed)
            }
        };
        countDownTimer.start();
    }

}

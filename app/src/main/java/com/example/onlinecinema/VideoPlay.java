package com.example.onlinecinema;

import static com.example.onlinecinema.AdsManager.createInter;
import static com.example.onlinecinema.R.drawable.baseline_pause_24;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.LoadControl;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class VideoPlay extends AppCompatActivity {
    private SimpleExoPlayer player;
    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<Messages> messageList = new ArrayList<>();
    private DatabaseReference messagesRef;
    private EditText messageEditText;
    ImageButton Strech;

    AdsManager adsManager;
    private String currentUserType = "user"; // or "admin"
    String Name,city,mobile;
    DatabaseReference reference;
    TextView videoText; String Mtype;
    SharedPreferences LoginPref;
    private StyledPlayerView playerView;

    String video,phone;

    private boolean playWhenReady = true;
    private long playbackPosition = 0;
    private int currentWindow = 0;
    private boolean isPlayerInitialized = false;
    boolean fullscreen = false;
    ImageView fullscreenButton,videosetting,playpause;
    ImageButton play;
    String demo; Uri videouri;
    private MaxInterstitialAd interstitialAd;
    private int retryAttempt;
    private long lastAdDisplayTime = 0;
    Handler handler = new Handler();
    void createInterstitialAd() {
        interstitialAd = new MaxInterstitialAd( getString(R.string.Inter), this );
        interstitialAd.setListener(new MaxAdListener() {
            // MAX Ad Listener
            @Override
            public void onAdLoaded(final MaxAd maxAd)
            {
                // Interstitial ad is ready to be shown. interstitialAd.isReady() will now return 'true'

                // Reset retry attempt
                retryAttempt = 0;
            }

            @Override
            public void onAdLoadFailed(final String adUnitId, final MaxError error)
            {
                // Interstitial ad failed to load
                // AppLovin recommends that you retry with exponentially higher delays up to a maximum delay (in this case 64 seconds)

                retryAttempt++;
                long delayMillis = TimeUnit.SECONDS.toMillis( (long) Math.pow( 2, Math.min( 6, retryAttempt ) ) );

                new Handler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        interstitialAd.loadAd();
                        handler.removeCallbacksAndMessages(null);
                        adtimer();
                    }
                }, delayMillis );
            }

            @Override
            public void onAdDisplayFailed(final MaxAd maxAd, final MaxError error)
            {
                // Interstitial ad failed to display. AppLovin recommends that you load the next ad.
                interstitialAd.loadAd();
                handler.removeCallbacksAndMessages(null);
                adtimer();
            }

            @Override
            public void onAdDisplayed(final MaxAd maxAd) {
                lastAdDisplayTime = System.currentTimeMillis();
               // adtimer();
            }

            @Override
            public void onAdClicked(final MaxAd maxAd) {}

            @Override
            public void onAdHidden(final MaxAd maxAd)
            {
                MediaItem mediaItem = MediaItem.fromUri(Uri.parse(video));
                playerView.setPlayer(player);
                player.setMediaItem(mediaItem);
                // Save the current playback position
                SharedPreferences preferences = getSharedPreferences("VideoPlayerPrefs", MODE_PRIVATE);
                playWhenReady = preferences.getBoolean("playWhenReady", true);
                playbackPosition = preferences.getLong("playbackPosition", 0);
                currentWindow = preferences.getInt("currentWindow", 0);


                player.prepare();
                player.setPlayWhenReady(playWhenReady);
                player.seekTo(currentWindow, playbackPosition);
                adtimer();
                // Interstitial ad is hidden. Pre-load the next ad
                interstitialAd.loadAd();
            }
        });

        // Load the first ad
        interstitialAd.loadAd();
    }
    void showInters(){
        if ( interstitialAd.isReady() )
        {
            interstitialAd.showAd();
        }
    }

    void adtimer(){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                    showInters();
                    savePlayerState();
            }
        },318000);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the activity to fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        adsManager = new AdsManager();
        createInter(VideoPlay.this);

        // Set the activity to landscape orientation
      //  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_video_play);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        LoginPref = getSharedPreferences("LoginPref",MODE_PRIVATE);
        video = getIntent().getStringExtra("filmurl");
        Mtype = getIntent().getStringExtra("Movietype");
        ModelFilm model = new ModelFilm();
        videouri = Uri.parse(video);
      //  Toast.makeText(this, video + "", Toast.LENGTH_SHORT).show();
        videoText = findViewById(R.id.videoText);
        phone = getIntent().getStringExtra("number");

        String filmna = getIntent().getStringExtra("filmname");
        videoText.setText(new StringBuilder().append(phone).append("\nOnline Cinema").toString());
        playerView = findViewById(R.id.playerView);
        fullscreenButton = playerView.findViewById(R.id.exo_fullscreen_icon);
        videosetting = playerView.findViewById(R.id.videosetting);
        playpause = playerView.findViewById(R.id.exo_play_pause);
        Strech = playerView.findViewById(R.id.btn_settin);
         long lastPlaybackPosition = C.TIME_UNSET;
        createInterstitialAd();
        adtimer();
        videosetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePlayerState();

                // Get the layout inflater
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_resolution, null);
                AlertDialog.Builder builder2 = new AlertDialog.Builder(VideoPlay.this);
                // Set up radio buttons

                final RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroup);
                final RadioButton radioButton520p = dialogView.findViewById(R.id.radioButton520p);
                final RadioButton radioButton720p = dialogView.findViewById(R.id.radioButton720p);
                final RadioButton radioButton1080p = dialogView.findViewById(R.id.radioButton1080p);
                final RadioButton radioButton360p = dialogView.findViewById(R.id.radioButton360p);
                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        if (checkedId == R.id.radioButton520p) {
                            video = getIntent().getStringExtra("560p");
                           // tosa = "520";
                          //  Toast.makeText(VideoPlay.this, model.getFilmurl780p() + video, Toast.LENGTH_SHORT).show();
                            //  tosa = "780";
                        } else if (checkedId == R.id.radioButton720p) {
                            video = getIntent().getStringExtra("780p");
                          //  Toast.makeText(VideoPlay.this, model.getFilmurl780p()+ video, Toast.LENGTH_SHORT).show();
                          //  tosa = "780";
                        } else if (checkedId == R.id.radioButton1080p) {
                            video = getIntent().getStringExtra("1080p");
                           // tosa = "1080";
                         //   Toast.makeText(VideoPlay.this, model.getFilmurl780p()+ video, Toast.LENGTH_SHORT).show();
                            //  tosa = "780";
                        } else if (checkedId== R.id.radioButton360p) {
                            video = getIntent().getStringExtra("360p");
                         //   tosa = "1080";
                          //  Toast.makeText(VideoPlay.this, model.getFilmurl780p()+ video, Toast.LENGTH_SHORT).show();
                            //  tosa = "780";
                        } else {
                            video = getIntent().getStringExtra("filmurl");
                           // tosa = "defaukt";
                           // Toast.makeText(VideoPlay.this, model.getFilmurl780p() + video, Toast.LENGTH_SHORT).show();
                            //  tosa = "780";
                        }

                    }

                });

                builder2.setView(dialogView)
                        .setTitle("Select Resolution")
                        .setPositiveButton("Play", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {


                                MediaItem mediaItem = MediaItem.fromUri(Uri.parse(video));
                                playerView.setPlayer(player);
                                player.setMediaItem(mediaItem);
                                // Save the current playback position
                                SharedPreferences preferences = getSharedPreferences("VideoPlayerPrefs", MODE_PRIVATE);
                                playWhenReady = preferences.getBoolean("playWhenReady", true);
                                playbackPosition = preferences.getLong("playbackPosition", 0);
                                currentWindow = preferences.getInt("currentWindow", 0);



                                // Start or resume playback
                                dialogInterface.dismiss();
                                player.prepare();
                                player.setPlayWhenReady(playWhenReady);
                                player.seekTo(currentWindow, playbackPosition);

                            }
                        });



                // Create the dialog after setting up the builder
                AlertDialog dialog = builder2.create();

                // Show the dialog
                dialog.show();
                player.pause();
                playpause.setImageDrawable(ContextCompat.getDrawable(VideoPlay.this,R.drawable.baseline_pause_24));
            }
        });

        Strech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentMode = playerView.getResizeMode();

                switch (currentMode) {
                    case AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH:
                        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                        Toast.makeText(VideoPlay.this, currentMode+"fit", Toast.LENGTH_SHORT).show();
                        break;

                    case AspectRatioFrameLayout.RESIZE_MODE_FIT:
                        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
                        Toast.makeText(VideoPlay.this, currentMode+"zoom", Toast.LENGTH_SHORT).show();
                        break;

                    case AspectRatioFrameLayout.RESIZE_MODE_ZOOM:
                        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                        Toast.makeText(VideoPlay.this, currentMode+"fill", Toast.LENGTH_SHORT).show();
                        break;

                    case AspectRatioFrameLayout.RESIZE_MODE_FILL:
                        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT);
                        Toast.makeText(VideoPlay.this, currentMode+"height", Toast.LENGTH_SHORT).show();
                        break;

                    case AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT:
                        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH);
                        Toast.makeText(VideoPlay.this, currentMode+"fixwid", Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
                        break;
                }

            }

                //Toast.makeText(VideoPlay.this, playerView.getResizeMode()+"", Toast.LENGTH_SHORT).show();

        });
        playpause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (player.getPlayWhenReady()){
                    playpause.setImageDrawable(ContextCompat.getDrawable(VideoPlay.this,R.drawable.baseline_play_arrow_24));
                    player.setPlayWhenReady(false);
                } else {
                    player.setPlayWhenReady(true);
                    playpause.setImageDrawable(ContextCompat.getDrawable(VideoPlay.this, baseline_pause_24));
                }



            }
        });
        LoadControl loadControl = new DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                        10_000, // Minimum buffer duration (in milliseconds)
                        60_000, // Maximum buffer duration (in milliseconds)
                        2_000,  // Buffer duration for playback (in milliseconds)
                        2_000)  // Buffer duration after rebuffer (in milliseconds)
                .build();
        player =  new SimpleExoPlayer.Builder(this)
                .setLoadControl(loadControl)
                .build();;
        // Per MediaItem settings.
        MediaItem mediaItem = MediaItem.fromUri(videouri);
        playerView.setPlayer(player);
        player.setMediaItem(mediaItem);

        // Restore the player state from SharedPreferences if available
        SharedPreferences preferences = getSharedPreferences("VideoPlayerPrefs", MODE_PRIVATE);
        playWhenReady = preferences.getBoolean("playWhenReady", true);
        playbackPosition = preferences.getLong("playbackPosition", 0);
        currentWindow = preferences.getInt("currentWindow", 0);

        player.prepare();
        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
        playerView.setPlayer(player);
        isPlayerInitialized = true;
        showText();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
       messagesRef = database.getReference(filmna+"comments");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        reference  = FirebaseDatabase.getInstance().getReference("user");
        String login = LoginPref.getString("login","");
        if (login.equals("ok")){

            loaduserdata();
            // Load messages from Firebase Realtime Database
            loadMessages();
        }


        recyclerView = findViewById(R.id.recycler_viewvideo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        adapter = new ChatAdapter(messageList);
        recyclerView.setAdapter(adapter);
        // Find the views
        messageEditText = findViewById(R.id.message_edit_text);
//

       findViewById(R.id.send_button).setOnClickListener(view -> onSendButtonClick());
        fullscreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fullscreen) {
                    fullscreenButton.setImageDrawable(ContextCompat.getDrawable(VideoPlay.this, R.drawable.ic_fullscreen_open));
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    if(getSupportActionBar() != null){
                        getSupportActionBar().show();
                    }
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) playerView.getLayoutParams();
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    params.height = (int) ( 350 * getApplicationContext().getResources().getDisplayMetrics().density);
                    playerView.setLayoutParams(params);
                    fullscreen = false;
                    recyclerView.setVisibility(View.VISIBLE);
                    messageEditText.setVisibility(View.VISIBLE);
                    findViewById(R.id.send_button).setVisibility(View.VISIBLE);

                }else{
                    fullscreenButton.setImageDrawable(ContextCompat.getDrawable(VideoPlay.this, R.drawable.ic_fullscreen_exit));
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                            |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                    if(getSupportActionBar() != null){
                        getSupportActionBar().hide();
                    }
                    playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) playerView.getLayoutParams();
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    playerView.setLayoutParams(params);
                    fullscreen = true;
                    recyclerView.setVisibility(View.GONE);
                    messageEditText.setVisibility(View.GONE);
                    findViewById(R.id.send_button).setVisibility(View.GONE);

                }
            }
        });

    }
    // Method to apply the selected playback quality to the ExoPlayer

    private void loaduserdata() {
        reference.child(phone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Name = snapshot.child("name").getValue(String.class);
                    mobile = snapshot.child("phone").getValue(String.class);
                    city = snapshot.child("city").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });
    }
    private void loadMessages() {
        messageList.clear();
        messagesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {
                Messages message = dataSnapshot.getValue(Messages.class);
                if (message != null) {
                    messageList.add(message);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {
                // Handle message changes if needed
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // Handle message removal if needed
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {
                // Handle message movement if needed
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }
    private void sendMessage(String text, String userId, String userName, String userType, String city, String mobile) {
        long timestamp = System.currentTimeMillis();


        String push = messagesRef.push().getKey();
        Calendar calendar = Calendar.getInstance();
        Date currentDateAndTime = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedDate = dateFormat.format(currentDateAndTime);
        Messages message = new Messages(text, timestamp, userId, userName, city, mobile, userType,push,formattedDate);
        messagesRef.child(push).setValue(message);
    }
//
    private void onSendButtonClick() {
        String messageText = messageEditText.getText().toString().trim();
        if (!messageText.isEmpty()) {
            String userId = phone;
            String userName = Name;
            String userCity = city; // Implement this function to get the user's city
            String userMobile = mobile; // Implement this function to get the user's mobile number
            sendMessage(messageText, userId, userName,currentUserType, userCity, userMobile);
            messageEditText.setText(""); // Clear the input field

        }
//
 }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (player.isPlaying()){
            //   show();
            player.stop();
            handler.removeCallbacksAndMessages(null);
        }
        if (Mtype.equals("free")){
            adsManager.showInter();
            handler.removeCallbacksAndMessages(null);
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        if (!isPlayerInitialized) {
            playerView.setPlayer(player);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24) {
            savePlayerState();
            releasePlayer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Util.SDK_INT < 24) {
            savePlayerState();
            releasePlayer();
        }
    }

    private void savePlayerState() {
        if (player != null) {
            playWhenReady = player.getPlayWhenReady();
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();

            SharedPreferences preferences = getSharedPreferences("VideoPlayerPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("playWhenReady", playWhenReady);
            editor.putLong("playbackPosition", playbackPosition);
            editor.putInt("currentWindow", currentWindow);
            editor.apply();
        }
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        messageList.clear();
        if (!isPlayerInitialized) {
            playerView.setPlayer(player);
        }
        if (player == null){
            LoadControl loadControl = new DefaultLoadControl.Builder()
                    .setBufferDurationsMs(
                            10_000, // Minimum buffer duration (in milliseconds)
                            60_000, // Maximum buffer duration (in milliseconds)
                            2_000,  // Buffer duration for playback (in milliseconds)
                            2_000)  // Buffer duration after rebuffer (in milliseconds)
                    .build();
            player =  new SimpleExoPlayer.Builder(this)
                    .setLoadControl(loadControl)
                    .build();
            player.prepare();
            player.setPlayWhenReady(playWhenReady);
            player.seekTo(currentWindow, playbackPosition);
            playerView.setPlayer(player);

        }
//        messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
//                if (snapshot.exists()){
//                    for (DataSnapshot snapshot1 : snapshot.getChildren()){
//                        Messages message = snapshot1.getValue(Messages.class);
//                        messageList.add(0,message);
//                        adapter.notifyDataSetChanged();
//                    }
//                }
//                adapter = new ChatAdapter(messageList);
//                recyclerView.setAdapter(adapter);
//
//            }
//
//            @Override
//            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
//
//            }
//        });
    }

    void showText()
    {
        final DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Random R = new Random();
                        final float dx = R.nextFloat() * displaymetrics.widthPixels;
                        final float dy = R.nextFloat() * displaymetrics.heightPixels;
                        final Timer timer = new Timer();
                        videoText.animate()
                                .x(dx)
                                .y(dy)
                                .setDuration(0)
                                .start();
                    }
                });
            }
        },1000,10000);
    }
//    private void startTimer() {
//        handler = new Handler();
//        showText();
//        runnable = new Runnable() {
//            @Override
//            public void run() {
//
//                String millis = UserDefaults.standard().getString(Utils.movieId(default_model.getVideoId()), "");
//                if (!millis.isEmpty()) {
//                    Calendar calendar = Calendar.getInstance();
//                    calendar.setTimeInMillis(Long.parseLong(millis));
//                    Calendar now = Calendar.getInstance();
//                    if (now.getTimeInMillis() > calendar.getTimeInMillis()) {
//                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//
//                        destroyEverything();
//                        finish();
//                    }
//                }
//
//                try {
//                    handler.postDelayed(this, 5000);
//                } catch (Exception ignored) { }
//            }
//        };
//        handler.post(runnable);
//    }
//    private void showSystemUI() {
//        View decorView = getWindow().getDecorView();
//        decorView.setSystemUiVisibility(0);
//    }
//
//    private void hideSystemUI() {
//        View decorView = getWindow().getDecorView();
//        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
//    }
//
//    private int dpToPx(int dp) {
//        return (int) (dp * getResources().getDisplayMetrics().density);
//    }
//    private void initializePlayer() {
//
//        player = new ExoPlayer.Builder(this).build();
//        playerView.setPlayer(player);
//
//        player.setPlayWhenReady(playWhenReady);
//        player.seekTo(currentWindow, playbackPosition);
//
//        Uri uri = Uri.parse(video);
//        MediaItem mediaItem = MediaItem.fromUri(uri);
//        player.setMediaItem(mediaItem);
//        player.prepare();
//    }
//    private void releasePlayer() {
//        if (player != null) {
//            playbackPosition = player.getCurrentPosition();
//            currentWindow = player.getCurrentWindowIndex();
//            playWhenReady = player.getPlayWhenReady();
//            player.release();
//            player = null;
//        }
//    }
//    @Override
//    protected void onStart() {
//        super.onStart();
//        if (Util.SDK_INT > 23 && player != null) {
//            initializePlayer();
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if ((Util.SDK_INT <= 23 || player == null)) {
//            initializePlayer();
//        }
//        if (Util.SDK_INT > 23 && player != null) {
//            player.setPlayWhenReady(true);
//            player.getPlaybackState();
//        }
//    }



//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (Util.SDK_INT <= 23) {
//            releasePlayer();
//        }
//    }
//
////    @Override
////    protected void onStop() {
////        super.onStop();
////        if (Util.SDK_INT > 23) {
////            releasePlayer();
////        }
////    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (player != null) {
//            player.release();
//            player = null;
//        }
//    }
}
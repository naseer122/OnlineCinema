package com.example.onlinecinema;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

public class DetailActivity extends AppCompatActivity {
ImageView DetailIV;
TextView DetailTv,timer;
String url;
Button MoviePay;
ImageView thumnail;
SharedPreferences sharedPreferences;

    private Handler handler;
    private Runnable timerRunnable; String filmname;
    private long totalTimeMillis;

    private static final String REMAINING_TIME_KEY = "remaining_time";

    String formattedTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        DetailIV = findViewById(R.id.DetailThumbnail);
        DetailTv = findViewById(R.id.DetailMovieName);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(DetailActivity.this);

        handler = new Handler();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        start();
        url = getIntent().getStringExtra("filmurl");
        Toast.makeText(this, url + "", Toast.LENGTH_SHORT).show();
        long remainingTimeMillis = sharedPreferences.getLong(REMAINING_TIME_KEY, 0);
        String name = getIntent().getStringExtra("filmname");
        int hours = (int) (remainingTimeMillis / (1000 * 60 * 60));
        int minutes = (int) ((remainingTimeMillis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) ((remainingTimeMillis % (1000 * 60)) / 1000);
        String imas = getIntent().getStringExtra("filmimg");
        formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        thumnail = findViewById(R.id.DetailThumbnail);
        Glide.with(DetailActivity.this).load(imas).into(thumnail);
        DetailTv.setText(name);

        MoviePay = findViewById(R.id.watchmovie);
        MoviePay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DetailActivity.this,VideoPlay.class).putExtra("filmurl",url));
               // Toast.makeText(DetailActivity.this, url + "", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void start(){      // Retrieve the remaining time from SharedPreferences
        long remainingTimeMillis = sharedPreferences.getLong(REMAINING_TIME_KEY, 0);
        if (remainingTimeMillis == 0){
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(REMAINING_TIME_KEY);
            editor.remove("filmname");
            editor.apply();
        }
        if (remainingTimeMillis > 0) {
            totalTimeMillis = remainingTimeMillis;
            // Store the initial remaining time in SharedPreferences
            startTimer();
            filmname = sharedPreferences.getString("filmname", "");
        } else {
            // Parse the time string from Firebase
            String firebaseTime = sharedPreferences.getString("duration", "");;
            String[] timeParts = firebaseTime.split(":");
            int hours = Integer.parseInt(timeParts[0]);
            int minutes = Integer.parseInt(timeParts[1]);
            int seconds = Integer.parseInt(timeParts[2]);

            // Calculate the total time in milliseconds
            totalTimeMillis = (hours * 60 * 60 * 1000) + (minutes * 60 * 1000) + (seconds * 1000);
            filmname = sharedPreferences.getString("filmname", "");
            startTimer();
        }
    }
    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (totalTimeMillis <= 0) {
                    // Lock the movie or perform any other action
                    // ...
                    // Clear the remaining time from SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove(REMAINING_TIME_KEY);
                    editor.apply();
                    // Stop the timer
                    handler.removeCallbacks(timerRunnable);
                } else {
                    // Update the UI or perform any other actions
                    // ...

                    // Format the remaining time
                    int remainingHours = (int) (totalTimeMillis / (1000 * 60 * 60));
                    int remainingMinutes = (int) ((totalTimeMillis % (1000 * 60 * 60)) / (1000 * 60));
                    int remainingSeconds = (int) ((totalTimeMillis % (1000 * 60)) / 1000);
                    String formattedTime = String.format("%02d:%02d:%02d", remainingHours, remainingMinutes, remainingSeconds);
                    timer.setText(formattedTime);
                    // Update the timer TextView

// Store the initial remaining time in SharedPreferences

                    // Update the timer by subtracting 1 second
                    totalTimeMillis -= 1000;
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putLong(REMAINING_TIME_KEY, totalTimeMillis);
                    editor.putString("filmname",filmname);

                    editor.apply();


                    // Schedule the next update after 1 second
                    handler.postDelayed(this, 1000);
                }
            }
        };

        // Start the timer immediately
        handler.postDelayed(timerRunnable, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        start();
    }
}
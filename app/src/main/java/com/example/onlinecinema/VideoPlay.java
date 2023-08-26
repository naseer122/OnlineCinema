package com.example.onlinecinema;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.LoadControl;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.util.Util;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class VideoPlay extends AppCompatActivity {
    private SimpleExoPlayer player;

    TextView videoText;
    private StyledPlayerView playerView;

    String video,phone;
    TextView filmname;
    private boolean playWhenReady = true;
    private long playbackPosition = 0;
    private int currentWindow = 0;
    private boolean isPlayerInitialized = false;

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

        // Set the activity to landscape orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_video_play);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        video = getIntent().getStringExtra("filmurl");
        Uri videouri = Uri.parse(video);
      //  Toast.makeText(this, video + "", Toast.LENGTH_SHORT).show();
        videoText = findViewById(R.id.videoText);
        phone = getIntent().getStringExtra("number");

        String filmna = getIntent().getStringExtra("filmname");
        videoText.setText(phone);
        playerView = findViewById(R.id.playerView);
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


    }
    // Method to apply the selected playback quality to the ExoPlayer


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (player.isPlaying()){
            //   show();
            player.stop();
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
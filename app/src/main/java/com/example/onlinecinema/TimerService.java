package com.example.onlinecinema;
import static com.google.android.exoplayer2.util.NotificationUtil.createNotificationChannel;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.onlinecinema.SharedPreferencesHelper;

public class TimerService extends Service {

    // Add the CountDownTimer instance
    private CountDownTimer countDownTimer;
    private long totalTimeMillis;
    private Runnable timerRunnable;
    private static final String CHANNEL_ID = "TimerServiceChannel";
    private static final int FOREGROUND_SERVICE_ID = 101;
    private SharedPreferences sharedPreferences;
    public static boolean isTimerRunning = false;
    private static final String REMAINING_TIME_KEY = "remaining_time";
    private static final String FILM_NAME_KEY = "film_name";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Retrieve the remaining time from SharedPreferences
        long remainingTimeMillis = SharedPreferencesHelper.getRemainingTime(this);
        if (sharedPreferences.contains(REMAINING_TIME_KEY)) {
            Notification notification = buildForegroundNotification();
            startForeground(FOREGROUND_SERVICE_ID, notification);
// Show the toast message with the remaining time
            // Set the totalTimeMillis to the correct remaining time
            totalTimeMillis = remainingTimeMillis;
            showRemainingTimeToast();
            startTimer();
        } else {
            // Shared preferences are empty, stop the service and timer
            stopTimer();
            stopSelf();
        }
        // Register a BroadcastReceiver to stop the timer
        BroadcastReceiver stopTimerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Stop the timer here
                stopTimer();
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(stopTimerReceiver, new IntentFilter("STOP_TIMER_ACTION"));

        return START_STICKY;
    }
    private Notification buildForegroundNotification() {
        // Create a notification for the foreground service
        // You can customize the notification content as needed
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Timer Running")
                .setContentText("Timer is running in the background.")
                .setSmallIcon(R.drawable.logo)
                .setPriority(NotificationCompat.PRIORITY_LOW); // Set the priority as per your requirement

        return builder.build();
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        // Save the remaining time to SharedPreferences when the task is removed
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(REMAINING_TIME_KEY, totalTimeMillis);
        editor.apply();
    }
    // Inner class for the Binder
    public class TimerBinder extends Binder {
        public long getRemainingTimeMillis() {
            return totalTimeMillis;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(totalTimeMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Update the remaining time in SharedPreferences
                if (shouldStopTimer()) {
                    cancel(); // Cancel the timer
                    return;
                }
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putLong(REMAINING_TIME_KEY, millisUntilFinished);
                editor.apply();

                // Calculate the remaining time in seconds
                int remainingSeconds = (int) (millisUntilFinished / 1000);

                // Format the remaining time
                int remainingHours = remainingSeconds / 3600;
                int remainingMinutes = (remainingSeconds % 3600) / 60;
                remainingSeconds = remainingSeconds % 60;
                String formattedTime = String.format("%02d:%02d:%02d", remainingHours, remainingMinutes, remainingSeconds);

                // Broadcast the updated remaining time to MainActivity
                Intent intent = new Intent("TIMER_UPDATE");
                intent.putExtra("remaining_time", formattedTime);
                LocalBroadcastManager.getInstance(TimerService.this).sendBroadcast(intent);
            }

            @Override
            public void onFinish() {
                // Timer completed, stop the service
                // Lock the movie or perform any other action
                // ...
                // Clear the remaining time from SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove(REMAINING_TIME_KEY);
                editor.remove(FILM_NAME_KEY);
                editor.apply();
                // Stop the timer
                stopTimer();
                // Stop the service
                stopSelf();
            }
        };
        // Start the CountDownTimer
        countDownTimer.start();
    }


        @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        totalTimeMillis = SharedPreferencesHelper.getRemainingTime(this); // Retrieve the remaining time from SharedPreferences
        if (totalTimeMillis > 0) {
            startTimer();
            Notification notification = buildForegroundNotification();
            startForeground(FOREGROUND_SERVICE_ID, notification);
        }
        createNotificationChannel();

        }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Timer Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

    }
    private void showRemainingTimeToast() {
        long remainingTimeMillis = SharedPreferencesHelper.getRemainingTime(this);
        if (remainingTimeMillis > 0) {
            int remainingHours = (int) (remainingTimeMillis / (1000 * 60 * 60));
            int remainingMinutes = (int) ((remainingTimeMillis % (1000 * 60 * 60)) / (1000 * 60));
            int remainingSeconds = (int) ((remainingTimeMillis % (1000 * 60)) / 1000);
            String formattedTime = String.format("%02d:%02d:%02d", remainingHours, remainingMinutes, remainingSeconds);

            String message = "Remaining Time: " + formattedTime;
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            Log.d("TimerService", message);
        }
    }

    public void stopTimer() {
        isTimerRunning = false; // Set the flag to stop the timer
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
    private boolean shouldStopTimer() {
        return !sharedPreferences.contains(REMAINING_TIME_KEY);
    }
}

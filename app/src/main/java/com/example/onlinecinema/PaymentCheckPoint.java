package com.example.onlinecinema;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PaymentCheckPoint extends AppCompatActivity implements PurchasesUpdatedListener {
LinearLayout Jazzcash, EasyPaisaLL,Google;
int Fprice;

    private SharedPreferences sharedPreferences;
    String number;
    FirebaseAuth auth;
    private static final String REMAINING_TIME_KEY = "remaining_time";

    private TextView timerTextView;

    private Handler handler;
    private Runnable timerRunnable;
    private long totalTimeMillis;

    String price;
    String duration;
    String filmurl;
    String filmname ;

    private BillingClient billingClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_check_point);
        EasyPaisaLL = findViewById(R.id.easypaisaLL);
        Google = findViewById(R.id.googleLL);
        price = getIntent().getStringExtra("price");
        duration = getIntent().getStringExtra("extraduration");
        filmurl = getIntent().getStringExtra("filmurl");
        filmname = getIntent().getStringExtra("filmname");
        auth = FirebaseAuth.getInstance();
        number = getIntent().getStringExtra("number");
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Initialize the BillingClient
        // Start the timer immediately
        handler = new Handler();
        billingClient = BillingClient.newBuilder(this)
                .setListener(this)
                .enablePendingPurchases()
                .build();

        EasyPaisaLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PaymentCheckPoint.this,EasyPaisa.class).putExtra("price",price).
                        putExtra("extraduration",duration).putExtra("filmurl",filmurl)
                        .putExtra("filmname",filmname));
                finish();
            }
        });
        Google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               startConnection(new BillingClientStateListener() {
                    @Override
                    public void onBillingSetupFinished(BillingResult billingResult) {
                        // Billing client is ready. You can query purchases here or start the purchase flow.
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            // Billing client is ready, you can initiate the purchase flow
                          purchaseMovieTicket();
                        } else {
                            // Handle setup failure
                        }
                    }

                    @Override
                    public void onBillingServiceDisconnected() {
                        // Handle billing service disconnection, you might want to try reconnecting here.
                    }
                });


            }
        });
    }
    public void startConnection(BillingClientStateListener listener) {
        billingClient.startConnection(listener);
    }

    public void endConnection() {
        billingClient.endConnection();
    }
    public void purchaseMovieTicket() {
        String productId = "moviepackage"; // Replace with your product ID from Google Play Console

        // Query the SkuDetails for the given productId
        SkuDetailsParams params = SkuDetailsParams.newBuilder()
                .setSkusList(Arrays.asList(productId))
                .setType(BillingClient.SkuType.INAPP)
                .build();

        billingClient.querySkuDetailsAsync(params, new SkuDetailsResponseListener() {
            @Override
            public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsList) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                    // Launch the purchase flow
                    // Get the SkuDetails for the product
                    SkuDetails skuDetails = skuDetailsList.get(0);

                    // Extract the price from SkuDetails and store it in a variable
                    int priceInMicros = (int) skuDetails.getPriceAmountMicros();
                    Fprice = priceInMicros / 1000000; // Convert from micros to regular currency


                    BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(skuDetailsList.get(0))
                            .build();

                    BillingResult result = billingClient.launchBillingFlow(PaymentCheckPoint.this, flowParams);
                    if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                        Log.e(TAG, "Failed to initiate purchase: " + result.getResponseCode());
                        // Handle error
                    }
                } else {
                    // Handle error in getting SkuDetails
                }
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
       endConnection();
    }

    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d(TAG, "User canceled the purchase");
            // Handle canceled purchase
        } else {
            Log.e(TAG, "Purchase failed with code: " + billingResult.getResponseCode());
            // Handle other errors
        }
    }

    private void handlePurchase(Purchase purchase) {
        // TODO: Handle the purchased movie ticket here
        if (purchase != null){
            // Consume the purchase to allow re-purchase
            consumePurchase(purchase);

        }
        // You can verify the purchase using the purchase object and grant access to the movie ticket
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
        } else {
            // Parse the time string from Firebase
            String firebaseTime = duration;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("duration",duration);

            editor.apply();
            String[] timeParts = firebaseTime.split(":");
            int hours = Integer.parseInt(timeParts[0]);
            int minutes = Integer.parseInt(timeParts[1]);
            int seconds = Integer.parseInt(timeParts[2]);

            // Calculate the total time in milliseconds
            totalTimeMillis = (hours * 60 * 60 * 1000) + (minutes * 60 * 1000) + (seconds * 1000);

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
    // Method to consume the purchase
    private void consumePurchase(Purchase purchase) {
        ConsumeParams consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.getPurchaseToken())
                .build();

        billingClient.consumeAsync(consumeParams, new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(@NonNull BillingResult billingResult, @NonNull String purchaseToken) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // Purchase successfully consumed. The user can buy the product again.
                    // Store the transaction ID (purchaseToken) and user's email in your SharedPreferences or database
                    savePurchaseDetailsToStorage(purchaseToken);

                    // start();
                    Log.d(TAG, "Purchase consumed successfully");
                } else {
                    // Handle error in consuming the purchase
                    Log.e(TAG, "Failed to consume purchase: " + billingResult.getResponseCode());
                }
            }
        });
    }

    private void savePurchaseDetailsToStorage(String purchaseToken) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("purchases");
        // String push = String.valueOf(reference.push());
        // Get the current date and time
        Date currentDate = new Date();

// Format the date and time as "02/11/2022 09:48AM"
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.US);
        String formattedDateTime = sdf.format(currentDate);
        String Phone = Objects.requireNonNull(auth.getCurrentUser()).getPhoneNumber();
        HashMap<String,Object> map = new HashMap<>();
        map.put("uid",auth.getCurrentUser().getUid());
        map.put("accountphone",Phone);
        map.put("tranid",purchaseToken);
        map.put("trantime",formattedDateTime);
        map.put("easypaisanumber","GOOGLE PAY BILLING");
        map.put("amount",Fprice);
        map.put("method","GOOGLE");
        map.put("film",filmname);
        String push = reference.push().getKey();
        map.put("push",push);

        reference.child(push).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    startActivity(new Intent(PaymentCheckPoint.this,VideoPlay.class).putExtra("filmurl",filmurl).putExtra("number",number)
                            .putExtra("filmname",filmname));
                    finish();
                    // Start the timer service after the purchase is successful
                    // Save the remaining time to SharedPreferences using SharedPreferencesHelper
                    String firebaseTime = duration;
                    String[] timeParts = firebaseTime.split(":");
                    int hours = Integer.parseInt(timeParts[0]);
                    int minutes = Integer.parseInt(timeParts[1]);
                    int seconds = Integer.parseInt(timeParts[2]);

// Calculate the total time in milliseconds
                    totalTimeMillis = (hours * 60 * 60 * 1000) + (minutes * 60 * 1000) + (seconds * 1000);

                    SharedPreferencesHelper.saveRemainingTime(PaymentCheckPoint.this, totalTimeMillis);
                    SharedPreferencesHelper.saveFilmName(PaymentCheckPoint.this,filmname);

// Start the timer service after the purchase is successful
                    startService(new Intent(PaymentCheckPoint.this, TimerService.class));


                } else {
                    Toast.makeText(PaymentCheckPoint.this, "Please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Revenu");
        reference1.child("Google").child(push).setValue(Fprice);
    }
}

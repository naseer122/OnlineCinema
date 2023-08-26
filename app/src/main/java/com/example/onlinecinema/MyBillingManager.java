package com.example.onlinecinema;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.Arrays;
import java.util.List;

public class MyBillingManager implements PurchasesUpdatedListener {
    private static final String TAG = "MyBillingManager";
    private static MyBillingManager instance;
    private final Context context;
    private final BillingClient billingClient;

    private MyBillingManager(Context context) {
        this.context = context.getApplicationContext();
        billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases()
                .build();

    }

    public static synchronized MyBillingManager getInstance(Context context) {
        if (instance == null) {
            instance = new MyBillingManager(context);
        }
        return instance;
    }

    public void startConnection(BillingClientStateListener listener) {
        billingClient.startConnection(listener);
    }

    public void endConnection() {
        billingClient.endConnection();
    }

    // Initiate the purchase flow for the movie ticket
    public void purchaseMovieTicket(Activity activity) {
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
                    BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(skuDetailsList.get(0))
                            .build();

                    BillingResult result = billingClient.launchBillingFlow(activity, flowParams);
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
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, List<Purchase> purchases) {
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

        }
        // You can verify the purchase using the purchase object and grant access to the movie ticket
    }


}

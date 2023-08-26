package com.example.onlinecinema;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.onlinecinema.AdsManager.*;

public class SplashActivity extends AppCompatActivity {
    FirebaseAuth auth;
    String DEMO;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentuser = auth.getCurrentUser();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                               if (currentuser!=null){
                    if (isEmulator()) {
                        // Show an error message and exit, or do whatever you want to do in this case
                        Toast.makeText(SplashActivity.this, "This app cannot run on an emulator", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        // Proceed with the rest of your code

                        startActivity(new Intent(SplashActivity.this,MainActivity.class));
                        finish();
                    }
                } else {
                    startActivity(new Intent(SplashActivity.this,Login.class).putExtra("demo",DEMO));
                    finish();
                }


            }
        },5500);
    }
    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }

}
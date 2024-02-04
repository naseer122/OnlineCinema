package com.example.onlinecinema;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
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
    String Appversion, currentverison;
    ProgressBar progressBar;
    DatabaseReference demoref;
    String account;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        DatabaseReference referenceq = FirebaseDatabase.getInstance().getReference("user");
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        progressBar = findViewById(R.id.progressBar2);
        SharedPreferences preferences = getSharedPreferences("LoginPref",MODE_PRIVATE);
        String login = preferences.getString("login","");
        String phone = preferences.getString("phone","");
        demoref = FirebaseDatabase.getInstance().getReference("publish");

        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(),0);
            currentverison = String.valueOf(info.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }

        DatabaseReference reference = database.getReference("appversion");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Appversion =  snapshot.child("version").getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        demoref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    DEMO = snapshot.child("demo").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        referenceq.child(phone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    account = snapshot.child("accountstatus").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (login.equals("ok")) {
                    if (isEmulator()) {
                        progressBar.setVisibility(View.GONE);
                        // Show an error message and exit, or do whatever you want to do in this case
                        Toast.makeText(SplashActivity.this, "This app cannot run on an emulator", Toast.LENGTH_LONG).show();
                        finish();
                    }
                    else {
                        if (currentverison.equals(Appversion)){
                            if (account.equals("Active")){
                                progressBar.setVisibility(View.GONE);
                                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                                //Toast.makeText(SplashActivity.this, BuildConfig.VERSION_CODE, Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                startActivity(new Intent(SplashActivity.this, PhoneVerfication.class).putExtra("AccountBan", "BANACCOUNT"));
                                Toast.makeText(SplashActivity.this, "Your Account is Banned", Toast.LENGTH_SHORT).show();
                                finish();
                            }

                        } else {
                            //showUpdateDialog();
                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                        }
                    }
                } else {
                    if (DEMO.equals("On")){
                        startActivity(new Intent(SplashActivity.this, Login.class));
                        finish();
                    } else {
                        if (currentverison.equals(Appversion)){
                            startActivity(new Intent(SplashActivity.this, Login.class));
                            finish();
                        } else {
                            showUpdateDialog();
                        }

                    }



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
    private void showUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please update to the latest version")
                .setPositiveButton("Update Now", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Perform any action you want after the user clicks OK
                        // For example, you can redirect the user to the app store for the update
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                            finish();
                        } catch (ActivityNotFoundException e) {
                            // If the Play Store is not installed on the device, open the website version
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                            finish();
                        }

                    }
                });
        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        // Show the dialog
        dialog.show();
    }

}
package com.example.onlinecinema;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class EasyPaisa extends AppCompatActivity {
Button Payeasy;


        String number;
        EditText PhoneEd;
FirebaseAuth auth;
    ProgressBar progressBar;
    String price,duration,filmurl,filmname ;


    //Timer
    private static final String REMAINING_TIME_KEY = "remaining_time";

    private TextView timerTextView;
    private Handler handler;
    private Runnable timerRunnable;
    private long totalTimeMillis;
String finalprice;
    private SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_easy_paisa);
        Payeasy = findViewById(R.id.payEasypaisaBtn);
        price = getIntent().getStringExtra("price");
        String pric[] = price.split("Rs");
        number = getIntent().getStringExtra("number");
        auth = FirebaseAuth.getInstance();
        finalprice = pric[1];
        PhoneEd = findViewById(R.id.phoneEdit);
        progressBar = findViewById(R.id.progressBar);
        handler = new Handler();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);


        price = getIntent().getStringExtra("price");
        duration = getIntent().getStringExtra("extraduration");
        filmurl = getIntent().getStringExtra("filmurl");
        filmname = getIntent().getStringExtra("filmname");
        Toast.makeText(this, duration + filmname + price , Toast.LENGTH_LONG).show();
    Payeasy.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (PhoneEd.getText().toString().isEmpty()){
                Toast.makeText(EasyPaisa.this, "Please Enter your EasyPaisa Number", Toast.LENGTH_SHORT).show();
            }else{
              //  mWebView = (WebView) findViewById(R.id.activity_payment_webview);
                // Enable Javascript

               // mWebView.setVisibility(View.VISIBLE);
                Date Date = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat("YYYYMMDD HHMMSS");
                String DateString = dateFormat.format(Date);
                String orderRefNumString = "T" + DateString;
                System.out.println("AhmadLogs: orderRefNum : " +orderRefNumString);
                // Convert Date to Calendar
                Calendar c = Calendar.getInstance();
                c.setTime(Date);
                c.add(Calendar.HOUR, 1);


               paywitheasypaisa();

            }

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


//    private class MyWebViewClient extends WebViewClient {
//        @Override
//        public void onPageStarted(WebView view, String url, Bitmap favicon) {
//            System.out.println("AhmadLogs: onPageStarted - url : " +url);
//            //http://localhost/easypay/order_confirm.php?auth_token=s234f5gH7jFb5d
//            //http://localhost/easypay/order_complete.php?status=000&desc=completed&orderRefNumber=123
//
//            String responseSplit[] = url.split("\\?");
//            String redirect_url = responseSplit[0];
//            String response = responseSplit[1];
//
//            System.out.println("AhmadLogs: onPageStarted - redirect_url : " +redirect_url);
//            System.out.println("AhmadLogs: onPageStarted - response : " +response);
//
//            if(redirect_url.equals(POST_BACK_URL1)) {
//                System.out.println("AhmadLogs: return url1 cancelling");
//                view.stopLoading();
//
//                String auth_tokenString = "";
//                String[] values = response.split("&");
//                for (String pair : values) {
//                    String[] nameValue = pair.split("=");
//                    if (nameValue.length == 2) {
//                        System.out.println("AhmadLogs: Name:" + nameValue[0] + " value:" + nameValue[1]);
//
//                        if (nameValue[0] == "auth_token") {
//                            auth_tokenString = nameValue[1];
//                            break;
//                        }
//                    }
//                }
//
//                String postData = "";
//                postData += "auth_token=" + auth_tokenString + "&";
//                postData += "postBackURL=" + POST_BACK_URL2;
//
//                mWebView.postUrl(TRANSACTION_POST_URL2, postData.getBytes());
//            }
//
//            else if(redirect_url.equals(POST_BACK_URL2)) {
//                //http://localhost/easypay/order_complete.php?status=000&desc=completed&orderRefNumber=123
//                System.out.println("AhmadLogs: return url2 cancelling");
//                view.stopLoading();
//
//                Intent i = new Intent(EasyPaisa.this, MainActivity.class);
//                String[] values = response.split("&");
//                for (String pair : values) {
//                    String[] nameValue = pair.split("=");
//                    if (nameValue.length == 2) {
//                        System.out.println("AhmadLogs: Name:" + nameValue[0] + " value:" + nameValue[1]);
//                        i.putExtra(nameValue[0], nameValue[1]);
//                    }
//                }
//                setResult(RESULT_OK, i);
//                finish();
//                return;
//            }
//
//            super.onPageStarted(view, url, favicon);
//        }
//    }
    public static String get_hash(String data, String key) {
        String hashString = "";
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedValue = cipher.doFinal(data.getBytes());
            hashString = Base64.encodeToString(encryptedValue, Base64.DEFAULT);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        return hashString;
    }
    private void paywitheasypaisa() {
        progressBar.setVisibility(View.VISIBLE);
        String orderid = String.valueOf(System.currentTimeMillis());
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JSONObject object = new JSONObject();
        try {
            //input your API parameters
            object.put("orderId", orderid);
            object.put("storeId", "71091");
            object.put("transactionAmount", finalprice);
            object.put("transactionType", "MA");
            object.put("mobileAccountNo", PhoneEd.getText().toString().trim());
            object.put("emailAddress", "0@1.com");
           // object.put("request", object);
            //object.put("signature", "Wx2luYCtkOMnTjWMu2FJzYsLEJwHgJGsrWHeKUErp6zdsSIZswc2vH2pm06hmeq/HZZCDjeVbrYXsV6pixq31SILoJ4GdvOj57AHzhw50o+7wS6lfh/psM6i3puD7zD0ySy8mU3NCLqkGNZnj3e3Kqmi6OXOgKc16rHy4HL9dUi74A1MuW7RdijRq8WMYfFa9c3fJFm+LaaGkoFzMR9kLxMN9BWYxJStiTQaKG9hsWwz6msG5g7GBi/ci57sOhcaIv99OQvqdTDS");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "https://easypay.easypaisa.com.pk/easypay-service/rest/v4/initiate-ma-transaction",
                object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressBar.setVisibility(View.GONE);
                try {
                    if (response.getString("responseCode").equals("0000")){
                       // start();
                        String transcationid = response.getString("transactionId");
                        String transactiontime = response.getString("transactionDateTime");
                        String easypaisanumber = PhoneEd.getText().toString();
                        savedata(transcationid,transactiontime,easypaisanumber);

                    } else if (response.getString("responseCode").equals("0001")) {
                        Toast.makeText(EasyPaisa.this, "System Error, Try Again Later", Toast.LENGTH_SHORT).show();
                    }  else if (response.getString("responseCode").equals("0002")) {
                        Toast.makeText(EasyPaisa.this, "Some Fields are missing", Toast.LENGTH_SHORT).show();
                    } else if (response.getString("responseCode").equals("0003")) {
                        Toast.makeText(EasyPaisa.this, "Invalid Order Id", Toast.LENGTH_SHORT).show();
                    } else if (response.getString("responseCode").equals("0004")) {
                        Toast.makeText(EasyPaisa.this, "Marchent does not exist", Toast.LENGTH_SHORT).show();
                    }  else if (response.getString("responseCode").equals("0013")) {
                        Toast.makeText(EasyPaisa.this, "Low Balance, Try Again Later", Toast.LENGTH_SHORT).show();
                    }
                    else if (response.getString("responseCode").equals("0013")) {
                        Toast.makeText(EasyPaisa.this, "Low Balance, Try Again Later", Toast.LENGTH_SHORT).show();
                    }
                    else if (response.getString("responseCode").equals("0013")) {
                        Toast.makeText(EasyPaisa.this, "Low Balance, Try Again Later", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EasyPaisa.this, "Payment Failed Try Again Later", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        })
        {

            /**
             * Passing some request headers
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("Credentials", "RnJlZUZpbG1zT25saW5lQ2luZW1hOjM1MDY2NGI4ZGJkMmYyYmM2NGQ5MTA4YTFhMDU4NWM4");
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(50000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(request);

    }

    private void savedata(String transcationid, String transactiontime, String easypaisanumber) {
        String Phone = Objects.requireNonNull(auth.getCurrentUser()).getPhoneNumber();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("purchases");
       // String push = String.valueOf(reference.push());
        HashMap<String,Object> map = new HashMap<>();
        map.put("uid",auth.getCurrentUser().getUid());
        map.put("accountphone",Phone);
        map.put("tranid",transcationid);
        map.put("trantime",transactiontime);
        map.put("easypaisanumber",easypaisanumber);
        map.put("amount",finalprice);
        map.put("method","easypaisa");
        map.put("film",filmname);
        String push = reference.push().getKey();
        map.put("push",push);
        reference.child(push).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    startActivity(new Intent(EasyPaisa.this,VideoPlay.class).putExtra("filmurl",filmurl).putExtra("number",number)
                            .putExtra("filmname",filmname));
                    // Start the timer service after the purchase is successful
                    // Save the remaining time to SharedPreferences
                    //     long remainingTimeMillis = sharedPreferences.getLong(REMAINING_TIME_KEY, 0);
                    // Save the remaining time to SharedPreferences using SharedPreferencesHelper
                    String firebaseTime = duration;
                    String[] timeParts = firebaseTime.split(":");
                    int hours = Integer.parseInt(timeParts[0]);
                    int minutes = Integer.parseInt(timeParts[1]);
                    int seconds = Integer.parseInt(timeParts[2]);
                    //Toast.makeText(EasyPaisa.this, response.getString("transactionId"), Toast.LENGTH_SHORT).show();

// Calculate the total time in milliseconds
                    totalTimeMillis = (hours * 60 * 60 * 1000) + (minutes * 60 * 1000) + (seconds * 1000);

                    SharedPreferencesHelper.saveRemainingTime(EasyPaisa.this, totalTimeMillis);
                    SharedPreferencesHelper.saveFilmName(EasyPaisa.this,filmname);

// Start the timer service after the purchase is successful
                    startService(new Intent(EasyPaisa.this, TimerService.class));


                } else {
                    Toast.makeText(EasyPaisa.this, "Please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Revenu");
        reference1.child("Easypaisa").child(push).setValue(finalprice);

    
    }
}
package com.example.onlinecinema;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.futuremind.recyclerviewfastscroll.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class JazzCash extends AppCompatActivity {
    EditText phoneEdit, cnicEdit;
    ProgressBar progressBar;
    String orderId = "";
    AppCompatCheckBox checkTerms;
    private static final String REMAINING_TIME_KEY = "remaining_time";
    private TextView timerTextView;
    private Handler handler;
    private Runnable timerRunnable;
    private long totalTimeMillis;
    private SharedPreferences sharedPreferences;
    String price,duration,filmurl,filmname ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jazz_cash);
        phoneEdit = findViewById(R.id.phoneEdit);
        cnicEdit = findViewById(R.id.cnicEdit);
        progressBar = findViewById(R.id.progressBar);
        checkTerms = findViewById(R.id.checkTerms);
        handler = new Handler();
        price = getIntent().getStringExtra("price");
        duration = getIntent().getStringExtra("extraduration");
        filmurl = getIntent().getStringExtra("filmurl");
        filmname = getIntent().getStringExtra("filmname");
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        findViewById(R.id.payJazzCashBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkTerms.isChecked()) {
                    Toast.makeText(JazzCash.this, "Please agree to Terms of Service in order to continue", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (phoneEdit.getText().toString().isEmpty()) {
                    Toast.makeText(JazzCash.this, "Enter Phone No", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (cnicEdit.getText().toString().isEmpty()) {
                    Toast.makeText(JazzCash.this, "Enter CNIC No", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (cnicEdit.getText().toString().length() < 13) {
                    Toast.makeText(JazzCash.this, "CNIC Must Be 13 Digits", Toast.LENGTH_SHORT).show();
                    return;
                }
                payWithJazz();
            }
        });
        TextView textView = findViewById(R.id.text);
        SpannableStringBuilder spanTxt = new SpannableStringBuilder(
                "By continuing you agree to ");
        spanTxt.append("Terms of Service");
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://freefilmsonlinecinema.com/terms-and-conditions"));
                    startActivity(browserIntent);
                } catch (Exception ignored) {
                }
            }
        }, spanTxt.length() - " Terms of Service".length(), spanTxt.length(), 0);
        spanTxt.append(" and ");
        spanTxt.append("Privacy Policy.");
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://freefilmsonlinecinema.com/privacy-and-policy"));
                    startActivity(browserIntent);
                } catch (Exception ignored) {
                }
            }
        }, spanTxt.length() - " Privacy Policy".length(), spanTxt.length(), 0);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(spanTxt, TextView.BufferType.SPANNABLE);

    }
    public void payWithJazz() {
        progressBar.setVisibility(View.VISIBLE);


        final String phone = phoneEdit.getText().toString();
        String cnicT = cnicEdit.getText().toString();

        cnicT = cnicT.substring(cnicT.length() - 6);
        try {

            Calendar expiry = Calendar.getInstance();
            expiry.add(Calendar.MONTH, 3);

            final Calendar current = Calendar.getInstance();
            current.add(Calendar.MINUTE, 1);
            orderId = String.format(Locale.getDefault(), "T%s", formatDate(String.valueOf(current.getTimeInMillis()), "yyyyMMddHHmmss"));

            int price = Integer.parseInt(getIntent().getStringExtra("price"));
            String amount = String.valueOf(price * 100);

            String hashString = "v8wt513471" + "&" + amount + "&billRef&" + cnicT + "&Hooray&EN&" +
                    "56454543" + "&" + phone + "&" +"z01twf6990" + "&PKR&" +
                    formatDate(String.valueOf(current.getTimeInMillis()), "yyyyMMddHHmmss") + "&" +
                    formatDate(String.valueOf(expiry.getTimeInMillis()), "yyyyMMddHHmmss") + "&" +
                    orderId;

            String bytes = new String(hashString.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
            String generated_hash = hmacSha(bytes);

            //https://www.ads_soft.com/success/index

            final JSONObject object = new JSONObject();
            object.put("pp_Language", "EN");
            object.put("pp_MerchantID", BuildConfig.PP_MCID);
            object.put("pp_SubMerchantID", "");
            object.put("pp_Password", BuildConfig.PP_PASS);
            object.put("pp_BankID", "");
            object.put("pp_ProductID", "");
            object.put("pp_TxnRefNo", orderId);
            object.put("pp_Amount", amount);
            object.put("pp_TxnCurrency", "PKR");
            object.put("pp_TxnDateTime", formatDate(String.valueOf(current.getTimeInMillis()), "yyyyMMddHHmmss"));
            object.put("pp_BillReference", "billRef");
            object.put("pp_Description", "Hooray");
            object.put("pp_TxnExpiryDateTime",formatDate(String.valueOf(expiry.getTimeInMillis()), "yyyyMMddHHmmss"));
            object.put("pp_SecureHash", generated_hash);
            object.put("ppmpf_1", "");
            object.put("ppmpf_2", "");
            object.put("ppmpf_3", "");
            object.put("ppmpf_4", "");
            object.put("ppmpf_5", "");
            object.put("pp_MobileNumber", phone);
            object.put("pp_CNIC", cnicT);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    OkHttpClient client = new OkHttpClient().newBuilder()
                            .connectTimeout(1000, TimeUnit.SECONDS)
                            .readTimeout(1000, TimeUnit.SECONDS)
                            .writeTimeout(1000, TimeUnit.SECONDS)
                            .build();

                    MediaType mediaType = MediaType.parse("application/json");
                    RequestBody body = RequestBody.create(mediaType, object.toString());

                    Request request = new Request.Builder()
                            .url("https://payments.jazzcash.com.pk/ApplicationAPI/API/4.0/purchase/domwallettransactionviatoken")
                            .method("POST", body)
                            .addHeader("Content-Type", "application/json")
                            .build();
                    try {
                        final Response response = client.newCall(request).execute();
                        if (response.isSuccessful() && response.body() != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String res = response.body().string();
                                        JSONObject resObject = new JSONObject(res);
                                        if (resObject.getString("pp_ResponseCode").equals("000")) {
                                            //success
                                            progressBar.setVisibility(View.GONE);
                                            start();
                                            startActivity(new Intent(JazzCash.this,VideoPlay.class).putExtra("filmurl",filmurl));


                                            Toast.makeText(JazzCash.this, "Purchase successful", Toast.LENGTH_SHORT).show();

                                        } else {
                                            //error
                                            progressBar.setVisibility(View.VISIBLE);
                                            String message = "Something went wrong. \nError: ";
                                            try {
                                                String resp = resObject.getString("pp_ResponseMessage");
                                                message = message + resp;
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            makeAlert(JazzCash.this, "Failed", message);
                                        }
                                    } catch (Exception e) {
                                        Toast.makeText(JazzCash.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.GONE);
                                    makeAlert(JazzCash.this, "Failed", "Invalid response from Server");
                                }
                            });
                        }
                    } catch (final Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.GONE);
                                makeAlert(JazzCash.this, "Failed", "An Error occurred. Error details: " + e.getLocalizedMessage());
                            }
                        });
                        e.printStackTrace();
                    }

                }
            }).start();


        } catch (Exception e) {
            e.printStackTrace();
            progressBar.setVisibility(View.GONE);
        }
    }
    private static String formatDate(String date, String pattern) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(new Date(Long.parseLong(date)));
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
    private String hmacSha(String value) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(BuildConfig.PP_HASH.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            byte[] hexArray = {(byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7', (byte) '8', (byte) '9', (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f'};
            byte[] hexChars = new byte[rawHmac.length * 2];
            for (int j = 0; j < rawHmac.length; j++) {
                int v = rawHmac[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            return new String(hexChars);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void makeAlert(Context context, String title, String body) {
        try {
            new AlertDialog.Builder(context).setTitle(title).setMessage(body).setPositiveButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            }).show();
        } catch (Exception ignored) {
        }
    }
}
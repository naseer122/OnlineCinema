package com.example.onlinecinema;

import static android.app.PendingIntent.getActivity;
import static com.google.android.exoplayer2.mediacodec.MediaCodecInfo.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;

import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

public class Login extends AppCompatActivity {
  //  FirebaseAuth auth;
    private FirebaseAuth mAuth;
    TextView demo;
    private String verificationId = "";

    Button Submit;
    EditText PasswordET;
    String finalNumber;
    FirebaseDatabase database;
    String demos;

    // Button verfil;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final EditText phoneText = findViewById(R.id.phoneEdit);
        demos = getIntent().getStringExtra("demo");
//       otpcontainer = findViewById(R.id.otpcontainer);
//       edtOTP = findViewById(R.id.idEdtOtp);
        PasswordET = findViewById(R.id.PasswordEd);
       // ccp = findViewById(R.id.ccp2);
        mAuth = FirebaseAuth.getInstance();
        demo = findViewById(R.id.demoAccount);
        database = FirebaseDatabase.getInstance();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("publish");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    demos = snapshot.child("demo").getValue(String.class);
                    if (demos.equals("on")) {
                        demo.setVisibility(View.VISIBLE);
                    }
                    // Toast.makeText(Login.this, "Data Found", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Login.this, "No Data Found", Toast.LENGTH_SHORT).show();
                    demo.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        // Toast.makeText(Login.this, "this is " + demos, Toast.LENGTH_SHORT).show();

        demo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, MainActivity.class).putExtra("demo", "demo"));
            }
        });


        Submit = findViewById(R.id.submit);
        //  verfil = findViewById(R.id.idBtnVerify);
        findViewById(R.id.contact_whatsapp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String toNumber = "+923024443676";
                String url = "https://api.whatsapp.com/send?phone=" + toNumber;
                try {
                    PackageManager pm = v.getContext().getPackageManager();
                    pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    v.getContext().startActivity(i);
                } catch (PackageManager.NameNotFoundException e) {
                    v.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }

            }
        });

        findViewById(R.id.contact_facebook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/freefilmsofficial"));
                    startActivity(browserIntent);
                } catch (Exception ignored) {
                }
            }
        });

        findViewById(R.id.contact_youtube).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/freefilms"));
                    startActivity(browserIntent);
                } catch (Exception ignored) {
                }
            }
        });

        findViewById(R.id.ac_login_privacy_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://freefilmsonlinecinema.blogspot.com/2023/04/online-cinema-privacy-policy-01-05-2023.html"));
                    startActivity(browserIntent);
                } catch (Exception ignored) {
                }
            }
        });

        //auth = FirebaseAuth.getInstance();
        Button login_btn = findViewById(R.id.ac_login_loginBtn);
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (phoneText.getText().toString().isEmpty()) {
                    phoneText.setError("enter phone");
                    return;
                }
                if (PasswordET.getText().toString().isEmpty()) {
                    PasswordET.setError("enter password");
                   return;
                }

                FirebaseDatabase database1 = FirebaseDatabase.getInstance();
                DatabaseReference reference1 = database1.getReference("user");

                Query query = reference1.orderByChild("phone").equalTo(phoneText.getText().toString());
                reference1.child(phoneText.getText().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String storedPassword = snapshot.child("password").getValue(String.class);
                            String accountStatus = snapshot.child("accountstatus").getValue(String.class);
                            String phone = snapshot.child("phone").getValue(String.class);
                            if (PasswordET.getText().toString().equals(storedPassword) && accountStatus.equals("Active")) {
                                // Password is correct and account is active
                                // Handle the successful case here
                                //Store it in Shared Prefrences
                                startActivity(new Intent(Login.this,MainActivity.class));
                                SharedPreferences LoginPref = getSharedPreferences("LoginPref",Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = LoginPref.edit();
                                editor.putString("login", "ok");
                                editor.putString("phone",phoneText.getText().toString());
                                editor.apply();
                                finish();

                            } else if (PasswordET.getText().toString().equals(storedPassword) && accountStatus.equals("UnActive")) {
                                // Password is correct but account is inactive
                                // Handle the inactive account case here
                                //Send the user to WhatsApp Admin
                                startActivity(new Intent(Login.this,PhoneVerfication.class));
                                Toast.makeText(Login.this, "Please Contact Admin on Whatsapp For Account Activation", Toast.LENGTH_LONG).show();
                            } else {
                                // Password is incorrect
                                // Handle the incorrect password case here
                                Toast.makeText(Login.this, "Password or Number is Incorrect Contact Admin", Toast.LENGTH_SHORT).show();

                            }

                        } else {
                            Toast.makeText(Login.this, "user Not Found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
//                query.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if (snapshot.exists()){
//
//                            String storedPassword = reference1.child(phoneText.getText().toString()).child("password").getValue(String.class);
//                            String accountStatus = snapshot.child("accountstatus").getValue(String.class);
//                            Toast.makeText(Login.this, storedPassword+"", Toast.LENGTH_SHORT).show();
//                            if (PasswordET.getText().toString().equals(storedPassword) && accountStatus.equals("Active")) {
//                                // Password is correct and account is active
//                                // Handle the successful case here
//                                //Store it in Shared Prefrences
//                                startActivity(new Intent(Login.this,MainActivity.class));
//                                SharedPreferences LoginPref = getSharedPreferences("LoginPref",Context.MODE_PRIVATE);
//                                SharedPreferences.Editor editor = LoginPref.edit();
//                                editor.putString("login", "ok");
//                                editor.putString("phone",phoneText.getText().toString());
//                                editor.apply();
//                                finish();
//
//                            } else if (PasswordET.getText().toString().equals(storedPassword) && accountStatus.equals("UnActive")) {
//                                // Password is correct but account is inactive
//                                // Handle the inactive account case here
//                                //Send the user to WhatsApp Admin
//                                Toast.makeText(Login.this, "Please Contact Admin on Whatsapp For Account Activation", Toast.LENGTH_LONG).show();
//                            } else {
//                                // Password is incorrect
//                                // Handle the incorrect password case here
//                                Toast.makeText(Login.this, "Password or Number is Incorrect Contact Admin", Toast.LENGTH_SHORT).show();
//
//                            }
//                        } else {
//                            // User data not found in the database
//                            // Handle the user not found case here
//                            Toast.makeText(Login.this, "user Not Found", Toast.LENGTH_SHORT).show();
//                        }
//
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });
             //   Query query1 = reference1.child("password").equalTo(PasswordET.getText().toString());


               // final String[] text = {ccp.getSelectedCountryCodeWithPlus()};
              //  String number = text[0] + phoneText.getText().toString();
                //number = number.replaceAll("[\\[\\](){}]", "");

//
//                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
//                builder.setTitle("Confirm");
//                builder.setMessage("Are you sure you want to send SMS on " + number);

//                builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//
//                       // sendVerificationCode(finalNumber);
////                        phoneText.setEnabled(false);
////                        login_btn.setVisibility(View.GONE);
//                      // otpcontainer.setVisibility(View.VISIBLE);
//                    }
//                });
//                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                        dialog.dismiss();
//                    }
//                });
//

//                String text = ccp.getSelectedCountryCodeWithPlus();
                //         String number = text + phoneText.getText().toString();
                //      number = number.replaceAll("[\\[\\](){}]", "");


            }
        });

        findViewById(R.id.signUpBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, RegisterActivity.class));
                finish();
            }
        });

    }
}
//    private void sendVerificationCode(String number) {
//        // this method is used for getting
//        // OTP on user phone number.
//        PhoneAuthOptions options =
//                PhoneAuthOptions.newBuilder(mAuth)
//                        .setPhoneNumber(number)            // Phone number to verify
//                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
//                        .setActivity(this)                 // Activity (for callback binding)
//                        .setCallbacks(mCallBack)           // OnVerificationStateChangedCallbacks
//                        .build();
//        PhoneAuthProvider.verifyPhoneNumber(options);
//      //  otpcontainer.setVisibility(View.VISIBLE);
//
//    }
//    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
//
//            // initializing our callbacks for on
//            // verification callback method.
//            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//
//        // below method is used when
//        // OTP is sent from Firebase
//        @Override
//        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
//            super.onCodeSent(s, forceResendingToken);
//            // when we receive the OTP it
//            // contains a unique id which
//            // we are storing in our string
//            // which we have already created.
//            verificationId = s;
//        }
//
//        // this method is called when user
//        // receive OTP from Firebase.
//        @Override
//        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
//            // below line is used for getting OTP code
//            // which is sent in phone auth credentials.
//            // This callback will be invoked in two situations:
//            // 1 - Instant verification. In some cases the phone number can be instantly
//            //     verified without needing to send or enter a verification code.
//            // 2 - Auto-retrieval. On some devices Google Play services can automatically
//            //     detect the incoming verification SMS and perform verification without
//            //     user action.
//            Log.d(TAG, "onVerificationCompleted:" + phoneAuthCredential);
//            final String code = phoneAuthCredential.getSmsCode();
//            // checking if the code
//            // is null or not.
//            if (code != null) {
//                // if the code is not null then
//                // we are setting that code to
//                // our OTP edittext field.
//                edtOTP.setText(code);
//                verifyCode(code);
//                // after setting this code
//                // to OTP edittext field we
//                // are calling our verifycode method.
//
//            }
//
//            //signInWithCredential(phoneAuthCredential);
//        }
//
//        // this method is called when firebase doesn't
//        // sends our OTP code due to any error or issue.
//        @Override
//        public void onVerificationFailed(FirebaseException e) {
//            // displaying error message with firebase exception.
//            Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_LONG).show();
//        }
//    };
//
//    private void verifyCode(String code) {
//        // below line is used for getting
//        // credentials from our verification id and code.
//        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
//
//        // after getting credential we are
//        // calling sign in method.
//        signInWithCredential(credential);
//    }
//    private void signInWithCredential(PhoneAuthCredential credential) {
//        // inside this method we are checking if
//        // the code entered is correct or not.
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            // if the code is correct and the task is successful
//                            // we are sending our user to new activity.
//                            String uid = mAuth.getCurrentUser().getUid();
//                            Intent i = new Intent(Login.this, MainActivity.class);
//                            i.putExtra("phone",finalNumber);
//                            startActivity(i);
//                            finish();
//                        } else {
//                            // if the code is not correct then we are
//                            // displaying an error message to the user.
//                            Toast.makeText(Login.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
//                        }
//                    }
//                });
//    }
//    }

    // [START sign_in_with_phone]
//    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            // Sign in success, update UI with the signed-in user's information
//                            Log.d(TAG, "signInWithCredential:success");
//
//                            FirebaseUser user = task.getResult().getUser();
//                            // Update UI
//                        } else {
//                            // Sign in failed, display a message and update the UI
//                            Log.w(TAG, "signInWithCredential:failure", task.getException());
//                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
//                                // The verification code entered was invalid
//                            }
//                        }
//                    }
//                });
//    }
//


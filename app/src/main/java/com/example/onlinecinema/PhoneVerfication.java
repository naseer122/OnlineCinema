package com.example.onlinecinema;

import static com.google.android.exoplayer2.mediacodec.MediaCodecInfo.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class PhoneVerfication extends AppCompatActivity {
    EditText numberEdit;
    CountryCodePicker ccp;
    // variable for FirebaseAuth class
    private FirebaseAuth mAuth;
    private  String verificationId = "";
     String finalNumber;
   // EditText edtOTP;
    Button verifyOTPBtn;
  //  LinearLayout OtpContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verfication);
       // ccp = findViewById(R.id.ccp2);
        numberEdit = findViewById(R.id.phoneEdit);
       // OtpContainer = findViewById(R.id.otpcontainer);
      //  edtOTP = findViewById(R.id.idEdtOtp);
        // below line is for getting instance
        // of our FirebaseAuth.
        mAuth = FirebaseAuth.getInstance();
       // verifyOTPBtn = findViewById(R.id.idBtnVerify);
//        findViewById(R.id.verifyBtn).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //Checking if number is provided or not
//                if (numberEdit.getText().toString().isEmpty()) {
//                    numberEdit.setError("Enter Your Number First");
//                    return;
//                }
//
//                String text = ccp.getSelectedCountryCodeWithPlus();
//                String number = text + numberEdit.getText().toString();
//                number = number.replaceAll("[\\[\\](){}]", "");
//
//                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
//                builder.setTitle("Confirm");
//                builder.setMessage("Are you sure you want to send SMS on " + number);
//                finalNumber = number;
//                builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//
//                        sendVerificationCode(finalNumber);
//                        numberEdit.setEnabled(false);
//                        findViewById(R.id.verifyBtn).setVisibility(View.GONE);
//                        //OtpContainer.setVisibility(View.VISIBLE);
//                    }
//                });
//                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
//                builder.show();
//            }
//        });
        findViewById(R.id.contact_whatsapp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String accountSuspend = getIntent().getStringExtra("AccountBan");
                String toNumber = "+923024443676";
                String url = "https://api.whatsapp.com/send?phone=" + toNumber;
                try {
                    PackageManager pm = v.getContext().getPackageManager();
                    pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
                    Intent i = new Intent(Intent.ACTION_SENDTO);
                    if (accountSuspend.equals("BANACCOUNT")){
                        i.putExtra("sms_body","Account Banned");
                    } else {
                        i.putExtra("sms_body","Account Verfication");
                    }

                   i.setData(Uri.parse("smsto:" + "+923024443676"));
                    //i.setData(Uri.parse(url));
                    v.getContext().startActivity(i);
                    finish();
                } catch (PackageManager.NameNotFoundException e) {
                    v.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    finish();
                }

            }
        });

        // initializing on click listener
        // for verify otp button
//        verifyOTPBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // validating if the OTP text field is empty or not.
//                if (TextUtils.isEmpty(edtOTP.getText().toString())) {
//                    // if the OTP text field is empty display
//                    // a message to user to enter OTP
//                    Toast.makeText(PhoneVerfication.this, "Please enter OTP", Toast.LENGTH_SHORT).show();
//                } else {
//                    // if OTP field is not empty calling
//                    // method to verify the OTP.
//                    verifyCode(edtOTP.getText().toString());
//                }
//            }
//        });
    }





    private void signInWithCredential(PhoneAuthCredential credential) {
        // inside this method we are checking if
        // the code entered is correct or not.
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // if the code is correct and the task is successful
                            // we are sending our user to new activity.
                            String uid = mAuth.getCurrentUser().getUid();
                            Intent i = new Intent(PhoneVerfication.this, RegisterActivity.class);
                            i.putExtra("phone",finalNumber);
                            i.putExtra("uid",uid);
                            startActivity(i);
                            finish();
                        } else {
                            // if the code is not correct then we are
                            // displaying an error message to the user.
                            Toast.makeText(PhoneVerfication.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    private void sendVerificationCode(String number) {
        // this method is used for getting
        // OTP on user phone number.
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(number)            // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallBack)           // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // callback method is called on Phone auth provider.
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks

            // initializing our callbacks for on
            // verification callback method.
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        // below method is used when
        // OTP is sent from Firebase
        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            // when we receive the OTP it
            // contains a unique id which
            // we are storing in our string
            // which we have already created.
            verificationId = s;
        }

        // this method is called when user
        // receive OTP from Firebase.
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            // below line is used for getting OTP code
            // which is sent in phone auth credentials.
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            final String code = phoneAuthCredential.getSmsCode();
            // checking if the code
            // is null or not.
            if (code != null) {
                // if the code is not null then
                // we are setting that code to
                // our OTP edittext field.
             //   edtOTP.setText(code);

                // after setting this code
                // to OTP edittext field we
                // are calling our verifycode method.
                verifyCode(code);
            } else {
                Toast.makeText(PhoneVerfication.this, "Did not detect OTP", Toast.LENGTH_SHORT).show();
            }
            Log.d(TAG, "onVerificationCompleted:" + phoneAuthCredential);

          //  signInWithCredential(phoneAuthCredential);
        }

        // this method is called when firebase doesn't
        // sends our OTP code due to any error or issue.
        @Override
        public void onVerificationFailed(FirebaseException e) {
            // displaying error message with firebase exception.
            Toast.makeText(PhoneVerfication.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    // below method is use to verify code from Firebase.
    private void verifyCode(String code) {
        // below line is used for getting
        // credentials from our verification id and code.
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);

        // after getting credential we are
        // calling sign in method.
        signInWithCredential(credential);
    }
}
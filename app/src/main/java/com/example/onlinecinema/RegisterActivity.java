package com.example.onlinecinema;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {
    EditText nameEdit, phoneEdit, passwordEdit,cityEt;

    String mPhoneNumber;

    String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        nameEdit = findViewById(R.id.edit_name);
        phoneEdit = findViewById(R.id.edit_phone);
        passwordEdit = findViewById(R.id.edit_password);
        cityEt = findViewById(R.id.edit_city);
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("user");
       // uid = getIntent().getStringExtra("uid");



        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        findViewById(R.id.privacy_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://freefilmsonlinecinema.blogspot.com/2023/04/online-cinema-privacy-policy-01-05-2023.html"));
                    startActivity(browserIntent);
                } catch (Exception ignored) { }
            }
        });
        findViewById(R.id.create_account_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (nameEdit.getText().toString().isEmpty()) {
                    nameEdit.setError("Enter Name");
                    return;
                }
                if (phoneEdit.getText().toString().isEmpty()) {
                    phoneEdit.setError("Enter Phone Number");
                    return;
                }
                if (passwordEdit.getText().toString().isEmpty()) {
                    passwordEdit.setError("Enter Password Please");
                    return;
                }
                if (cityEt.getText().toString().isEmpty()) {
                    cityEt.setError("Enter City Please");
                    return;
                }
                HashMap<String,Object> map = new HashMap<>();
                mPhoneNumber = phoneEdit.getText().toString();
                map.put("phone",mPhoneNumber);
                map.put("name",nameEdit.getText().toString());
                map.put("password",passwordEdit.getText().toString());
                map.put("city",cityEt.getText().toString());
                map.put("accountstatus","UnActive");
                // Get the current date and time using Calendar
                Calendar calendar = Calendar.getInstance();
                Date currentDateAndTime = calendar.getTime();

                // Format the date
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String formattedDate = dateFormat.format(currentDateAndTime);

                // Format the time
                SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                String formattedTime = timeFormat.format(currentDateAndTime);
                map.put("date",formattedDate);
                map.put("time",formattedTime);
                reference1.child(mPhoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            Toast.makeText(RegisterActivity.this,"Number Already register",Toast.LENGTH_LONG).show();
                            startActivity(new Intent(RegisterActivity.this,PhoneVerfication.class));
                        }else{
                            reference1.child(mPhoneNumber).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(RegisterActivity.this, "Account Created. Contact To Admin For Activation", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(RegisterActivity.this,PhoneVerfication.class));
                                        finish();

                                    } else {
                                        Toast.makeText(RegisterActivity.this, "please Try Again Later", Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        });


    }

}
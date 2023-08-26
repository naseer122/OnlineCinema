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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    EditText nameEdit, phoneEdit, passwordEdit,emailet;

    String mPhoneNumber;
    FirebaseAuth auth;
    DatabaseReference reference;
    String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        nameEdit = findViewById(R.id.edit_name);
        phoneEdit = findViewById(R.id.edit_phone);
        passwordEdit = findViewById(R.id.edit_password);
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("users");
        uid = getIntent().getStringExtra("uid");
        mPhoneNumber = getIntent().getStringExtra("phone");

        phoneEdit.setText(mPhoneNumber);


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
                    passwordEdit.setError("Enter City Please");
                    return;
                }


                signUp();
            }
        });


    }
    public void signUp() {
        final ProgressDialog dialog = new ProgressDialog(RegisterActivity.this);
        dialog.setIndeterminate(true);
        dialog.show();

        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("users");
        HashMap<String,Object> map = new HashMap<>();
        map.put("phone",mPhoneNumber);
        map.put("name",nameEdit.getText().toString());
        map.put("city",passwordEdit.getText().toString());
        map.put("uid",uid);
        reference1.child(uid).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    startActivity(new Intent(RegisterActivity.this,MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "please Try Again Later", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
            }
        });

    }
}
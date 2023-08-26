package com.example.onlinecinema;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Register extends AppCompatActivity {
    EditText nameEdit, phoneEdit, passwordEdit,emailet;

    FirebaseAuth auth;
    DatabaseReference reference;
    String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register2);
        nameEdit = findViewById(R.id.edit_name);
        phoneEdit = findViewById(R.id.edit_phone);
        passwordEdit = findViewById(R.id.edit_password);
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("users");


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
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://freefilmsonlinecinema.com/privacy-and-policy"));
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
                    passwordEdit.setError("Enter Password");
                    return;
                }
                if (emailet.getText().toString().isEmpty()) {
                    emailet.setError("Enter Email");
                    return;
                }

                signUp();
            }
        });


    }
    public void signUp() {
        final ProgressDialog dialog = new ProgressDialog(Register.this);
        dialog.setIndeterminate(true);
        dialog.show();
        auth.createUserWithEmailAndPassword(emailet.getText().toString(),passwordEdit.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap<String,Object> map = new HashMap<>();
                    map.put("email",emailet.getText().toString());
                    map.put("name",nameEdit.getText().toString());
                    map.put("phone",phoneEdit.getText().toString());
                    map.put("password",passwordEdit.getText().toString());
                    map.put("uid",uid);
                    reference.child(uid).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                dialog.dismiss();
                                startActivity(new Intent(Register.this,MainActivity.class));
                            }
                        }
                    });
                }
            }
        });

    }
}
package com.example.onlinecinema;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ContactUs extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<Messages> messageList = new ArrayList<>();
    private DatabaseReference messagesRef;
    private EditText messageEditText;
    private String currentUserType = "user"; // or "admin"
    String uid,Name,city,mobile;
    DatabaseReference reference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Initialize Firebase and get a reference to the messages
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        messagesRef = database.getReference("forum_messages");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        uid = auth.getCurrentUser().getUid();
        reference  = FirebaseDatabase.getInstance().getReference("users");
        loaduserdata();
        // Load messages from Firebase Realtime Database
       // loadMessages();
        adapter = new ChatAdapter(messageList);
        recyclerView.setAdapter(adapter);
        // Find the views
        messageEditText = findViewById(R.id.message_edit_text);



        findViewById(R.id.send_button).setOnClickListener(view -> onSendButtonClick());
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.forummenu, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.whatsapp) {
            openWhatsAppChooser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void openWhatsAppChooser() {
        String phoneNumber = "+923024443676"; // Replace with the desired phone number
        String message = "Online Cinema Public Forum"; // Replace with the desired message
        String url = "https://api.whatsapp.com/send?phone=" + phoneNumber;
        try {
            PackageManager pm = getPackageManager();
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            i.putExtra(Intent.EXTRA_TEXT,message);
            startActivity(Intent.createChooser(i, "Select Messaging App"));
        } catch (PackageManager.NameNotFoundException e) {
         Intent intent = new Intent(Intent.ACTION_VIEW);
         intent.setData(Uri.parse(url));
         intent.putExtra(Intent.EXTRA_TEXT,message);
            startActivity(Intent.createChooser(intent, "Select Messaging App"));
        }
    }

    private void loaduserdata() {
        reference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Name = snapshot.child("name").getValue(String.class);
                    mobile = snapshot.child("phone").getValue(String.class);
                    city = snapshot.child("city").getValue(String.class);
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });
    }

    private String getUserId() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            //uid = currentUser.getUid();
            return currentUser.getUid();
        } else {
            // User is not authenticated, handle accordingly
            return null;
        }
    }

    private String getUserName() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userName = currentUser.getDisplayName();
            return userName != null ? userName : "Default Name";
        } else {
            // User is not authenticated, handle accordingly
            return "Default Name";
        }
    }
    private void sendMessage(String text, String userId, String userName, String userType, String city, String mobile) {
        long timestamp = System.currentTimeMillis();

        String push = messagesRef.push().getKey();
        Messages message = new Messages(text, timestamp, userId, userName, city, mobile, userType,push);
        messagesRef.child(push).setValue(message);
    }
    // Function to load messages from Firebase Realtime Database
    private void loadMessages() {
        messagesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {
                Messages message = dataSnapshot.getValue(Messages.class);
                if (message != null) {
                    messageList.add(message);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {
                // Handle message changes if needed
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // Handle message removal if needed
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String prevChildKey) {
                // Handle message movement if needed
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }

    // Function to send a message
    // Function to handle send button click
    private void onSendButtonClick() {
        String messageText = messageEditText.getText().toString().trim();
        if (!messageText.isEmpty()) {
            String userId = uid;
            String userName = Name;
            String userCity = city; // Implement this function to get the user's city
            String userMobile = mobile; // Implement this function to get the user's mobile number

            sendMessage(messageText, userId, userName,currentUserType, userCity, userMobile);
            messageEditText.setText(""); // Clear the input field

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        messageList.clear();
        messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot snapshot1 : snapshot.getChildren()){
                        Messages message = snapshot1.getValue(Messages.class);
                        messageList.add(0,message);
                        adapter.notifyDataSetChanged();
                    }
                }
                adapter = new ChatAdapter(messageList);
                recyclerView.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });
    }
}
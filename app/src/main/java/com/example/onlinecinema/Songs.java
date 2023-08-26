package com.example.onlinecinema;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Songs extends AppCompatActivity {
ArrayList<ModelFilm> models;
FilmAdapter adapter;
RecyclerView SongsRv;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs);
        SongsRv = findViewById(R.id.SongsRv);
        models = new ArrayList<>();
        GridLayoutManager layoutManager = new GridLayoutManager(this,2);
        SongsRv.setLayoutManager(layoutManager);


        SongsRv.setAdapter(adapter);


    }

    @Override
    protected void onResume() {
        super.onResume();
        models.clear();
        reference = FirebaseDatabase.getInstance().getReference("songs");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot snapshot1 : snapshot.getChildren()){
                        ModelFilm a = snapshot1.getValue(ModelFilm.class);
                        models.add(0,a);

                    }
                } else {
                    Toast.makeText(Songs.this, "NO data found", Toast.LENGTH_SHORT).show();
                }
                adapter = new FilmAdapter(Songs.this,models);
                SongsRv.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
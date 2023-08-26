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

public class BehindtheScene extends AppCompatActivity {
    ArrayList<ModelFilm> models;
    FilmAdapter adapter;
    RecyclerView BTHRv;
    DatabaseReference reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_behindthe_scene);
        BTHRv = findViewById(R.id.BTHRv);
        models = new ArrayList<>();

        GridLayoutManager layoutManager = new GridLayoutManager(this,2);
        BTHRv.setLayoutManager(layoutManager);


        BTHRv.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        models.clear();
        reference = FirebaseDatabase.getInstance().getReference("bth");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot snapshot1 : snapshot.getChildren()){
                        ModelFilm a = snapshot1.getValue(ModelFilm.class);
                        models.add(0,a);
                    }
                } else {
                    Toast.makeText(BehindtheScene.this, "NO data found", Toast.LENGTH_SHORT).show();
                }
                adapter = new FilmAdapter(BehindtheScene.this,models);
                BTHRv.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
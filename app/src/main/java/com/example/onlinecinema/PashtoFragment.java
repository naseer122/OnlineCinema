package com.example.onlinecinema;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class PashtoFragment extends Fragment {

RecyclerView PashtoRv;
ArrayList<ModelFilm> models;
FilmAdapter adapter;
    DatabaseReference reference;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         View view = inflater.inflate(R.layout.fragment_pashto, container, false);
         PashtoRv = view.findViewById(R.id.PashtRv);
         models = new ArrayList<>();
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(),2);
        PashtoRv.setLayoutManager(layoutManager);


        PashtoRv.setAdapter(adapter);





         return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        models.clear();
        reference = FirebaseDatabase.getInstance().getReference("pahstofilms");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for (DataSnapshot snapshot1 : snapshot.getChildren()){
                        ModelFilm a = snapshot1.getValue(ModelFilm.class);
                        models.add(0,a);

                    }
                } else {
                    Toast.makeText(requireContext(), "NO data found", Toast.LENGTH_SHORT).show();
                }
                adapter = new FilmAdapter(requireContext(),models);
                PashtoRv.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}
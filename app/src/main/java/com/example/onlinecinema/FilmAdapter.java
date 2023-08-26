package com.example.onlinecinema;

import static com.example.onlinecinema.AdsManager.createInter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Transition;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;

public class FilmAdapter extends RecyclerView.Adapter<FilmAdapter.FilmHolder> {
Context context;
ArrayList<ModelFilm> models;
SharedPreferences preferences;
DatabaseReference reference;
FirebaseAuth auth;
String number;
AdsManager adsManager;
    FirebaseUser currentuser;
    private SharedPreferences sharedPreferences;
    private static final String REMAINING_TIME_KEY = "remaining_time";
    @NonNull
    @Override
    public FilmHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       adsManager = new AdsManager();
        createInter(context);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        auth = FirebaseAuth.getInstance();
        currentuser = auth.getCurrentUser();
        if (currentuser!=null){
            String uid = auth.getCurrentUser().getUid();

            reference = FirebaseDatabase.getInstance().getReference("users");
            reference.child(uid).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        number = snapshot.child("phone").getValue().toString();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        //Toast.makeText(context,number+"",Toast.LENGTH_LONG).show();
        return new FilmHolder(LayoutInflater.from(context).inflate(R.layout.row_list,parent,false));

    }

    @Override
    public void onBindViewHolder(@NonNull FilmHolder holder, int position) {
        ModelFilm model = models.get(position);
        holder.FilmName.setText(model.getFilmname());
        Picasso.get().load(model.getImg()).into(holder.Thumbnail);

            holder.FilmPrice.setText(model.getPrice());



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // long remainingTimeMillis = preferences.getLong(REMAINING_TIME_KEY, 0);


// For example, let's check if the "remaining_time" key exists
                long defaultValue = 0; // Provide a default value based on the data type
                // Retrieve the remaining time from SharedPreferences and update the UI
                long remainingTimeMillis = SharedPreferencesHelper.getRemainingTime(context);
                String filmname = SharedPreferencesHelper.getFilmName(context);

                if (remainingTimeMillis != defaultValue && filmname.equals(model.getFilmname())) {
                    Intent intent = new Intent(context,VideoPlay.class);
                    intent.putExtra("filmurl",model.getFilmurl());
                    intent.putExtra("filmname",model.getFilmname());
                    intent.putExtra("filmimg",model.getImg());
                    intent.putExtra("number",number);
                    context.startActivity(intent);
                    //Toast.makeText(context, model.getFilmurl() + "", Toast.LENGTH_SHORT).show();
                } else if (remainingTimeMillis != defaultValue && filmname != model.filmname && model.getStatus().equals("paid")){



                    // Create the object of AlertDialog Builder class
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    // Set the message show for the Alert time
                    builder.setMessage("You Want to watch other movie Press Yes to off Previous Movie Time.");

                    // Set Alert Title
                    builder.setTitle("Already Time Remaining");

                    // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
                    builder.setCancelable(false);

                    // Set the positive button with yes name Lambda OnClickListener method is use of DialogInterface interface.
                    builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog, which) -> {
                        // When the user click yes button then app will close
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove(REMAINING_TIME_KEY);
                        editor.remove(filmname);
                        editor.apply();

                        Toast.makeText(context, "You Can Now Buy other Movies", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(context,PaymentCheckPoint.class);
                        intent.putExtra("extraduration",model.getExtendedduration());
                        intent.putExtra("filmurl",model.getFilmurl());
                        intent.putExtra("filmname",model.getFilmname());
                        intent.putExtra("price",model.getPrice());
                        intent.putExtra("number",number);
//// Send a broadcast to stop the timer
//                        Intent stopTimerIntent = new Intent("STOP_TIMER_ACTION");
//                        LocalBroadcastManager.getInstance(context).sendBroadcast(stopTimerIntent);
//
//                        // Toast.makeText(context, model.getExtendedduration() + "" + model.getFilmname(), Toast.LENGTH_LONG).show();
                      context.startActivity(intent);

                    });
                    // Set the Negative button with No name Lambda OnClickListener method is use of DialogInterface interface.
                    builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog, which) -> {
                        // If user click no then dialog box is canceled.
                        dialog.cancel();
                    });

                    // Create the Alert dialog
                    AlertDialog alertDialog = builder.create();
                    // Show the Alert Dialog box
                    alertDialog.show();

                   // Toast.makeText(context, "Please remove timer", Toast.LENGTH_SHORT).show();
                }
                else if (remainingTimeMillis == defaultValue && model.getStatus().equals("paid")){
                    if (currentuser!= null){
                        Intent intent = new Intent(context,PaymentCheckPoint.class);
                        intent.putExtra("extraduration",model.getExtendedduration());
                        intent.putExtra("filmurl",model.getFilmurl());
                        intent.putExtra("filmname",model.getFilmname());
                        intent.putExtra("price",model.getPrice());
                        intent.putExtra("number",number);

                        // Toast.makeText(context, model.getExtendedduration() + "" + model.getFilmname(), Toast.LENGTH_LONG).show();
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "Please Login To watch movie using your phone number", Toast.LENGTH_LONG).show();
                        context.startActivity(new Intent(context,Login.class));
                    }

                } else if (model.getStatus().equals("free") && remainingTimeMillis == defaultValue || model.getStatus().equals("free") && remainingTimeMillis!= defaultValue){
                    Intent intent = new Intent(context,VideoPlay.class);
                    intent.putExtra("filmurl",model.getFilmurl());
                    intent.putExtra("filmname",model.getFilmname());
                    intent.putExtra("filmimg",model.getImg());
                    intent.putExtra("number","Online Cinema");
                   adsManager.showInter();
                    context.startActivity(intent);
                    //Toast.makeText(context, "Watch The Previous Movie First", Toast.LENGTH_LONG).show();
                }


            }
        });


    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public FilmAdapter(Context context, ArrayList<ModelFilm> models) {
        this.context = context;
        this.models = models;
    }

    class FilmHolder extends RecyclerView.ViewHolder{
        ImageView Thumbnail;
        TextView FilmName,FilmPrice;
        public FilmHolder(@NonNull View itemView) {
            super(itemView);
            Thumbnail = itemView.findViewById(R.id.thumbnailIV);
            FilmName = itemView.findViewById(R.id.filmsongtv);
            FilmPrice = itemView.findViewById(R.id.filmsongpricetv);
        }
    }
}

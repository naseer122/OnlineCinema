package com.example.onlinecinema;

import static android.app.PendingIntent.getActivity;
import static com.example.onlinecinema.AdsManager.createInter;

import android.app.Activity;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
String number,demo="";

    String url;
    String login;
    private SharedPreferences sharedPreferences;
    private static final String REMAINING_TIME_KEY = "remaining_time";
    @NonNull
    @Override
    public FilmHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
       SharedPreferences pref = context.getSharedPreferences("LoginPref",Context.MODE_PRIVATE);
       login = pref.getString("login","");
        if (context instanceof Activity) {
            Intent intent = ((Activity) context).getIntent();
            if (intent != null) {
                demo = intent.getStringExtra("key");
                // Use the data as needed
            }
        }
        SharedPreferences LoginPref = ((Context) context).getSharedPreferences("LoginPref",Context.MODE_PRIVATE);
         number = LoginPref.getString("phone","");



        return new FilmHolder(LayoutInflater.from(context).inflate(R.layout.row_list,parent,false));

    }

    @Override
    public void onBindViewHolder(@NonNull FilmHolder holder, int position) {
        ModelFilm model = models.get(position);
        holder.FilmName.setText(model.getFilmname());
        Picasso.get().load(model.getImg()).into(holder.Thumbnail);


            holder.FilmPrice.setText(model.getPrice());
        long defaultValue = 0; // Provide a default value based on the data type
        // Retrieve the remaining time from SharedPreferences and update the UI
        long remainingTimeMillis = SharedPreferencesHelper.getRemainingTime(context);
        String filmname = SharedPreferencesHelper.getFilmName(context);

        // Get the layout inflater
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_resolution, null);
        // Set up radio buttons
        final RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroup);
        final RadioButton radioButton520p = dialogView.findViewById(R.id.radioButton520p);
        final RadioButton radioButton720p = dialogView.findViewById(R.id.radioButton720p);
        final RadioButton radioButton1080p = dialogView.findViewById(R.id.radioButton1080p);
        final RadioButton radioButton360p = dialogView.findViewById(R.id.radioButton360p);
        // Set default selected radio button
// Listen for radio button changes
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioButton520p) {
                    url = model.getFilmurl520p();

                } else if (checkedId == R.id.radioButton720p) {
                    url = model.getFilmurl780p();

                } else if (checkedId == R.id.radioButton1080p) {
                    url = model.getFilmurl1080p();

                } else if (checkedId== R.id.radioButton360p) {
                    url = model.getFilmurl360p();
                }

            }

        });
// Create the AlertDialog.Builder
        AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
        builder2.setView(dialogView)
                .setTitle("Select Resolution")
                .setPositiveButton("Play", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which1) {
                        // Handle the "Play" button click
                        // You can use the selectedResolution variable to get the chosen resolution
                        // and perform the play action accordingly.
                        // For now, let's just print the selected resolution.
                       // Toast.makeText(context,tosa,Toast.LENGTH_SHORT).show();
                        if (url == null){
                            url = model.getFilmurl360p();
                        }
                            if (remainingTimeMillis != defaultValue && filmname.equals(model.getFilmname()) && model.getStatus().equals("paid")) {
                                Intent intent = new Intent(context,VideoPlay.class);
                                intent.putExtra("filmurl",url);
                                intent.putExtra("filmname",model.getFilmname());
                                intent.putExtra("filmimg",model.getImg());
                                intent.putExtra("number",number);
                                intent.putExtra("demo",demo);
                                intent.putExtra("560p",model.getFilmurl520p());
                                intent.putExtra("780p",model.getFilmurl780p());
                                intent.putExtra("360p",model.getFilmurl360p());
                                intent.putExtra("1080p",model.getFilmurl1080p());
                                intent.putExtra("Movietype","paid");

                                context.startActivity(intent);
                                //Toast.makeText(context, model.getFilmurl() + "", Toast.LENGTH_SHORT).show();
                            }
//                        else if (remainingTimeMillis != defaultValue && filmname == model.filmname && model.getStatus().equals("paid"))
//                        {
//
//
//
////                            // Create the object of AlertDialog Builder class
////                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
////                            // Set the message show for the Alert time
////                            builder.setMessage("You Want to watch other movie Press Yes to off Previous Movie Time.");
////
////                            // Set Alert Title
////                            builder.setTitle("Already Time Remaining");
////
////                            // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
////                            builder.setCancelable(false);
////
////                            // Set the positive button with yes name Lambda OnClickListener method is use of DialogInterface interface.
////                            builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog1, which) -> {
////                                // When the user click yes button then app will close
////                                SharedPreferences.Editor editor = sharedPreferences.edit();
////                                editor.remove(REMAINING_TIME_KEY);
////                                editor.remove(filmname);
////                                editor.apply();
////
////                                Toast.makeText(context, "You Can Now Buy other Movies", Toast.LENGTH_SHORT).show();
////
////                                Intent intent = new Intent(context,PaymentCheckPoint.class);
////                                intent.putExtra("extraduration",model.getExtendedduration());
////                                intent.putExtra("filmurl",url);
////                                intent.putExtra("filmname",model.getFilmname());
////                                intent.putExtra("price",model.getPrice());
////                                intent.putExtra("number",number);
////                                intent.putExtra("demo",demo);
////                                intent.putExtra("560p",model.getFilmurl520p());
////                                intent.putExtra("780p",model.getFilmurl780p());
////                                intent.putExtra("360p",model.getFilmurl360p());
////                                intent.putExtra("1080p",model.getFilmurl1080p());
//////// Send a broadcast to stop the timer
//////                        Intent stopTimerIntent = new Intent("STOP_TIMER_ACTION");
//////                        LocalBroadcastManager.getInstance(context).sendBroadcast(stopTimerIntent);
//////
//////                        // Toast.makeText(context, model.getExtendedduration() + "" + model.getFilmname(), Toast.LENGTH_LONG).show();
////                                context.startActivity(intent);
////
////                            });
////                            // Set the Negative button with No name Lambda OnClickListener method is use of DialogInterface interface.
////                            builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog2, which) -> {
////                                // If user click no then dialog box is canceled.
////                                dialog2.cancel();
////                            });
////
////                            // Create the Alert dialog
////                            AlertDialog alertDialog = builder.create();
////                            // Show the Alert Dialog box
////                            alertDialog.show();
//
//                            // Toast.makeText(context, "Please remove timer", Toast.LENGTH_SHORT).show();
//                        }
//                        else if (remainingTimeMillis == defaultValue && model.getStatus().equals("paid")){
//                            if (currentuser!= null){
//                                Intent intent = new Intent(context,PaymentCheckPoint.class);
//                                intent.putExtra("extraduration",model.getExtendedduration());
//                                intent.putExtra("filmurl",url);
//                                intent.putExtra("filmname",model.getFilmname());
//                                intent.putExtra("price",model.getPrice());
//                                intent.putExtra("number",number);
//                                intent.putExtra("demo",demo);
//                                intent.putExtra("560p",model.getFilmurl520p());
//                                intent.putExtra("780p",model.getFilmurl780p());
//                                intent.putExtra("360p",model.getFilmurl360p());
//                                intent.putExtra("1080p",model.getFilmurl1080p());
//
//                                // Toast.makeText(context, model.getExtendedduration() + "" + model.getFilmname(), Toast.LENGTH_LONG).show();
//                                context.startActivity(intent);
//                            } else {
//                                Toast.makeText(context, "Please Login To watch movie using your phone number", Toast.LENGTH_LONG).show();
//                                context.startActivity(new Intent(context,Login.class));
//                            }
//
//                        }
                            else if (model.getStatus().equals("free") && remainingTimeMillis == defaultValue || model.getStatus().equals("free") && remainingTimeMillis!= defaultValue){
                                Intent intent = new Intent(context,VideoPlay.class);
                                intent.putExtra("filmurl",url);
                                intent.putExtra("filmname",model.getFilmname());
                                intent.putExtra("filmimg",model.getImg());
                                intent.putExtra("number",number);
                                intent.putExtra("Movietype" , "free");
                                intent.putExtra("demo",demo);
                                intent.putExtra("560p",model.getFilmurl520p());
                                intent.putExtra("780p",model.getFilmurl780p());
                                intent.putExtra("360p",model.getFilmurl360p());
                                intent.putExtra("1080p",model.getFilmurl1080p());
                                context.startActivity(intent);

                                //Toast.makeText(context, "Watch The Previous Movie First", Toast.LENGTH_LONG).show();
                            }



                       // System.out.println("Selected Resolution: " + url);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Handle the "Cancel" button click if needed
                        dialog.dismiss();
                    }
                });




        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // long remainingTimeMillis = preferences.getLong(REMAINING_TIME_KEY, 0);
                if (remainingTimeMillis != defaultValue && filmname.equals(model.getFilmname())){
                    AlertDialog dialog = builder2.create();
                    dialog.show();
                }
                else if (remainingTimeMillis != defaultValue && filmname != model.filmname && model.getStatus().equals("paid")){
                  //  Toast.makeText(context, filmname, Toast.LENGTH_SHORT).show();
                    // Create the object of AlertDialog Builder class
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    // Set the message show for the Alert time
                    builder.setMessage("You Want to watch other movie Press Yes to off Previous Movie Time.");

                    // Set Alert Title
                    builder.setTitle("Already Time Remaining");

                    // Set Cancelable false for when the user clicks on the outside the Dialog Box then it will remain show
                    builder.setCancelable(false);

                    // Set the positive button with yes name Lambda OnClickListener method is use of DialogInterface interface.
                    builder.setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialog1, which) -> {
                        // When the user click yes button then app will close
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove(REMAINING_TIME_KEY);
                        editor.remove(filmname);
                        editor.apply();

                        Toast.makeText(context, "You Can Now Buy other Movies", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(context,PaymentCheckPoint.class);
                        intent.putExtra("extraduration",model.getExtendedduration());
                        intent.putExtra("filmurl",url);
                        intent.putExtra("filmname",model.getFilmname());
                        intent.putExtra("price",model.getPrice());
                        intent.putExtra("number",number);
                        intent.putExtra("demo",demo);
                        intent.putExtra("560p",model.getFilmurl520p());
                        intent.putExtra("780p",model.getFilmurl780p());
                        intent.putExtra("360p",model.getFilmurl360p());
                        intent.putExtra("1080p",model.getFilmurl1080p());
                        intent.putExtra("Movietype","paid");

//// Send a broadcast to stop the timer
//                        Intent stopTimerIntent = new Intent("STOP_TIMER_ACTION");
//                        LocalBroadcastManager.getInstance(context).sendBroadcast(stopTimerIntent);
//
//                        // Toast.makeText(context, model.getExtendedduration() + "" + model.getFilmname(), Toast.LENGTH_LONG).show();
                        context.startActivity(intent);

                    });
                    // Set the Negative button with No name Lambda OnClickListener method is use of DialogInterface interface.
                    builder.setNegativeButton("No", (DialogInterface.OnClickListener) (dialog2, which) -> {
                        // If user click no then dialog box is canceled.
                        dialog2.cancel();
                    });

                    // Create the Alert dialog
                    AlertDialog alertDialog = builder.create();
                    // Show the Alert Dialog box
                    alertDialog.show();

                }

                else if (remainingTimeMillis == defaultValue && model.getStatus().equals("paid")) {
                    if (login.equals("ok")) {
                        Intent intent = new Intent(context, PaymentCheckPoint.class);
                        intent.putExtra("extraduration", model.getExtendedduration());
                        intent.putExtra("filmurl", url);
                        intent.putExtra("filmname", model.getFilmname());
                        intent.putExtra("price", model.getPrice());
                        intent.putExtra("number", number);
                        intent.putExtra("demo", demo);
                        intent.putExtra("560p", model.getFilmurl520p());
                        intent.putExtra("780p", model.getFilmurl780p());
                        intent.putExtra("360p", model.getFilmurl360p());
                        intent.putExtra("1080p", model.getFilmurl1080p());
                        intent.putExtra("Movietype","paid");

                        // Toast.makeText(context, model.getExtendedduration() + "" + model.getFilmname(), Toast.LENGTH_LONG).show();
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "Please Login To watch movie using your phone number", Toast.LENGTH_LONG).show();
                        context.startActivity(new Intent(context, Login.class));
                    }
                }

                else{
                    AlertDialog dialog = builder2.create();
                    dialog.show();
                }

// For example, let's check if the "remaining_time" key exists


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
    public FilmAdapter(Context context, ArrayList<ModelFilm> models,String intent) {
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

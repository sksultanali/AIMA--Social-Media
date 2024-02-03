package com.developerali.aima.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.developerali.aima.Adapters.GallActAdapter;
import com.developerali.aima.CommonFeatures;
import com.developerali.aima.MainActivity;
import com.developerali.aima.Models.GalleryModel;
import com.developerali.aima.Models.UsagesModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ActivityGalleryBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    ActivityGalleryBinding binding;
    FirebaseDatabase database;
    ProgressDialog dialog;
    Activity activity;
    GallActAdapter adapter;
    String galleryId;

    private long startTime;
    private long totalSeconds;
    SharedPreferences sharedPreferences;
    Boolean showGrid2, showGrid3, linear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGalleryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();

        dialog = new ProgressDialog(GalleryActivity.this);
        dialog.setMessage("fetching images...");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        showGrid3 = false;
        showGrid2 = true;
        linear = false;

        activity = GalleryActivity.this;

        Intent intent = getIntent();
        if (intent != null && intent.getData() != null){
            Uri data = intent.getData();
            galleryId = extractLink(data);
        }else {
            galleryId = intent.getStringExtra("galleryId");
        }

        GridLayoutManager gridLayoutManager = new GridLayoutManager(GalleryActivity.this, 3);
        binding.postRecyclerView.setLayoutManager(gridLayoutManager);

        binding.showImage.setOnClickListener(v->{
            if (showGrid2){
                GridLayoutManager gnm1 = new GridLayoutManager(GalleryActivity.this, 2);
                binding.postRecyclerView.setLayoutManager(gnm1);

                showGrid2 = false;
                showGrid3 = false;
                linear = true;
            }else if (linear){
                LinearLayoutManager lnm = new LinearLayoutManager(GalleryActivity.this);
                binding.postRecyclerView.setLayoutManager(lnm);

                showGrid2 = false;
                linear = false;
                showGrid3 = true;
            }else {
                GridLayoutManager gnm1 = new GridLayoutManager(GalleryActivity.this, 3);
                binding.postRecyclerView.setLayoutManager(gnm1);

                showGrid2 = true;
                showGrid3 = false;
                linear = false;
            }
        });


        if (galleryId != null){
            database.getReference().child("gallery")
                    .child(galleryId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){

                                GalleryModel galleryModel = snapshot.getValue(GalleryModel.class);

                                if (galleryModel.getCaption() != null && galleryModel.getCaption().length()!=0){
                                    if (galleryModel.getCaption().length() > 150){
                                        String text = galleryModel.getCaption().substring(0, 135);
                                        if (text.contains("\n")) {
                                            int length = text.length(); int enterCount = 0; int enterIndex = -1;
                                            for (int i = 0; i < length; i++) {
                                                if (text.charAt(i) == '\n') {
                                                    enterCount++;
                                                    enterIndex = i - 1;

                                                    binding.galleryCaption.setText( text.substring(0, enterIndex) + "...Read more");
                                                }
                                            }
                                        }else {
                                            binding.galleryCaption.setText( text + "...Read more");
                                        }
                                    }else {
                                        binding.galleryCaption.setText(galleryModel.getCaption());
                                    }
                                }else {
                                    binding.galleryCaption.setVisibility(View.GONE);
                                }

                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy");
                                String date = simpleDateFormat.format(galleryModel.getTime());
                                binding.addedText.setText("Added On " + date);

                                binding.galleryCaption.setOnClickListener(v->{
                                    binding.galleryCaption.setText(galleryModel.getCaption());
                                });


                                adapter = new GallActAdapter(GalleryActivity.this, galleryModel.getImages());
                                binding.spinKit.setVisibility(View.GONE);
                                binding.postRecyclerView.setAdapter(adapter);
                                adapter.notifyDataSetChanged();

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }


        binding.shareGallery.setOnClickListener(v->{
            Toast.makeText(this, "loading request...", Toast.LENGTH_SHORT).show();
            String text = "This shared from AIMA Gallery. Check using app only ! \n\n" +
                    "link: https://i.aima.gallery/" + galleryId;
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/html");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
            if (sharingIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(Intent.createChooser(sharingIntent,"Share using"));
            }
        });

        binding.backImage.setOnClickListener(v->{
            onBackPressed();
        });







    }

    @Override
    protected void onResume() {
        super.onResume();
        sharedPreferences = getSharedPreferences("UsageTime", MODE_PRIVATE); //creating database
        totalSeconds = sharedPreferences.getLong("total_seconds", 0);  //getting previous value
        startTime = System.currentTimeMillis();  //get start time for counting
    }

    @Override
    protected void onPause() {
        long currentTime = System.currentTimeMillis();  //get stop time for counting
        long totalTime = currentTime - startTime;   //calculating watch time
        long newTime = totalSeconds + (totalTime/1000);    //add previous sec and now time converting in sec

        SharedPreferences.Editor editor = sharedPreferences.edit();  // updating in database
        editor.putLong("total_seconds", newTime);
        editor.apply();

        ArrayList<UsagesModel> arrayList = CommonFeatures.readListFromPref(this);
        UsagesModel usagesModel = new UsagesModel("AIMA Gallery", startTime, currentTime);
        arrayList.add(usagesModel);
        CommonFeatures.writeListInPref(GalleryActivity.this, arrayList);

        super.onPause();
    }

    private String extractLink(Uri data) {
        Toast.makeText(this, "loading request...", Toast.LENGTH_SHORT).show();
        List<String> pathSegments = data.getPathSegments();
        if (pathSegments.size() >= 1){
            String value = pathSegments.get(0);
            return value;
        }
        return "null";
    }
}
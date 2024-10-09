package com.developerali.aima.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developerali.aima.Models.UserModel;
import com.developerali.aima.Models.VideoModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ActivityVideoShowBinding;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;

public class VideoShow extends AppCompatActivity {

    ActivityVideoShowBinding binding;
    VideoModel videoModel;
    FirebaseDatabase database;
    Activity activity;
    private long startTime;
    private long totalSeconds;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVideoShowBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        database = FirebaseDatabase.getInstance();

        Intent intent = getIntent();
        activity = VideoShow.this;

        if (intent != null){
            videoModel = intent.getParcelableExtra("videoModel");

            getLifecycle().addObserver(binding.youtubePostView);
            binding.youtubePostView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                    youTubePlayer.loadVideo(videoModel.getVideoId(), 0f);
                }
            });

            if (videoModel.getCaption() != null){
                if (videoModel.getCaption().length() > 150){
                    binding.discoverCaption
                            .setText(videoModel.getCaption().substring(0, 135) + "...Read more");
                }else {
                    binding.discoverCaption.setText(videoModel.getCaption());
                }
            }else {
                binding.discoverCaption.setVisibility(View.GONE);
            }

            binding.discoverCaption.setOnClickListener(c->{
                binding.discoverCaption.setText(videoModel.getCaption());
            });


            database.getReference().child("users").child(videoModel.getUploader())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                UserModel userModel = snapshot.getValue(UserModel.class);
                                String timeAgo = TimeAgo.using(videoModel.getTime());
                                binding.discoverProfileName.setText(userModel.getName());
                                if (userModel.isVerified()){
                                    binding.verifiedProfile.setVisibility(View.VISIBLE);
                                }
                                if (userModel.getType() != null){
                                    binding.discoverProfile.setText(userModel.getType() + " • " + timeAgo + " •");
                                }else {
                                    binding.discoverProfile.setText("Public Profile" + " • " + timeAgo + " •");
                                }
                                if (userModel.getImage() != null && !activity.isDestroyed()){

                                    Glide.with(VideoShow.this)
                                            .load(userModel.getImage())
                                            .placeholder(getDrawable(R.drawable.placeholder))
                                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                            .into(binding.discoverProfileImage);
                                }

                                binding.videoProfile.setOnClickListener(v->{
                                    if (videoModel.getUploader() != null && !videoModel.getUploader().equalsIgnoreCase("admin")){
                                        Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
                                        i.putExtra("profileId", videoModel.getUploader());
                                        startActivity(i);
                                    }else {
                                        Toast.makeText(VideoShow.this, "not possible...", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            binding.videoShare.setOnClickListener(c->{
                Toast.makeText(VideoShow.this, "loading request...", Toast.LENGTH_SHORT).show();
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/html");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                        "https://www.youtube.com/watch?v=" + videoModel.getVideoId());
                if (sharingIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(Intent.createChooser(sharingIntent,"Download Using"));
                }

            });


        }


    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        sharedPreferences = getSharedPreferences("UsageTime", MODE_PRIVATE); //creating database
//        totalSeconds = sharedPreferences.getLong("total_seconds", 0);  //getting previous value
//        startTime = System.currentTimeMillis();  //get start time for counting
//    }
//
//    @Override
//    protected void onPause() {
//        long currentTime = System.currentTimeMillis();  //get stop time for counting
//        long totalTime = currentTime - startTime;   //calculating watch time
//        long newTime = totalSeconds + (totalTime/1000);    //add previous sec and now time converting in sec
//
//        SharedPreferences.Editor editor = sharedPreferences.edit();  // updating in database
//        editor.putLong("total_seconds", newTime);
//        editor.apply();
//
//        ArrayList<UsagesModel> arrayList = CommonFeatures.readListFromPref(this);
//        UsagesModel usagesModel = new UsagesModel("Seen Videos", startTime, currentTime);
//        arrayList.add(usagesModel);
//        CommonFeatures.writeListInPref(VideoShow.this, arrayList);
//
//        super.onPause();
//    }
}
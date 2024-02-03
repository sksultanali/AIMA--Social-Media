package com.developerali.aima.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developerali.aima.Activities.ProfileActivity;
import com.developerali.aima.Activities.VideoShow;
import com.developerali.aima.Models.UserModel;
import com.developerali.aima.Models.VideoModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ItemSingleVideoBinding;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;

import java.util.ArrayList;

public class VideoPostAdapter extends RecyclerView.Adapter<VideoPostAdapter.viewHolder>{

    Context context;
    Activity activity;
    ArrayList<VideoModel> models;
    private final Lifecycle lifecycle;
    FirebaseDatabase database;

    public VideoPostAdapter(Context context, ArrayList<VideoModel> models, Lifecycle lifecycle, Activity activity) {
        this.context = context;
        this.models = models;
        this.lifecycle = lifecycle;
        this.activity = activity;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_single_video, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        VideoModel videoModel = models.get(position);

        database = FirebaseDatabase.getInstance();

        if (videoModel.getVideoId() != null && !activity.isDestroyed()){
            lifecycle.addObserver(holder.binding.youtubePostView);
            holder.binding.youtubePostView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                    youTubePlayer.cueVideo(videoModel.getVideoId(), 0f);
                }

            });
        }


        if (videoModel.getCaption() != null && !activity.isDestroyed()){
            if (videoModel.getCaption().length() > 150){
                holder.binding.discoverCaption
                        .setText(videoModel.getCaption().substring(0, 135) + "...Read more");
            }else if (videoModel.getCaption().length() < 1){
                holder.binding.discoverCaption.setVisibility(View.GONE);
            }else {
                holder.binding.discoverCaption.setText(videoModel.getCaption());
            }
        }else if (videoModel.getCaption() == null){
            holder.binding.discoverCaption.setVisibility(View.GONE);
        }

        holder.binding.discoverCaption.setOnClickListener(c->{
            holder.binding.discoverCaption.setText(videoModel.getCaption());
        });

        holder.binding.fulScreen.setOnClickListener(c->{
            Intent intent = new Intent(context.getApplicationContext(), VideoShow.class);
            intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("videoModel", videoModel);
            context.startActivity(intent);
        });

        String timeAgo = TimeAgo.using(videoModel.getTime());
        if (!videoModel.getUploader().equalsIgnoreCase("Admin")){
            database.getReference().child("users").child(videoModel.getUploader())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                UserModel userModel = snapshot.getValue(UserModel.class);
                                holder.binding.discoverProfileName.setText(userModel.getName());
                                if (userModel.isVerified()){
                                    holder.binding.verifiedProfile.setVisibility(View.VISIBLE);
                                }
                                if (userModel.getType() != null){
                                    holder.binding.discoverProfile.setText(userModel.getType() + " • " + timeAgo + " •");
                                }else {
                                    holder.binding.discoverProfile.setText("Public Profile" + " • " + timeAgo + " •");
                                }
                                if (userModel.getImage() != null){

                                    Glide.with(context)
                                            .load(userModel.getImage())
                                            .placeholder(context.getDrawable(R.drawable.placeholder))
                                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                            .into(holder.binding.discoverProfileImage);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }else {
            holder.binding.discoverProfileName.setText("Admin Post");
            holder.binding.verifiedProfile.setVisibility(View.VISIBLE);
            holder.binding.discoverProfileImage.setImageDrawable(context.getDrawable(R.drawable.aimalogo));
            holder.binding.discoverProfile.setText("App Admin" + " • " + timeAgo + " •");
        }


        holder.binding.profClick.setOnClickListener(v->{
            if (videoModel.getUploader() != null && !videoModel.getUploader().equalsIgnoreCase("Admin")){
                Intent i = new Intent(context.getApplicationContext(), ProfileActivity.class);
                i.putExtra("profileId", videoModel.getUploader());
                i.setFlags(i.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }else {
                Toast.makeText(context, "not profile found...", Toast.LENGTH_SHORT).show();
            }

        });

        holder.binding.videoShare.setOnClickListener(c->{
            Toast.makeText(context, "loading request...", Toast.LENGTH_SHORT).show();
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                    "https://www.youtube.com/watch?v=" + videoModel.getVideoId());
            if (sharingIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(Intent.createChooser(sharingIntent,"Download Using"));
            }
        });


    }


    @Override
    public int getItemCount() {
        return models.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        ItemSingleVideoBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSingleVideoBinding.bind(itemView);
        }
    }
}

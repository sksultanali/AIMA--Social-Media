package com.developerali.aima.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developerali.aima.Activities.ProfileActivity;
import com.developerali.aima.Activities.VideoShow;
import com.developerali.aima.Helpers.Helper;
import com.developerali.aima.Model_Apis.PostResponse;
import com.developerali.aima.Model_Apis.VideoResponse;
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
import java.util.List;

public class VideoPostAdapter extends RecyclerView.Adapter<VideoPostAdapter.viewHolder>{

    Activity activity;
    ArrayList<VideoResponse.PostData> models;
    private final Lifecycle lifecycle;
    FirebaseDatabase database;
    boolean myPost;
    Animation animation;

    public VideoPostAdapter(ArrayList<VideoResponse.PostData> models, Lifecycle lifecycle,
                            Activity activity, boolean myPost) {
        this.models = models;
        this.lifecycle = lifecycle;
        this.activity = activity;
        this.myPost = myPost;
        animation = AnimationUtils.loadAnimation(activity, R.anim.blink);
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_single_video, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        VideoResponse.PostData videoModel = models.get(position);

        database = FirebaseDatabase.getInstance();

        if (videoModel.getYouTubeId() != null && !activity.isDestroyed()){
            lifecycle.addObserver(holder.binding.youtubePostView);
            holder.binding.youtubePostView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                    youTubePlayer.cueVideo(videoModel.getYouTubeId(), 0f);
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
            Intent intent = new Intent(activity.getApplicationContext(), VideoShow.class);
            intent.putExtra("videoModel", videoModel);
            activity.startActivity(intent);
        });

        long time = Helper.convertToLongTime(videoModel.getTime());
        String timeAgo = (time == -1) ? Helper.formatDate("yyyy-MM-dd HH:mm:ss", "dd LLL yyyy", videoModel.getTime()) :
                TimeAgo.using(time);

        if (myPost){
            if (Helper.userDetails != null){
                videoModel.setUploader(Helper.userDetails.getUserId());
                holder.binding.discoverProfileName.setText(Helper.userDetails.getName());
                holder.binding.discoverProfile.setText(videoModel.getStatus() + " • " + timeAgo + " •");
                if (videoModel.getStatus().equalsIgnoreCase("Approved")){
                    holder.binding.discoverProfile.setTextColor(activity.getColor(R.color.green_colour));
                } else if (videoModel.getStatus().equalsIgnoreCase("Pending Approval")) {
                    holder.binding.discoverProfile.setTextColor(activity.getColor(R.color.backgroundBottomColour));
                    holder.binding.discoverProfile.setAnimation(animation);
                }else {
                    holder.binding.discoverProfile.setTextColor(activity.getColor(R.color.red_colour));
                }
                Helper.showBadge(videoModel.getVerified(), videoModel.getVerified_valid(), holder.binding.verifiedProfile);
                if (Helper.userDetails.getImage() != null && !activity.isDestroyed()){
                    Glide.with(activity.getApplicationContext())
                            .load(Helper.userDetails.getImage())
                            .placeholder(activity.getDrawable(R.drawable.profileplaceholder))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .override(50, 50)
                            .priority(Priority.HIGH)
                            .into(holder.binding.discoverProfileImage);
                }
            }
        } else if (!videoModel.getUploader().equalsIgnoreCase("Admin")){
            holder.binding.discoverProfileName.setText(videoModel.getName());
            Helper.showBadge(videoModel.getVerified(), videoModel.getVerified_valid(), holder.binding.verifiedProfile);
            if (videoModel.getType() != null){
                holder.binding.discoverProfile.setText(videoModel.getType() + " • " + timeAgo + " •");
            }else {
                holder.binding.discoverProfile.setText("Public Profile" + " • " + timeAgo + " •");
            }
            if (videoModel.getUser_image() != null){
                Glide.with(activity.getApplicationContext())
                        .load(videoModel.getUser_image())
                        .placeholder(activity.getDrawable(R.drawable.placeholder))
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(holder.binding.discoverProfileImage);
            }
        }else {
            holder.binding.discoverProfileName.setText("Admin Post");
            holder.binding.verifiedProfile.setVisibility(View.VISIBLE);
            holder.binding.discoverProfileImage.setImageDrawable(activity.getDrawable(R.drawable.aimalogo));
            holder.binding.discoverProfile.setText("App Admin" + " • " + timeAgo + " •");
        }


        holder.binding.profClick.setOnClickListener(v->{
            if (videoModel.getUploader() != null && !videoModel.getUploader().equalsIgnoreCase("Admin")){
                Intent i = new Intent(activity.getApplicationContext(), ProfileActivity.class);
                i.putExtra("profileId", videoModel.getUploader());
                activity.startActivity(i);
            }else {
                Helper.showAlertNoAction(activity, "Admin Profile",
                        "This profile can't be check by you. Have a good day!", "Thank You");
            }

        });

        holder.binding.videoShare.setOnClickListener(c->{
            Toast.makeText(activity, "loading request...", Toast.LENGTH_SHORT).show();
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                    "https://www.youtube.com/watch?v=" + videoModel.getVideoId());
            if (sharingIntent.resolveActivity(activity.getPackageManager()) != null) {
                activity.startActivity(Intent.createChooser(sharingIntent,"Download Using"));
            }
        });


    }

    public void addItems(List<VideoResponse.PostData> newItems) {
//        this.models.addAll(newVoters);
//        notifyDataSetChanged();
        int startPosition = models.size();
        models.addAll(newItems);
        notifyItemRangeInserted(startPosition, newItems.size());
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder{
        ItemSingleVideoBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSingleVideoBinding.bind(itemView);
        }
    }
}

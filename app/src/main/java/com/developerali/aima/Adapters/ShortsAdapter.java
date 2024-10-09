package com.developerali.aima.Adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developerali.aima.Models.UserModel;
import com.developerali.aima.Models.shortsModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.SignleVideoRowBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;

public class ShortsAdapter extends FirebaseRecyclerAdapter<shortsModel, ShortsAdapter.viewHolder>{


    public ShortsAdapter(@NonNull FirebaseRecyclerOptions<shortsModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull viewHolder holder, int position, @NonNull shortsModel model) {
        holder.setdata(model);
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.signle_video_row, parent, false);
        return new viewHolder(view);
    }

    public static class viewHolder extends RecyclerView.ViewHolder{
        SignleVideoRowBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = SignleVideoRowBinding.bind(itemView);
        }

        void setdata(shortsModel obj) {
            binding.youtubeMineView
                    .addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                        @Override
                        public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                            String videoId = obj.getVideoLink();
                            youTubePlayer.cueVideo(videoId, 0f);
                        }

                    });



            FirebaseDatabase.getInstance().getReference().child("users")
                    .child(obj.getUploader())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            UserModel userModel = snapshot.getValue(UserModel.class);
                            if (userModel != null){
                                if (userModel.getImage() != null){
                                    Glide.with(itemView.getContext())
                                            .load(userModel.getImage())
                                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                            .into(binding.uploadedImage);
                                }

                                String timeAgo = TimeAgo.using(obj.getTime());

                                binding.uploadNameAs.setText(userModel.getName());
                                if (userModel.isVerified()){
                                    binding.verifiedProfile.setVisibility(View.VISIBLE);
                                }
                                if (userModel.getType() != null){
                                    binding.uploadTimeProfile.setText(userModel.getType() + " • " + timeAgo + " •");
                                }else {
                                    binding.uploadTimeProfile.setText("Public Profile" + " • " + timeAgo + " •");
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            binding.shareLink.setOnClickListener(v->{
                Toast.makeText(v.getContext(), "loading request...", Toast.LENGTH_SHORT).show();
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "https://www.youtube.com/shorts/"+obj.getVideoLink());
                v.getContext().startActivity(Intent.createChooser(sharingIntent,"Share using"));
            });

            binding.downloadLink.setOnClickListener(v->{
                Toast.makeText(v.getContext(), "coming soon !", Toast.LENGTH_SHORT).show();
            });

        }
    }
}

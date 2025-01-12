package com.developerali.aima.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developerali.aima.Activities.ProfileActivity;
import com.developerali.aima.Helpers.Helper;
import com.developerali.aima.Helpers.TextUtils;
import com.developerali.aima.Helpers.UserDataUpdate;
import com.developerali.aima.MainActivity;
import com.developerali.aima.Model_Apis.PostResponse;
import com.developerali.aima.Models.FollowModel;
import com.developerali.aima.Models.NotificationModel;
import com.developerali.aima.Models.UserModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ItemFollowBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FollowAdapter extends RecyclerView.Adapter<FollowAdapter.vieHolder>{

    ArrayList<UserModel> list;
    Activity context;
    FirebaseDatabase database;
    FirebaseAuth auth;
    UserDataUpdate userDataUpdate;

    public FollowAdapter(Activity context, ArrayList<UserModel> list){
        this.context = context;
        this.list = list;
        userDataUpdate = new UserDataUpdate();
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public vieHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_follow, parent, false);
        return new vieHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull vieHolder holder, int position) {
        UserModel model = list.get(position);

        if (model.getImage() != null){
            Glide.with(context)
                    .load(model.getImage())
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .placeholder(R.drawable.profileplaceholder)
                    .into(holder.binding.followProfile);
        }

        holder.binding.followName.setText(model.getName());
        if (model.getFollowing() > 1000){
            holder.binding.followMemberType.setText(model.getType() + " • " + TextUtils.formatVideoViews(model.getFollowing()));
        }else {
            holder.binding.followMemberType.setText(model.getType());
        }


        if (model.getBio() != null && !model.getBio().isEmpty()){
            holder.binding.followMemberBio.setVisibility(View.VISIBLE);
            holder.binding.followMemberBio.setText(model.getBio());
        }else {
            holder.binding.followMemberBio.setVisibility(View.GONE);
        }

        Helper.showBadge(model.getVerified(), model.getVerified_valid(), holder.binding.verifiedProfile);

        database.getReference().child("users").child(model.getUserId())
                .child("follows").child(auth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            holder.binding.unfollowButton.setVisibility(View.VISIBLE);
                            holder.binding.followButton.setVisibility(View.GONE);
                        }else {
                            holder.binding.unfollowButton.setVisibility(View.GONE);
                            holder.binding.followButton.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });



        holder.itemView.setOnClickListener(c->{
            Intent i = new Intent(context.getApplicationContext(), ProfileActivity.class);
            i.putExtra("profileId", model.getUserId());
            context.startActivity(i);
        });

        holder.binding.unfollowButton.setOnClickListener(c->{
            FollowModel followModel = new FollowModel();
            followModel.setFollowBy(auth.getCurrentUser().getUid());
            followModel.setFollowAt(new Date().getTime());

            database.getReference().child("users").child(model.getUserId())
                    .child("follows").child(auth.getCurrentUser().getUid()).removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            userDataUpdate.enqueueUpdateTask(model.getUserId(), "follower", String.valueOf(-1), ()->{
                                userDataUpdate.enqueueUpdateTask(auth.getCurrentUser().getUid(), "following", String.valueOf(-1), ()->{
                                    holder.binding.unfollowButton.setVisibility(View.GONE);
                                    holder.binding.followButton.setVisibility(View.VISIBLE);
                                    NotificationModel notificationModel = new NotificationModel(auth.getCurrentUser().getUid(),
                                            "unfollow", auth.getCurrentUser().getUid(), new Date().getTime(), false);
                                    database.getReference().child("notification")
                                            .child(model.getUserId()).push().setValue(notificationModel);

                                });
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        holder.binding.followButton.setOnClickListener(c->{
            FollowModel followModel = new FollowModel();
            followModel.setFollowBy(auth.getCurrentUser().getUid());
            followModel.setFollowAt(new Date().getTime());

            database.getReference().child("users").child(model.getUserId())
                    .child("follows").child(auth.getCurrentUser().getUid()).setValue(followModel)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            userDataUpdate.enqueueUpdateTask(model.getUserId(), "follower", String.valueOf(1), ()->{
                                MainActivity.sendNotification(model.getToken(), "Follower",
                                        "Someone started following you!");
                                userDataUpdate.enqueueUpdateTask(auth.getCurrentUser().getUid(), "following", String.valueOf(1), ()->{
                                    holder.binding.unfollowButton.setVisibility(View.VISIBLE);
                                    holder.binding.followButton.setVisibility(View.GONE);
                                    NotificationModel notificationModel = new NotificationModel(auth.getCurrentUser().getUid(),
                                            "follow", auth.getCurrentUser().getUid(), new Date().getTime(), false);
                                    database.getReference().child("notification")
                                            .child(model.getUserId()).push().setValue(notificationModel);
                                });
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

    }

    public void addItems(List<UserModel> newItems) {
        int startPosition = list.size();
        list.addAll(newItems);
        notifyItemRangeInserted(startPosition, newItems.size());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class vieHolder extends RecyclerView.ViewHolder{
        ItemFollowBinding binding;
        public vieHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemFollowBinding.bind(itemView);
        }
    }
}

package com.developerali.aima.Adapters;

import android.content.Context;
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

public class FollowAdapter extends RecyclerView.Adapter<FollowAdapter.vieHolder>{

    ArrayList<UserModel> list;
    Context context;
    FirebaseDatabase database;
    FirebaseAuth auth;

    public FollowAdapter(Context context, ArrayList<UserModel> list){
        this.context = context;
        this.list = list;
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
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        if (model.getImage() != null){
            Glide.with(context)
                            .load(model.getImage())
                                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                            .placeholder(R.drawable.profileplaceholder)
                                                    .into(holder.binding.followProfile);
        }

        holder.binding.followName.setText(model.getName());
        if (model.getType() != null){
            holder.binding.followMemberType.setText(model.getType());
        }else {
            holder.binding.followMemberType.setText(R.string.public_profile);
        }

        if (model.getBio() != null){
            holder.binding.followMemberBio.setVisibility(View.VISIBLE);
            holder.binding.followMemberBio.setText(model.getBio());
        }


        holder.itemView.setOnClickListener(c->{
            Intent i = new Intent(context.getApplicationContext(), ProfileActivity.class);
            i.putExtra("profileId", model.getUserId());
            context.startActivity(i);
        });


        database.getReference().child("users").child(model.getUserId())
                .child("follows").child(auth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            holder.binding.followButton.setText("Unfollow");
                            holder.binding.followButton.setTextColor(context.getColor(R.color.black));
                            holder.binding.followButton
                                    .setBackground(context.getDrawable(R.drawable.button_already_followd));


                            holder.binding.followButton.setOnClickListener(c->{
                                FollowModel followModel = new FollowModel();
                                followModel.setFollowBy(auth.getCurrentUser().getUid());
                                followModel.setFollowAt(new Date().getTime());

                                database.getReference().child("users").child(model.getUserId())
                                        .child("follows").child(auth.getCurrentUser().getUid()).removeValue()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {

                                                        database.getReference().child("users").child(model.getUserId())
                                                                .child("follower")
                                                                .setValue(model.getFollower() - 1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void unused) {



                                                                        database.getReference().child("users").child(auth.getCurrentUser().getUid())
                                                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                        if (snapshot.exists()){
                                                                                            UserModel userModel = snapshot.getValue(UserModel.class);
                                                                                            database.getReference().child("users").child(auth.getCurrentUser().getUid())
                                                                                                    .child("following")
                                                                                                    .setValue(userModel.getFollowing() - 1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onSuccess(Void unused) {
                                                                                                            holder.binding.followButton.setText("Follow");
                                                                                                            holder.binding.followButton.setTextColor(context.getColor(R.color.white));
                                                                                                            holder.binding.followButton
                                                                                                                    .setBackground(context.getDrawable(R.drawable.button_follow_background));

                                                                                                            NotificationModel notificationModel = new NotificationModel(auth.getCurrentUser().getUid(),
                                                                                                                    "follow", auth.getCurrentUser().getUid(), new Date().getTime(), false);
                                                                                                            database.getReference().child("notification")
                                                                                                                    .child(model.getUserId())
                                                                                                                    .push()
                                                                                                                    .setValue(notificationModel);
                                                                                                        }
                                                                                                    });
                                                                                        }
                                                                                    }

                                                                                    @Override
                                                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                                                    }
                                                                                });






                                                                    }
                                                                });
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            });



                        }else {

                            holder.binding.followButton.setOnClickListener(c->{
                                FollowModel followModel = new FollowModel();
                                followModel.setFollowBy(auth.getCurrentUser().getUid());
                                followModel.setFollowAt(new Date().getTime());

                                database.getReference().child("users").child(model.getUserId())
                                        .child("follows").child(auth.getCurrentUser().getUid())
                                        .setValue(followModel)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                                database.getReference().child("users").child(model.getUserId())
                                                        .child("follower")
                                                        .setValue(model.getFollower() + 1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {



                                                                database.getReference().child("users").child(auth.getCurrentUser().getUid())
                                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                                                if (snapshot.exists()){
                                                                                    UserModel userModel = snapshot.getValue(UserModel.class);
                                                                                    database.getReference().child("users").child(auth.getCurrentUser().getUid())
                                                                                            .child("following")
                                                                                            .setValue(userModel.getFollowing() + 1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                                @Override
                                                                                                public void onSuccess(Void unused) {
                                                                                                    holder.binding.followButton.setText("Follow");
                                                                                                    holder.binding.followButton.setTextColor(context.getColor(R.color.white));
                                                                                                    holder.binding.followButton
                                                                                                            .setBackground(context.getDrawable(R.drawable.button_follow_background));
                                                                                                }
                                                                                            });
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(@NonNull DatabaseError error) {

                                                                            }
                                                                        });





                                                            }
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
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });




    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class vieHolder extends RecyclerView.ViewHolder{
        ItemFollowBinding binding;
        public vieHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemFollowBinding.bind(itemView);
        }
    }
}

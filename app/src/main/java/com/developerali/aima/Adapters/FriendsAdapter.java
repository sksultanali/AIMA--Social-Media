package com.developerali.aima.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developerali.aima.Activities.ProfileActivity;
import com.developerali.aima.Models.FollowModel;
import com.developerali.aima.Models.UserModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ItemFollowProfileBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.viewHolder>{

    ArrayList<FollowModel> models;
    Context context;
    FirebaseDatabase database;


    public FriendsAdapter(ArrayList<FollowModel> list, Context context){
        this.context = context;
        this.models = list;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_follow_profile, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        FollowModel followModel = models.get(position);
        database = FirebaseDatabase.getInstance();
        database.getReference().child("users").child(followModel.getFollowBy())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel userModel = snapshot.getValue(UserModel.class);
                        if (userModel.getImage() != null){
                            Glide.with(context)
                                    .load(userModel.getImage())
                                    .placeholder(context.getDrawable(R.drawable.profileplaceholder))
                                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                    .into(holder.binding.followerProfile);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        holder.itemView.setOnClickListener(v->{
            Intent i = new Intent(context.getApplicationContext(), ProfileActivity.class);
            i.putExtra("profileId", followModel.getFollowBy());
            context.startActivity(i);
        });

    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        ItemFollowProfileBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemFollowProfileBinding.bind(itemView);
        }
    }
}

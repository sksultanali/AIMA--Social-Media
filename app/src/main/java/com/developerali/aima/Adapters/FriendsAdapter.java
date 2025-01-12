package com.developerali.aima.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developerali.aima.Activities.ProfileActivity;
import com.developerali.aima.Model_Apis.ApiService;
import com.developerali.aima.Model_Apis.RetrofitClient;
import com.developerali.aima.Model_Apis.UserDetails;
import com.developerali.aima.Models.FollowModel;
import com.developerali.aima.Models.UserModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ItemFollowProfileBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.viewHolder>{

    ArrayList<FollowModel> models;
    Activity context;
    FirebaseDatabase database;
    ApiService apiService;
    Call<UserDetails> call;

    public FriendsAdapter(ArrayList<FollowModel> list, Activity context){
        this.context = context;
        this.models = list;
        apiService = RetrofitClient.getClient().create(ApiService.class);
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

        call = apiService.getUserDetails("getUserDetails", followModel.getFollowBy());
        call.enqueue(new Callback<UserDetails>() {
            @Override
            public void onResponse(Call<UserDetails> call, Response<UserDetails> response) {
                if (response.isSuccessful() && response.body() != null){
                    UserDetails userDetails = response.body();
                    if (userDetails.getStatus().equalsIgnoreCase("success")){
                        UserModel userModel = userDetails.getData();
                        if (userModel.getImage() != null && !userModel.getImage().isEmpty()){
                            Glide.with(context)
                                    .load(userModel.getImage())
                                    .placeholder(context.getDrawable(R.drawable.profileplaceholder))
                                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                    .into(holder.binding.followerProfile);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<UserDetails> call, Throwable t) {

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

    public static class viewHolder extends RecyclerView.ViewHolder{
        ItemFollowProfileBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemFollowProfileBinding.bind(itemView);
        }
    }
}

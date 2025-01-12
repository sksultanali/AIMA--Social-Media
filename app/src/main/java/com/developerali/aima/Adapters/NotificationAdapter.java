package com.developerali.aima.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developerali.aima.Activities.Comments_Post;
import com.developerali.aima.Activities.ProfileActivity;
import com.developerali.aima.Activities.See_Post;
import com.developerali.aima.Helpers.Helper;
import com.developerali.aima.Model_Apis.ApiService;
import com.developerali.aima.Model_Apis.RetrofitClient;
import com.developerali.aima.Model_Apis.UserDetails;
import com.developerali.aima.Models.NotificationModel;
import com.developerali.aima.Models.UserModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ItemNotificationLayoutBinding;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.viewHolder>{

    ArrayList<NotificationModel> models;
    Activity activity;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ApiService apiService;

    public NotificationAdapter(ArrayList<NotificationModel> models, Activity activity) {
        this.models = models;
        this.activity = activity;
        apiService = RetrofitClient.getClient().create(ApiService.class);
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_notification_layout, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        NotificationModel notificationModel = models.get(position);
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        if (notificationModel.getSeen()){
            holder.binding.backgroundNotification.setBackgroundColor(activity.getColor(R.color.backgroundColour));
        }

        if (notificationModel.getNotifyBy().equalsIgnoreCase("Admin")){
            holder.binding.notificationImage.setImageDrawable(activity.getDrawable(R.drawable.aimalogo));
            String timeAgo = TimeAgo.using(notificationModel.getNotifyAt());
            holder.binding.notificationType.setText(Html.fromHtml("<b>Admin</b>" + " • "
                    + timeAgo
                    + " • ") + notificationModel.getType());
        }


        Call<UserDetails> call = apiService.getUserDetails(
                "getUserDetails", notificationModel.getNotifyBy()
        );

        call.enqueue(new Callback<UserDetails>() {
            @Override
            public void onResponse(Call<UserDetails> call, Response<UserDetails> response) {
                if (response.isSuccessful() && response.body() != null){
                    UserDetails apiResponse = response.body();
                    if (apiResponse.getStatus().equalsIgnoreCase("success")){
                        UserModel userModel = apiResponse.getData();
                        if (userModel != null){
                            if (userModel.getImage() != null && !activity.isDestroyed()){
                                Glide.with(activity.getApplicationContext())
                                        .load(userModel.getImage())
                                        .skipMemoryCache(true)
                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                        .placeholder(activity.getDrawable(R.drawable.profileplaceholder))
                                        .into(holder.binding.notificationImage);
                            }
                            String timeAgo = TimeAgo.using(notificationModel.getNotifyAt());

                            if (notificationModel.getType().equalsIgnoreCase("like")){
                                holder.binding.notificationType.setText(Html.fromHtml("<b>" + userModel.getName() + "</b>"
                                        + " • " + timeAgo
                                        + " •  liked on your post."));


                            }else if (notificationModel.getType().equalsIgnoreCase("comment")){
                                holder.binding.notificationType.setText(Html.fromHtml("<b>" + userModel.getName() + "</b>"
                                        + " • " + timeAgo
                                        + " • commented on your post."));
                            }else if (notificationModel.getType().equalsIgnoreCase("follow")){

                                holder.binding.notificationType.setText(Html.fromHtml("<b>" + userModel.getName() + "</b>" + " • "
                                        + timeAgo
                                        + " • followed your profile. See profile"));
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<UserDetails> call, Throwable t) {
            }
        });

        holder.itemView.setOnClickListener(v->{
            database.getReference().child("notification")
                    .child(auth.getCurrentUser().getUid())
                    .child(notificationModel.getNotificationId())
                    .child("seen")
                    .setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            if (notificationModel.getType().equalsIgnoreCase("like")){
                                Intent i = new Intent(activity.getApplicationContext(), See_Post.class);
                                i.putExtra("postId", notificationModel.getId());
                                activity.startActivity(i);
                            }else if (notificationModel.getType().equalsIgnoreCase("comment")){
                                Intent i = new Intent(activity.getApplicationContext(), Comments_Post.class);
                                i.putExtra("postId", notificationModel.getId());
                                activity.startActivity(i);
                            }else if (notificationModel.getType().equalsIgnoreCase("follow")){
                                if (notificationModel.getNotifyBy() != null){
                                    Intent i = new Intent(activity.getApplicationContext(), ProfileActivity.class);
                                    i.putExtra("profileId", notificationModel.getNotifyBy());
                                    activity.startActivity(i);
                                }else {
                                    Toast.makeText(activity, "wait a while...", Toast.LENGTH_SHORT).show();
                                }
                            }
                            holder.binding.backgroundNotification.setBackgroundColor(activity.getColor(R.color.backgroundColour));
                        }
                    });
        });

    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder{
        ItemNotificationLayoutBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemNotificationLayoutBinding.bind(itemView);
        }
    }
}

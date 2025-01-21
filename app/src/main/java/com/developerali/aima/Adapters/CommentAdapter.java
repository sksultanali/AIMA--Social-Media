package com.developerali.aima.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developerali.aima.Helpers.Helper;
import com.developerali.aima.Model_Apis.ApiResponse;
import com.developerali.aima.Model_Apis.ApiService;
import com.developerali.aima.Model_Apis.RetrofitClient;
import com.developerali.aima.Model_Apis.UserDetails;
import com.developerali.aima.Models.CommentModel;
import com.developerali.aima.Models.UserModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ItemCommentBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.viewHolder>{

    ArrayList<CommentModel> models;
    FirebaseDatabase database;
    Activity activity;
    ApiService apiService;
    FirebaseAuth auth;

    public CommentAdapter(Activity activity, ArrayList<CommentModel> list){
        this.models = list;
        this.activity = activity;
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        apiService = RetrofitClient.getClient().create(ApiService.class);
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_comment, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, @SuppressLint("RecyclerView") int position) {
        CommentModel commentModel = models.get(position);
        Call<UserDetails> call = apiService.getUserDetails(
                "getUserDetails", commentModel.getCommentedBy()
        );

        call.enqueue(new Callback<UserDetails>() {
            @Override
            public void onResponse(Call<UserDetails> call, Response<UserDetails> response) {
                if (response.isSuccessful() && response.body() != null){
                    UserDetails apiResponse = response.body();
                    if (apiResponse.getStatus().equalsIgnoreCase("success")){
                        if (apiResponse.getData() != null){
                            if (apiResponse.getData().getImage() != null && !activity.isDestroyed()
                                    && !apiResponse.getData().getImage().isEmpty()){
                                Glide.with(activity.getApplicationContext())
                                        .load(apiResponse.getData().getImage())
                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                        .placeholder(activity.getDrawable(R.drawable.profileplaceholder))
                                        .into(holder.binding.CommentsProfileImage);
                            }
                            holder.binding.commentProfileName.setText(apiResponse.getData().getName());
                            Helper.showBadge(apiResponse.getData().getVerified(), apiResponse.getData().getVerified_valid(),
                                    holder.binding.verifiedProfile);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<UserDetails> call, Throwable t) {
            }
        });
        if (commentModel.getComment().length() > 300){
            holder.binding.actualComment.setText(commentModel.getComment().substring(0, 135) + "...Read more");
        }else {
            holder.binding.actualComment.setText(commentModel.getComment());
        }

        if (commentModel.getCommentedBy().equalsIgnoreCase(auth.getCurrentUser().getUid())){
            holder.binding.dots.setVisibility(View.VISIBLE);
        }else {
            holder.binding.dots.setVisibility(View.GONE);
        }

        holder.binding.dots.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(activity, holder.binding.dots);
            popupMenu.getMenuInflater().inflate(R.menu.delete_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.deleteMenu) {
                    if (database != null && commentModel.getPostId() != null && commentModel.getCommentId() != null) {
                        database.getReference()
                                .child("comments")
                                .child(commentModel.getPostId())
                                .child(commentModel.getCommentId())
                                .removeValue()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Call<ApiResponse> call2 = apiService.updatePostField(
                                                "updatePostField", commentModel.getPostId(), "commentsCount", String.valueOf(-1)
                                        );
                                        call2.enqueue(new Callback<ApiResponse>() {
                                            @Override
                                            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                                            }

                                            @Override
                                            public void onFailure(Call<ApiResponse> call, Throwable t) {

                                            }
                                        });
                                        try {
                                            models.remove(position);
                                            notifyItemRemoved(position);
                                            notifyItemRangeChanged(position, models.size());
                                        }catch (Exception e){

                                        }
                                        Toast.makeText(activity, "Comment deleted!", Toast.LENGTH_SHORT).show();
                                        notifyDataSetChanged();
                                    } else {
                                        Toast.makeText(activity, "Failed to delete comment!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(activity, "Unable to delete comment. Invalid data.", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            });
            popupMenu.show();
        });



    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder{
        ItemCommentBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemCommentBinding.bind(itemView);
        }
    }

}

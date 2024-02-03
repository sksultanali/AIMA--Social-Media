package com.developerali.aima.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developerali.aima.Models.CommentModel;
import com.developerali.aima.Models.UserModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ItemCommentBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.viewHolder>{

    ArrayList<CommentModel> models;
    Context context;
    FirebaseDatabase database;
    Activity activity;

    public CommentAdapter(Context context, Activity activity, ArrayList<CommentModel> list){
        this.models = list;
        this.activity = activity;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        CommentModel commentModel = models.get(position);
        database = FirebaseDatabase.getInstance();
        database.getReference().child("users").child(commentModel.getCommentedBy())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            UserModel userModel = snapshot.getValue(UserModel.class);
                            if (userModel.getImage() != null && !activity.isDestroyed()){
                                Glide.with(context)
                                                .load(userModel.getImage())
                                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                                .placeholder(context.getDrawable(R.drawable.profileplaceholder))
                                        .into(holder.binding.CommentsProfileImage);
                            }
                            holder.binding.commentProfileName.setText(userModel.getName());
                            if (userModel.isVerified()){
                                holder.binding.verifiedProfile.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        if (commentModel.getComment().length() > 300){
            holder.binding.actualComment.setText(commentModel.getComment().substring(0, 135) + "...Read more");
        }else {
            holder.binding.actualComment.setText(commentModel.getComment());
        }


        holder.itemView.setOnClickListener(v->{

        });



    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        ItemCommentBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemCommentBinding.bind(itemView);
        }
    }

}

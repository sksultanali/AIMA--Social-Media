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
import com.developerali.aima.Activities.See_Post;
import com.developerali.aima.Models.PostModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.SingleAdminPostBinding;
import com.github.marlonlom.utilities.timeago.TimeAgo;

import java.util.ArrayList;

public class AdminActivityAdapter extends RecyclerView.Adapter<AdminActivityAdapter.ViewHolder>{

    Activity context;
    ArrayList<PostModel> models;

    public AdminActivityAdapter(Activity context, ArrayList<PostModel> models) {
        this.context = context;
        this.models = models;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_admin_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        PostModel postModel = models.get(position);

        if (postModel.getCaption() != null){
            if (postModel.getCaption().length() > 150){
                holder.binding.discoverCaption
                        .setText(postModel.getCaption().substring(0, 135) + "...Read more");
            }else {
                holder.binding.discoverCaption.setText(postModel.getCaption());
            }
        }else {
            holder.binding.discoverCaption.setText("No Caption For This Activity !");
        }

        if (postModel.getImage() != null){
            Glide.with(context)
                    .load(postModel.getImage())
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(holder.binding.discover);
        }else {
            holder.binding.discover.setVisibility(View.GONE);
        }

        String timeAgo = TimeAgo.using(postModel.getTime());
        holder.binding.adminName.setText("Admin Activity • " + timeAgo + " •");
        holder.binding.likeTextCount.setText(postModel.getLikesCount() + " likes • " + postModel.getCommentsCount() + " comments");

        holder.binding.discoverClose.setOnClickListener(c->{
            //removing from list onBindViewHolder
            models.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, models.size());
            notifyDataSetChanged();
        });

        holder.itemView.setOnClickListener(v->{
            Intent i = new Intent(context.getApplicationContext(), See_Post.class);
            i.putExtra("postId", postModel.getId());
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        SingleAdminPostBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = SingleAdminPostBinding.bind(itemView);
        }
    }
}

package com.developerali.aima.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developerali.aima.Activities.ImageShow;
import com.developerali.aima.Models.GalleryModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ItemPostProfileBinding;

import java.util.ArrayList;

public class GallActAdapter extends RecyclerView.Adapter<GallActAdapter.ViewHolder>{
    Activity activity;
    ArrayList<String> images;

    public GallActAdapter(Activity activity, ArrayList<String> images) {
        this.activity = activity;
        this.images = images;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_post_profile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String image = images.get(position);

        if (image != null && !activity.isDestroyed()){
            Glide.with(activity)
                    .load(image)
                    .skipMemoryCache(true)
                    .placeholder(activity.getDrawable(R.drawable.placeholder))
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(holder.binding.imageView1);

            holder.itemView.setOnClickListener(c->{
                Intent i = new Intent(activity.getApplicationContext(), ImageShow.class);
                i.putExtra("image", image);
                i.setFlags(i.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(i);
            });
        }

    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ItemPostProfileBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemPostProfileBinding.bind(itemView);
        }
    }
}

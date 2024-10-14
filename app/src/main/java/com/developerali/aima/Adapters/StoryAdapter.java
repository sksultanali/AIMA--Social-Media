package com.developerali.aima.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.developerali.aima.Models.StoryModel;
import com.developerali.aima.R;

import java.util.ArrayList;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.viewHolder>{

    ArrayList<StoryModel> list;
    Activity context;

    public StoryAdapter(ArrayList<StoryModel> list, Activity context){
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_stories, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        StoryModel model = list.get(position);
        holder.storyImage.setImageResource(model.getStory());
        holder.profile.setImageResource(model.getProfile());
        holder.name.setText(model.getName());


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder{
        ImageView storyImage, profile, storyType;
        TextView name;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            storyImage = itemView.findViewById(R.id.story);
            profile = itemView.findViewById(R.id.storyProfileImage);
            storyType = itemView.findViewById(R.id.storyDots);
            name = itemView.findViewById(R.id.storyProfileName);

        }
    }


}

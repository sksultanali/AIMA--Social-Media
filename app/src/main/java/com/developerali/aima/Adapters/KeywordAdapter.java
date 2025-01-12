package com.developerali.aima.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.developerali.aima.Activities.SearchResults;
import com.developerali.aima.Helpers.DB_Helper;
import com.developerali.aima.Model_Apis.KeywordResponse;
import com.developerali.aima.Models.RecentSearchModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ChildSearchKeywordBinding;

import java.util.List;

public class KeywordAdapter extends RecyclerView.Adapter<KeywordAdapter.ViewHolder>{

    Activity activity;
    List<KeywordResponse.Data> models;
    private DB_Helper dbHelper;

    public KeywordAdapter(Activity activity, List<KeywordResponse.Data> models) {
        this.activity = activity;
        this.models = models;
        dbHelper = new DB_Helper(activity);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.child_search_keyword, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        KeywordResponse.Data keywordData = models.get(position);
        holder.binding.name.setText(keywordData.getMatchKeyword());
        holder.binding.typeText.setText(keywordData.getType());
        if (keywordData.getType().equalsIgnoreCase("people")){
            holder.binding.icon.setImageDrawable(activity.getDrawable(R.drawable.user_keyword));
        } else if (keywordData.getType().equalsIgnoreCase("post")) {
            holder.binding.icon.setImageDrawable(activity.getDrawable(R.drawable.post_keyword));
        }else {
            holder.binding.icon.setImageDrawable(activity.getDrawable(R.drawable.video_keyword));
        }

        holder.itemView.setOnClickListener(v->{
            dbHelper.addSearchQuery(new RecentSearchModel(keywordData.getMatchKeyword(), keywordData.getType()));
            Intent i = new Intent(activity.getApplicationContext(), SearchResults.class);
            i.putExtra("type", keywordData.getType());
            i.putExtra("keyword", keywordData.getMatchKeyword());
            activity.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ChildSearchKeywordBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ChildSearchKeywordBinding.bind(itemView);
        }
    }
}

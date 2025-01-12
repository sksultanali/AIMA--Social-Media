package com.developerali.aima.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.developerali.aima.Activities.SearchResults;
import com.developerali.aima.Helpers.DB_Helper;
import com.developerali.aima.Models.RecentSearchModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ChildSearchKeywordBinding;
import com.developerali.aima.databinding.LayoutSerachHistoryBinding;

import java.util.ArrayList;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder>{

    ArrayList<RecentSearchModel> arrayList;
    Activity activity;
    private final DB_Helper dbHelper;

    public SearchAdapter(ArrayList<RecentSearchModel> arrayList, Activity activity) {
        this.arrayList = arrayList;
        this.activity = activity;
        dbHelper = new DB_Helper(activity);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.child_search_keyword, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecentSearchModel recentSearchModel = arrayList.get(position);
        holder.binding.name.setText(recentSearchModel.getSearch_query());
        holder.binding.typeText.setText(recentSearchModel.getType());

        holder.binding.icon.setImageDrawable(activity.getDrawable(R.drawable.history_24));
        holder.binding.iconSearch.setImageDrawable(activity.getDrawable(R.drawable.close_24));

        holder.binding.iconSearch.setOnClickListener(v->{
            dbHelper.deleteSearchQuery(recentSearchModel.getSearch_query());
            try {
                arrayList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, arrayList.size());
            }catch (Exception e){

            }
        });

        holder.itemView.setOnClickListener(v->{
            Intent i = new Intent(activity.getApplicationContext(), SearchResults.class);
            i.putExtra("type", recentSearchModel.getType());
            i.putExtra("keyword", recentSearchModel.getSearch_query());
            activity.startActivity(i);
        });


    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public interface SelectedItem {
        void onShowAction(RecentSearchModel searchModel);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ChildSearchKeywordBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ChildSearchKeywordBinding.bind(itemView);
        }
    }
}

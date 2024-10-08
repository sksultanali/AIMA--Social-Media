package com.developerali.aima.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.developerali.aima.DB_Helper;
import com.developerali.aima.Models.RecentSearchModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.LayoutSerachHistoryBinding;

import java.util.ArrayList;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder>{

    ArrayList<RecentSearchModel> arrayList;
    Activity activity;
    SelectedItem selectedItem;
    private DB_Helper dbHelper;

    public SearchAdapter(ArrayList<RecentSearchModel> arrayList, Activity activity, SelectedItem selectedItem) {
        this.arrayList = arrayList;
        this.activity = activity;
        this.selectedItem = selectedItem;
        dbHelper = new DB_Helper(activity);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.layout_serach_history, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecentSearchModel recentSearchModel = arrayList.get(position);
        holder.binding.searchText.setText(recentSearchModel.getSearch_query());
        holder.binding.searchText.setOnClickListener(v->{
            selectedItem.onShowAction(recentSearchModel);
        });

        holder.binding.closeBtn.setOnClickListener(v->{
            dbHelper.deleteSearchQuery(recentSearchModel.getSearch_query());
            arrayList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, arrayList.size());
        });


    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public interface SelectedItem {
        void onShowAction(RecentSearchModel searchModel);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        LayoutSerachHistoryBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = LayoutSerachHistoryBinding.bind(itemView);
        }
    }
}

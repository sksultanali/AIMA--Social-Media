package com.developerali.aima.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.developerali.aima.Models.UsagesModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ItemUsagesAppBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class UsagesAdapter extends RecyclerView.Adapter<UsagesAdapter.ViewHolder>{

    Context context;
    ArrayList<UsagesModel> usagesModels;

    public UsagesAdapter(Context context, ArrayList<UsagesModel> usagesModels) {
        this.context = context;
        this.usagesModels = usagesModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_usages_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UsagesModel usagesModel = usagesModels.get(position);

        holder.binding.actText.setText(usagesModel.getActivity());

        SimpleDateFormat time = new SimpleDateFormat("h:mm:ss a");
        SimpleDateFormat date = new SimpleDateFormat("dd MMM yyyy");

        String startTime = time.format(usagesModel.getStartTime());
        String endTime = time.format(usagesModel.getEndTime());
        String acDate = date.format(usagesModel.getStartTime());

        holder.binding.startEnd.setText(startTime + " ~ " + endTime);
        holder.binding.date.setText(acDate);

        long dif = usagesModel.getEndTime() - usagesModel.getStartTime();
        long sec = dif / 1000;
        long min = sec / 60;
        sec %= 60; // Calculate remaining seconds

        holder.binding.actualTime.setText(min + " min " + sec + " sec");

        try {
            String preDate = date.format(usagesModels.get(position - 1).getStartTime());
            if (acDate.equalsIgnoreCase(preDate)){
                holder.binding.date.setVisibility(View.GONE);
            }
        }catch (Exception e){

        }






    }

    @Override
    public int getItemCount() {
        return usagesModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ItemUsagesAppBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemUsagesAppBinding.bind(itemView);
        }
    }
}

package com.developerali.aima.Adapters;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import com.developerali.aima.Activities.WebViewActivity;
import com.developerali.aima.Helper;
import com.developerali.aima.R;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.developerali.aima.Models.pdfModel;
import com.developerali.aima.databinding.ItemPdfsBinding;
import com.google.rpc.Help;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class pdfAdapter extends RecyclerView.Adapter<pdfAdapter.ViewHolder>{

    Activity activity;
    ArrayList<pdfModel> pdfModels;

    public pdfAdapter(Activity activity, ArrayList<pdfModel> pdfModels) {
        this.activity = activity;
        this.pdfModels = pdfModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_pdfs, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        pdfModel pdf = pdfModels.get(position);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy");
        String date = simpleDateFormat.format(pdf.getTime());

        holder.binding.pdfCaption.setText(pdf.getCaption());
        holder.binding.addedOn.setText("Added on " + date );

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Helper.isChromeCustomTabsSupported(activity)){
                    Helper.openChromeTab(pdf.getLink(), activity);
                }else {
                    Intent i = new Intent(activity.getApplicationContext(), WebViewActivity.class);
                    i.putExtra("provide", pdf.getLink());
                    i.setFlags(i.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(i);
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return pdfModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemPdfsBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemPdfsBinding.bind(itemView);
        }
    }

}

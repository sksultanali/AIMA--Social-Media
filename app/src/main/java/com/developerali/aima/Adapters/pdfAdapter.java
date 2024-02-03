package com.developerali.aima.Adapters;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import com.developerali.aima.Activities.WebViewActivity;
import com.developerali.aima.R;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.developerali.aima.Models.pdfModel;
import com.developerali.aima.databinding.ItemPdfsBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class pdfAdapter extends RecyclerView.Adapter<pdfAdapter.ViewHolder>{

    Context context;
    ArrayList<pdfModel> pdfModels;

    public pdfAdapter(Context context, ArrayList<pdfModel> pdfModels) {
        this.context = context;
        this.pdfModels = pdfModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pdfs, parent, false);
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
//                Intent i = new Intent(context.getApplicationContext(), PdfSeeActivity.class);
//                i.putExtra("pdfModel", pdf);
//                i.setFlags(i.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(i);

                Intent i = new Intent(context.getApplicationContext(), WebViewActivity.class);
                i.putExtra("provide", pdf.getLink());
                i.setFlags(i.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
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

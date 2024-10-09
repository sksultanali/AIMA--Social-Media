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
import com.developerali.aima.Activities.GalleryActivity;
import com.developerali.aima.Activities.ImageShow;
import com.developerali.aima.Models.GalleryModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ItemGalleryBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder>{

    Activity context;
    ArrayList<GalleryModel> galleryModels;

    public GalleryAdapter(Activity activity, ArrayList<GalleryModel> galleryModels) {
        this.context = activity;
        this.galleryModels = galleryModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_gallery, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GalleryModel galleryModel = galleryModels.get(position);

        if (galleryModel.getCaption() != null && galleryModel.getCaption().length()!=0){
            if (galleryModel.getCaption().length() > 150){
                String text = galleryModel.getCaption().substring(0, 135);
                if (text.contains("\n")) {
                    int length = text.length(); int enterCount = 0; int enterIndex = -1;
                    for (int i = 0; i < length; i++) {
                        if (text.charAt(i) == '\n') {
                            enterCount++;
                            enterIndex = i - 1;

                            holder.binding.galleryCaption.setText( text.substring(0, enterIndex) + "...Read more");
                        }
                    }
                }else {
                    holder.binding.galleryCaption.setText( text + "...Read more");
                }
            }else {
                holder.binding.galleryCaption.setText(galleryModel.getCaption());
            }
        }else {
            holder.binding.galleryCaption.setVisibility(View.GONE);
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy");
        String date = simpleDateFormat.format(galleryModel.getTime());
        holder.binding.addedOnDate.setText("Added On " + date);


        if (galleryModel.getImages() != null){
            if (galleryModel.getImages().get(0) != null && !context.isDestroyed()){
                Glide.with(context)
                        .load(galleryModel.getImages().get(0))
                        .skipMemoryCache(true)
                        .placeholder(context.getDrawable(R.drawable.placeholder))
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(holder.binding.imageView1);

                holder.binding.imageView1.setOnClickListener(c->{
                    Intent i = new Intent(context.getApplicationContext(), ImageShow.class);
                    i.putExtra("image", galleryModel.getImages().get(0));
                    context.startActivity(i);
                });
            }
            if (galleryModel.getImages().get(1) != null && !context.isDestroyed()){
                Glide.with(context)
                        .load(galleryModel.getImages().get(1))
                        .skipMemoryCache(true)
                        .placeholder(context.getDrawable(R.drawable.placeholder))
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(holder.binding.imageView2);

                holder.binding.imageView2.setOnClickListener(c->{
                    Intent i = new Intent(context.getApplicationContext(), ImageShow.class);
                    i.putExtra("image", galleryModel.getImages().get(1));
                    context.startActivity(i);
                });
            }else {
                holder.binding.imageView2.setVisibility(View.GONE);
            }
            if (galleryModel.getImages().get(2) != null && !context.isDestroyed()){
                Glide.with(context)
                        .load(galleryModel.getImages().get(2))
                        .skipMemoryCache(true)
                        .placeholder(context.getDrawable(R.drawable.placeholder))
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(holder.binding.imageView3);

                holder.binding.imageView3.setOnClickListener(c->{
                    Intent i = new Intent(context.getApplicationContext(), ImageShow.class);
                    i.putExtra("image", galleryModel.getImages().get(2));
                    context.startActivity(i);
                });
            }else {
                holder.binding.imageView3.setVisibility(View.GONE);
            }
        }


        holder.binding.seeAll.setOnClickListener(v->{
            Intent i = new Intent(context.getApplicationContext(), GalleryActivity.class);
            i.putExtra("galleryId", galleryModel.getId());
            context.startActivity(i);
        });

        holder.itemView.setOnClickListener(v->{
            Intent i = new Intent(context.getApplicationContext(), GalleryActivity.class);
            i.putExtra("galleryId", galleryModel.getId());
            context.startActivity(i);
        });


    }

    @Override
    public int getItemCount() {
        return galleryModels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        ItemGalleryBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemGalleryBinding.bind(itemView);
        }
    }
}

package com.developerali.aima.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developerali.aima.Model_Apis.MapPointerResponse;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ChildMapLayoutBinding;

import java.util.List;

public class DialogMapSearchAdapter extends RecyclerView.Adapter<DialogMapSearchAdapter.ViewHolder>{

    Activity activity;
    List<MapPointerResponse.Data> data;

    public DialogMapSearchAdapter(Activity activity, List<MapPointerResponse.Data> data) {
        this.activity = activity;
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.child_map_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MapPointerResponse.Data pointerResponse = data.get(position);

        holder.binding.propName.setText(pointerResponse.getName());
        holder.binding.des.setText(pointerResponse.getDescription());
        holder.binding.tag.setText(pointerResponse.getTags());

        if (pointerResponse.getImage() != null && !pointerResponse.getImage().isEmpty()){
            Glide.with(activity)
                    .load(pointerResponse.getImage())
                    .skipMemoryCache(false)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .placeholder(activity.getDrawable(R.drawable.placeholder))
                    .into(holder.binding.imageView5);
        }

        holder.binding.start.setOnClickListener(v->{
            String encodedLabel = Uri.encode(pointerResponse.getName());
            Uri geoLocation = Uri.parse("geo:" + pointerResponse.getLatitude() + "," + pointerResponse.getLongitude() + "?q=" +
                    pointerResponse.getLatitude() + "," + pointerResponse.getLongitude() + "(" + encodedLabel + ")");
            Intent intent = new Intent(Intent.ACTION_VIEW, geoLocation);
            Intent chooser = Intent.createChooser(intent, "Open with");
            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                activity.startActivity(chooser);
            } else {
                Toast.makeText(activity, "No app available to open the map.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ChildMapLayoutBinding binding;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ChildMapLayoutBinding.bind(itemView);
        }
    }
}

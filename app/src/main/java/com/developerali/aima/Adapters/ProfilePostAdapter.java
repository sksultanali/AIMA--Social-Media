package com.developerali.aima.Adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developerali.aima.Activities.See_Post;
import com.developerali.aima.Models.PostModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ItemPostProfileBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ProfilePostAdapter extends RecyclerView.Adapter<ProfilePostAdapter.viewHolder>{

    ArrayList<PostModel> postModels;
    Activity activity;

    public ProfilePostAdapter(ArrayList<PostModel> postModels, Activity activity) {
        this.postModels = postModels;
        this.activity = activity;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_post_profile, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, @SuppressLint("RecyclerView") int position) {
        PostModel postModel = postModels.get(position);
        if (postModel.getImage() != null && !activity.isDestroyed()){
            Glide.with(activity.getApplicationContext())
                    .load(postModel.getImage())
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .placeholder(activity.getDrawable(R.drawable.placeholder))
                    .into(holder.binding.imageView1);
        }else {
            holder.binding.imageView1.setImageDrawable(activity.getDrawable(R.drawable.text));
        }

        holder.itemView.setOnClickListener(v->{
            Intent i = new Intent(activity.getApplicationContext(), See_Post.class);
            i.putExtra("postId", postModel.getId());
            activity.startActivity(i);
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (postModel.getUploader().equalsIgnoreCase(FirebaseAuth.getInstance().getCurrentUser().getUid())){


                    PopupMenu popupMenu = new PopupMenu(activity.getApplicationContext(), holder.binding.imageView1);
                    popupMenu.getMenu().add("Delete");
                    popupMenu.show();

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {

                            holder.binding.progressCircular.setVisibility(View.VISIBLE);
                            FirebaseFirestore.getInstance().collection("post")
                                    .document(postModel.getId())
                                    .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            FirebaseDatabase.getInstance().getReference().child("likes")
                                                    .child(postModel.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            FirebaseDatabase.getInstance().getReference().child("comments")
                                                                    .child(postModel.getId()).removeValue()
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void unused) {
                                                                            holder.binding.progressCircular.setVisibility(View.GONE);
                                                                            //removing from list onBindViewHolder
                                                                            postModels.remove(position);
                                                                            notifyItemRemoved(position);
                                                                            notifyItemRangeChanged(position, postModels.size());
                                                                            notifyDataSetChanged();
                                                                        }
                                                                    });
                                                        }
                                                    });
                                        }
                                    });

                            return false;
                        }
                    });
                }

                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return postModels.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder{
        ItemPostProfileBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemPostProfileBinding.bind(itemView);
        }
    }
}

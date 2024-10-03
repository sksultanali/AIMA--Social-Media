package com.developerali.aima.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developerali.aima.Activities.Comments_Post;
import com.developerali.aima.Activities.ProfileActivity;
import com.developerali.aima.Activities.See_Post;
import com.developerali.aima.Models.NotificationModel;
import com.developerali.aima.Models.PostModel;
import com.developerali.aima.Models.UserModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ItemSinglePostBinding;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.like.LikeButton;
import com.like.OnLikeListener;

import java.util.ArrayList;
import java.util.Date;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.viewHolder>{

    Context context;
    ArrayList<PostModel> models;

    Activity activity;
    FirebaseDatabase database;
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;

    public PostAdapter(ArrayList<PostModel> list, Activity activity){
        this.models = list;
        this.context = activity.getApplicationContext();
        this.activity = activity;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_single_post, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        PostModel postModel = models.get(position);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();


        holder.binding.commentText.setText(postModel.getCommentsCount()+"");
        holder.binding.likeTextCount.setText(postModel.getLikesCount()+"");

        database.getReference().child("users").child(postModel.getUploader())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            UserModel userModel = snapshot.getValue(UserModel.class);
                            if (userModel.isVerified()){
                                holder.binding.verifiedProfile.setVisibility(View.VISIBLE);
                            }else {
                                holder.binding.verifiedProfile.setVisibility(View.GONE);
                            }
                            String timeAgo = TimeAgo.using(postModel.getTime());
                            holder.binding.discoverProfileName.setText(userModel.getName());
                            if (userModel.getType() != null){
                                holder.binding.discoverProfile.setText(userModel.getType() + " • " + timeAgo + " •");
                            }else {
                                holder.binding.discoverProfile.setText("Public Profile" + " • " + timeAgo + " •");
                            }
                            if (userModel.getImage() != null && !activity.isDestroyed()){
                                Glide.with(context)
                                        .load(userModel.getImage())
                                        .placeholder(context.getDrawable(R.drawable.profileplaceholder))
                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                        .into(holder.binding.discoverProfileImage);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        if (postModel.getUploader().equalsIgnoreCase("admin")){
            String timeAgo = TimeAgo.using(postModel.getTime());
            holder.binding.discoverProfileName.setText("Admin Post");
            holder.binding.verifiedProfile.setVisibility(View.VISIBLE);
            holder.binding.discoverProfile.setText("App Admin" + " • " + timeAgo + " •");
            holder.binding.discoverProfileImage.setImageDrawable(context.getDrawable(R.drawable.aimalogo));
        }


        if (postModel.getCaption() != null && postModel.getCaption().length()!=0){
            if (postModel.getCaption().length() > 150){
                String text = postModel.getCaption().substring(0, 135);
                if (text.contains("\n")) {
                    int length = text.length(); int enterCount = 0; int enterIndex = -1;
                    for (int i = 0; i < length; i++) {
                        if (text.charAt(i) == '\n') {
                            enterCount++;
                            enterIndex = i - 1;

                            holder.binding.discoverCaption.setText( text.substring(0, enterIndex) + "...Read more");
                        }
                    }
                }else {
                    holder.binding.discoverCaption.setText( text + "...Read more");
                }
            }else {
                holder.binding.discoverCaption.setText(postModel.getCaption());
            }
        }else {
            holder.binding.discoverCaption.setVisibility(View.GONE);
        }


        if (postModel.getImage() != null && !activity.isDestroyed()){

            Glide.with(context)
                    .load(postModel.getImage())
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(holder.binding.discover);
        }else {
            holder.binding.discover.setVisibility(View.GONE);
        }

        holder.binding.discoverClose.setOnClickListener(c->{
            //removing from list onBindViewHolder
            models.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, models.size());
            notifyDataSetChanged();
        });

        holder.binding.profClick.setOnClickListener(v->{
            if (postModel.getUploader() != null && !postModel.getUploader().equalsIgnoreCase("admin")){
                Intent i = new Intent(context.getApplicationContext(), ProfileActivity.class);
                i.putExtra("profileId", postModel.getUploader());
                i.setFlags(i.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }else {
                Toast.makeText(context, "not possible...", Toast.LENGTH_SHORT).show();
            }
        });

        holder.binding.discoverDots.setOnClickListener(c->{
            PopupMenu popupMenu = new PopupMenu(context.getApplicationContext(), holder.binding.discoverDots);
            popupMenu.getMenu().add("Share");

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Toast.makeText(context, "loading request...", Toast.LENGTH_SHORT).show();
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    sharingIntent.setType("text/html");
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                            "This post is shared from AIMA App. Check using app only ! \n\n" +
                                    " link: https://i.aima.post/" + postModel.getId());
                    if (sharingIntent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(Intent.createChooser(sharingIntent,"Share using"));
                    }
                    return true;
                }
            });

            popupMenu.show();
        });

        database.getReference().child("likes")
                        .child(postModel.getId())
                                .child(auth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            boolean like = snapshot.getValue(Boolean.class);
                            if (like){
                                holder.binding.discoverLike.setLiked(true);
                                holder.binding.likeText.setText("Liked");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        holder.binding.like01.setOnClickListener(c->{
            holder.binding.discoverLike.performClick();
        });

        holder.binding.discoverLike.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                holder.binding.discoverLike.setEnabled(false);
                int k = Integer.parseInt(holder.binding.likeTextCount.getText().toString()) + 1;
                holder.binding.likeTextCount.setText(k+"");
                database.getReference().child("likes")
                        .child(postModel.getId())
                        .child(auth.getCurrentUser().getUid())
                        .setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                firebaseFirestore.collection("post")
                                        .document(postModel.getId())
                                        .update("likesCount", FieldValue.increment(1))
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        holder.binding.likeText.setText("liked");
                                                        holder.binding.discoverLike.setEnabled(true);

                                                        NotificationModel notificationModel = new NotificationModel(auth.getCurrentUser().getUid(),
                                                                "like", postModel.getId(), new Date().getTime(), false);
                                                        database.getReference().child("notification")
                                                                .child(postModel.getUploader())
                                                                .push()
                                                                .setValue(notificationModel);
                                                    }
                                                });

                            }
                        });
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                holder.binding.discoverLike.setEnabled(false);
                int k = Integer.parseInt(holder.binding.likeTextCount.getText().toString()) - 1;
                holder.binding.likeTextCount.setText(k+"");
                database.getReference().child("likes")
                        .child(postModel.getId())
                        .child(auth.getCurrentUser().getUid())
                        .removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                firebaseFirestore.collection("post")
                                        .document(postModel.getId())
                                        .update("likesCount", FieldValue.increment(-1));

                                holder.binding.likeText.setText("like");
                                holder.binding.discoverLike.setEnabled(true);
                            }
                        });
            }
        });

        holder.binding.share01.setOnClickListener(c->{
            Toast.makeText(context, "loading request...", Toast.LENGTH_SHORT).show();
            holder.binding.discoverShare.performClick();
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/html");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                    "This post is shared from AIMA App. Check using app only ! \n\n" +
                            " link: https://i.aima.post/" + postModel.getId());
            if (sharingIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(Intent.createChooser(sharingIntent,"Share using"));
            }

        });























        

        holder.binding.discoverComment.setOnClickListener(c->{
            Intent i = new Intent(context.getApplicationContext(), Comments_Post.class);
            i.putExtra("postId", postModel.getId());
            i.putExtra("uploaderId", postModel.getUploader());
            i.setFlags(i.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        });

        holder.itemView.setOnClickListener(v->{
            Intent i = new Intent(context.getApplicationContext(), See_Post.class);
            i.putExtra("postId", postModel.getId());
            i.setFlags(i.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        });












        //Double click checker!

//        holder.binding.discoverLike.setOnClickListener(new DoubleClickHandler(new DoubleClickListener() {
//            @Override
//            public void onDoubleClick(View view) {
//                Toast.makeText(context, "double clicked", Toast.LENGTH_SHORT).show();
//            }
//        }));
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder{
        ItemSinglePostBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSinglePostBinding.bind(itemView);
        }
    }
}

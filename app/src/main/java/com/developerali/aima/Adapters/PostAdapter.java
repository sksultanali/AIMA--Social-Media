package com.developerali.aima.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developerali.aima.Activities.Comments_Post;
import com.developerali.aima.Activities.ProfileActivity;
import com.developerali.aima.Activities.See_Post;
import com.developerali.aima.Helpers.Helper;
import com.developerali.aima.Helpers.TextUtils;
import com.developerali.aima.Helpers.UserDataUpdate;
import com.developerali.aima.MainActivity;
import com.developerali.aima.Model_Apis.ApiResponse;
import com.developerali.aima.Model_Apis.ApiService;
import com.developerali.aima.Model_Apis.PostResponse;
import com.developerali.aima.Model_Apis.RetrofitClient;
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
import com.google.rpc.Help;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.makeramen.roundedimageview.RoundedImageView;
import com.qkopy.richlink.ViewListener;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.viewHolder>{


    ArrayList<PostResponse.PostData> models;
    Activity activity;
    FirebaseDatabase database;
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;
    private HashMap<String, Bitmap> imageCache;
    ApiService apiService;
    int maxLength = 150;
    boolean myPost;
    Animation animation;

    public PostAdapter(ArrayList<PostResponse.PostData> list, Activity activity, boolean myPost){
        this.models = list;
        this.activity = activity;
        this.myPost = myPost;
        this.imageCache = new HashMap<>();
        animation = AnimationUtils.loadAnimation(activity, R.anim.blink);
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        apiService = RetrofitClient.getClient().create(ApiService.class);
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        ItemSinglePostBinding binding = ItemSinglePostBinding.inflate(inflater, parent, false);
        return new viewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        PostResponse.PostData postModel = models.get(position);

        List<String> links = TextUtils.extractLinks(postModel.getCaption());
        if (!links.isEmpty()){
            if (postModel.getImage() == null || postModel.getImage().isEmpty()){
                holder.binding.richLink.setLink(links.get(0), activity, new ViewListener() {
                    @Override
                    public void onSuccess(boolean b) {
                        holder.binding.richLink.setVisibility(View.VISIBLE);
                        holder.binding.discover.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(@NonNull Exception e) {
                        holder.binding.richLink.setVisibility(View.GONE);
                        holder.binding.discover.setImageDrawable(activity.getDrawable(R.drawable.link_broken));
                        holder.binding.discover.setVisibility(View.VISIBLE);
                    }
                });
            }
        }else {
            holder.binding.richLink.setVisibility(View.GONE);
        }

        if (postModel.getImage() != null && !postModel.getImage().isEmpty() && !activity.isDestroyed()){
            holder.binding.discover.setVisibility(View.VISIBLE);
            Glide.with(activity)
                    .load(postModel.getImage())
                    .placeholder(R.drawable.placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .skipMemoryCache(false)
                    .into(holder.binding.discover);
        }

        holder.binding.commentText.setText(String.valueOf(postModel.getCommentsCount()));
        holder.binding.likeTextCount.setText(String.valueOf(postModel.getLikesCount()));


        long time = Helper.convertToLongTime(postModel.getTime());
        String timeAgo = (time == -1) ? Helper.formatDate("yyyy-MM-dd HH:mm:ss",
                "dd LLL yyyy", postModel.getTime()) : TimeAgo.using(time);

        if (myPost){

            holder.binding.discoverClose.setVisibility(View.GONE);
            holder.binding.discoverDots.setVisibility(View.VISIBLE);

            if (Helper.userDetails != null){
                postModel.setUploader(Helper.userDetails.getUserId());
                holder.binding.discoverProfileName.setText(Helper.userDetails.getName());
                holder.binding.discoverProfile.setText(postModel.getStatus() + " • " + timeAgo + " •");
                if (postModel.getStatus().equalsIgnoreCase("Approved")){
                    holder.binding.discoverProfile.setTextColor(activity.getColor(R.color.green_colour));
                } else if (postModel.getStatus().equalsIgnoreCase("Pending Approval")) {
                    holder.binding.discoverProfile.setTextColor(activity.getColor(R.color.backgroundBottomColour));
                    holder.binding.discoverProfile.setAnimation(animation);
                }else {
                    holder.binding.discoverProfile.setTextColor(activity.getColor(R.color.red_colour));
                }
                Helper.showBadge(postModel.getVerified(), postModel.getVerified_valid(), holder.binding.verifiedProfile);
                if (Helper.userDetails.getImage() != null && !activity.isDestroyed()){
                    Glide.with(activity.getApplicationContext())
                            .load(Helper.userDetails.getImage())
                            .placeholder(activity.getDrawable(R.drawable.profileplaceholder))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .override(50, 50)
                            .priority(Priority.HIGH)
                            .into(holder.binding.discoverProfileImage);
                }
            }

        } else if (postModel.getUploader().equalsIgnoreCase("admin")){
            holder.binding.discoverProfileName.setText("Admin Post");
            holder.binding.verifiedProfile.setVisibility(View.VISIBLE);
            holder.binding.discoverProfile.setText("App Admin" + " • " + timeAgo + " •");
            holder.binding.discoverProfileImage.setImageDrawable(activity.getDrawable(R.drawable.aimalogo));
        }else {
            holder.binding.discoverProfileName.setText(postModel.getName());
            if (postModel.getType() != null){
                holder.binding.discoverProfile.setText(postModel.getType() + " • " + timeAgo + " •");
            }else {
                holder.binding.discoverProfile.setText("Public Profile" + " • " + timeAgo + " •");
            }
            Helper.showBadge(postModel.getVerified(), postModel.getVerified_valid(), holder.binding.verifiedProfile);
            if (postModel.getUser_image() != null && !activity.isDestroyed()){
                Glide.with(activity.getApplicationContext())
                        .load(postModel.getUser_image())
                        .placeholder(activity.getDrawable(R.drawable.profileplaceholder))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .override(50, 50)
                        .priority(Priority.HIGH)
                        .into(holder.binding.discoverProfileImage);
            }
        }


        if (postModel.getCaption() != null && !postModel.getCaption().isEmpty()) {
            String fullCaption = postModel.getCaption();
            String displayText;
            if (fullCaption.length() > maxLength) {
                String truncatedText = fullCaption.substring(0, maxLength);
                int lastNewLineIndex = truncatedText.lastIndexOf('\n');
                if (lastNewLineIndex != -1) {
                    displayText = truncatedText.substring(0, lastNewLineIndex) + "... Read more";
                } else {
                    displayText = truncatedText + "... Read more";
                }
            } else {
                displayText = fullCaption;
            }

            SpannableString spannableString = TextUtils.applySpannable(activity, displayText, fullCaption, holder.binding.discoverCaption);
            holder.binding.discoverCaption.setText(spannableString);
            holder.binding.discoverCaption.setMovementMethod(LinkMovementMethod.getInstance());
            holder.binding.discoverCaption.setHighlightColor(Color.TRANSPARENT);
        } else {
            holder.binding.discoverCaption.setVisibility(View.GONE);
        }



//        String imageUrl = postModel.getImage();
//        if (imageCache.containsKey(imageUrl)) {
//            holder.binding.discover.setImageBitmap(imageCache.get(imageUrl));
//        } else {
//            holder.binding.discover.setImageResource(R.drawable.placeholder); // Placeholder image
//            new ImageLoaderTask(holder.binding.discover, imageUrl).execute();
//        }

        holder.binding.discoverClose.setOnClickListener(c->{
            models.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, models.size());
            notifyDataSetChanged();
        });
        
        holder.binding.discoverDots.setOnClickListener(c->{
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Select an Action")
                    .setItems(new CharSequence[]{"Unpublish", "Delete Post", "Republish"}, (dialog, which) -> {
                        switch (which) {
                            case 0: // Copy Profile Link
                                Call<ApiResponse> call = apiService.updatePostField(
                                        "updatePostField", postModel.getId(), "status", "Unpublish"
                                );
                                call.enqueue(new Callback<ApiResponse>() {
                                    @Override
                                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                                        if (response.isSuccessful() && response.body() != null){
                                            ApiResponse apiResponse = response.body();
                                            if (apiResponse.getStatus().equalsIgnoreCase("success")){
                                                holder.binding.discoverProfile.setText("Unpublished");
                                            }else {
                                                Toast.makeText(activity, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                                        Toast.makeText(activity, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;
                            case 1: // Log Out
                                Call<ApiResponse> call2 = apiService.updatePostField(
                                        "updatePostField", postModel.getId(), "status", "Restricted"
                                );
                                call2.enqueue(new Callback<ApiResponse>() {
                                    @Override
                                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                                        if (response.isSuccessful() && response.body() != null){
                                            ApiResponse apiResponse = response.body();
                                            if (apiResponse.getStatus().equalsIgnoreCase("success")){
                                                Helper.showAlertNoAction(activity,
                                                        "Request Submitted", "Your post will delete automatically after few hours.",
                                                        "Okay");
                                            }else {
                                                Toast.makeText(activity, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                                        Toast.makeText(activity, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;
                            case 2: // Copy Profile Link
                                Call<ApiResponse> call3 = apiService.updatePostField(
                                        "updatePostField", postModel.getId(), "status", "Pending Approval"
                                );
                                call3.enqueue(new Callback<ApiResponse>() {
                                    @Override
                                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                                        if (response.isSuccessful() && response.body() != null){
                                            ApiResponse apiResponse = response.body();
                                            if (apiResponse.getStatus().equalsIgnoreCase("success")){
                                                holder.binding.discoverProfile.setText("Re_Published");
                                            }else {
                                                Toast.makeText(activity, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                                        Toast.makeText(activity, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            notifyDataSetChanged();
        });

        holder.binding.profClick.setOnClickListener(v->{
            if (postModel.getUploader() != null && !postModel.getUploader().equalsIgnoreCase("admin")){
                Intent i = new Intent(activity.getApplicationContext(), ProfileActivity.class);
                i.putExtra("profileId", postModel.getUploader());
                activity.startActivity(i);
            }else {
                Helper.showAlertNoAction(activity, "Admin Profile",
                        "This profile can't be check by you. Have a good day!", "Thank You");
            }
        });

//        holder.binding.discoverDots.setOnClickListener(c->{
//            PopupMenu popupMenu = new PopupMenu(activity.getApplicationContext(), holder.binding.discoverDots);
//            popupMenu.getMenu().add("Share");
//            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                @Override
//                public boolean onMenuItemClick(MenuItem item) {
//                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
//                    sharingIntent.setType("text/html");
//                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, Helper.generateShareText(postModel.getId()));
//                    if (sharingIntent.resolveActivity(activity.getPackageManager()) != null) {
//                        activity.startActivity(Intent.createChooser(sharingIntent,"Share using"));
//                    }
//                    return true;
//                }
//            });
//            popupMenu.show();
//        });

        database.getReference().child("likes")
                .child(postModel.getId()).child(auth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            boolean like = snapshot.getValue(Boolean.class);
                            if (like){
                                holder.binding.discoverLike.setLiked(true);
                                holder.binding.likeText.setText("Liked");
                            }else {
                                holder.binding.discoverLike.setLiked(false);
                                holder.binding.likeText.setText("Like");
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
                int k = postModel.getLikesCount() + 1;
                holder.binding.likeTextCount.setText(String.valueOf(k));

                database.getReference().child("likes")
                        .child(postModel.getId())
                        .child(auth.getCurrentUser().getUid())
                        .setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Call<ApiResponse> call = apiService.updatePostField(
                                        "updatePostField", postModel.getId(), "likesCount", String.valueOf(1)
                                );

                                NotificationModel notificationModel = new NotificationModel(auth.getCurrentUser().getUid(),
                                        "like", postModel.getId(), new Date().getTime(), false);
                                database.getReference().child("notification")
                                        .child(postModel.getUploader()).push().setValue(notificationModel);
                                if (postModel.getToken() != null && !postModel.getToken().isEmpty() &&
                                    !postModel.getToken().equalsIgnoreCase("NA")){
                                    MainActivity.sendNotification(postModel.getToken(), "Post Liked",
                                            "Someone liked your post!");
                                }
                                startCall(call, true, holder);
                            }
                        });
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                holder.binding.discoverLike.setEnabled(false);
                int k = postModel.getLikesCount() - 1;
                holder.binding.likeTextCount.setText(String.valueOf(k));

                database.getReference().child("likes")
                        .child(postModel.getId())
                        .child(auth.getCurrentUser().getUid())
                        .removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Call<ApiResponse> call = apiService.updatePostField(
                                        "updatePostField", postModel.getId(), "likesCount", String.valueOf(-1)
                                );
                                startCall(call, true, holder);
                            }
                        });
            }
        });

        holder.binding.share01.setOnClickListener(c->{
            holder.binding.discoverShare.performClick();
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/html");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, Helper.generateShareText(postModel.getId()));
            if (sharingIntent.resolveActivity(activity.getPackageManager()) != null) {
                activity.startActivity(Intent.createChooser(sharingIntent,"Share using"));
            }
        });

        holder.binding.discoverComment.setOnClickListener(c->{
            Intent i = new Intent(activity.getApplicationContext(), Comments_Post.class);
            i.putExtra("postId", postModel.getId());
            i.putExtra("uploaderId", postModel.getUploader());
            activity.startActivity(i);
        });

        holder.itemView.setOnClickListener(v->{
            Intent i = new Intent(activity.getApplicationContext(), See_Post.class);
            i.putExtra("postId", postModel.getId());
            activity.startActivity(i);
        });


    }

    public void startCall(Call<ApiResponse> call, boolean liked, viewHolder holder) {
        if (liked){
            holder.binding.likeText.setText("liked");
            holder.binding.discoverLike.setEnabled(true);
        }else {
            holder.binding.likeText.setText("like");
            holder.binding.discoverLike.setEnabled(true);
        }

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {

            }
        });
    }


    private class ImageLoaderTask extends AsyncTask<Void, Void, Bitmap> {
        private RoundedImageView imageView;
        private String url;

        public ImageLoaderTask(RoundedImageView imageView, String url) {
            this.imageView = imageView;
            this.url = url;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            try {
                URL imageUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) imageUrl.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                imageCache.put(url, bitmap); // Cache the image
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    public void addItems(List<PostResponse.PostData> newItems) {
        int startPosition = models.size();
        models.addAll(newItems);
        notifyItemRangeInserted(startPosition, newItems.size());
    }

//    public void addItems(List<PostResponse.PostData> newPosts) {
//        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new PostDiffCallback(models, newPosts));
//        models.clear();
//        models.addAll(newPosts);
//        diffResult.dispatchUpdatesTo(this);
//    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ItemSinglePostBinding binding;

        public PostViewHolder(@NonNull ItemSinglePostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    static class PostDiffCallback extends DiffUtil.Callback {

        private final List<PostResponse.PostData> oldList;
        private final List<PostResponse.PostData> newList;

        public PostDiffCallback(List<PostResponse.PostData> oldList, List<PostResponse.PostData> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getId().equals(newList.get(newItemPosition).getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
        }
    }

    public void removeItems() {
        this.models.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return models.size();
    }

    public static class viewHolder extends RecyclerView.ViewHolder{
        ItemSinglePostBinding binding;
        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSinglePostBinding.bind(itemView);
        }
    }
}
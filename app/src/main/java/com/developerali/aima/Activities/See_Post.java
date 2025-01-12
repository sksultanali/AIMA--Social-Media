package com.developerali.aima.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developerali.aima.Adapters.PostAdapter;
import com.developerali.aima.Helpers.Helper;
import com.developerali.aima.Helpers.TextUtils;
import com.developerali.aima.MainActivity;
import com.developerali.aima.Model_Apis.ApiResponse;
import com.developerali.aima.Model_Apis.ApiService;
import com.developerali.aima.Model_Apis.PostResponse;
import com.developerali.aima.Model_Apis.RetrofitClient;
import com.developerali.aima.Model_Apis.SinglePostData;
import com.developerali.aima.Model_Apis.UserDetails;
import com.developerali.aima.Models.NotificationModel;
import com.developerali.aima.Models.PostModel;
import com.developerali.aima.Models.UserModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ActivitySeePostBinding;
import com.developerali.aima.databinding.DialogNotLoginBinding;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.rpc.Help;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.qkopy.richlink.ViewListener;

import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class See_Post extends AppCompatActivity {

    ActivitySeePostBinding binding;
    String postId;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth auth;
    Activity activity;
    FirebaseDatabase database;
    Boolean isOpen;
    private long startTime;
    private long totalSeconds;
    SharedPreferences sharedPreferences;
    ApiService apiService;
    int maxLength = 150;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySeePostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        apiService = RetrofitClient.getClient().create(ApiService.class);

        firebaseFirestore = FirebaseFirestore.getInstance();
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        activity = See_Post.this;
        isOpen = true;

        Intent intent = getIntent();
        Uri data = intent.getData();

        if (data != null && auth.getCurrentUser() != null) {
            Toast.makeText(this, "loading request...", Toast.LENGTH_SHORT).show();
            postId = extractLink(data);
        }else if (data!= null && auth.getCurrentUser() == null){
            Toast.makeText(this, "loading request...", Toast.LENGTH_SHORT).show();
            showNotLoginDialog();
        }else {
            postId = intent.getStringExtra("postId");
        }

        if (auth.getCurrentUser() != null){
            loadAllData(postId);
        }


        binding.postedImage.setOnClickListener(v->{
            if (isOpen){
                binding.topView.setVisibility(View.GONE);
                binding.bottomView.setVisibility(View.GONE);
                isOpen = false;
            }else {
                binding.topView.setVisibility(View.VISIBLE);
                binding.bottomView.setVisibility(View.VISIBLE);
                isOpen = true;
            }
        });

        binding.backBtn.setOnClickListener(v->{
            Intent i;
            if (auth.getCurrentUser() != null){
                i = new Intent(See_Post.this, MainActivity.class);

            }else {
                i = new Intent(See_Post.this, Login.class);
            }
            startActivity(i);
            finish();
        });








    }

    private void showNotLoginDialog() {
        DialogNotLoginBinding dialogNotLoginBinding = DialogNotLoginBinding.inflate(getLayoutInflater());
        Dialog dialog1 = new Dialog(this);
        dialog1.setContentView(dialogNotLoginBinding.getRoot());

        dialog1.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog1.getWindow().setGravity(Gravity.CENTER_VERTICAL);

        dialog1.setCancelable(false);
        dialog1.setCanceledOnTouchOutside(false);

        dialogNotLoginBinding.messageText.setText("Login is required for visit a POST. You can see this post after get logged in :)");
        dialogNotLoginBinding.loginBtn.setOnClickListener(v->{
            Intent i = new Intent(See_Post.this, Login.class);
            startActivity(i);
            finish();
        });

        dialog1.show();
    }

    private void loadAllData(String postId) {
        Call<SinglePostData> call = apiService.getSinglePost(
                "getSinglePost", postId
        );

        call.enqueue(new Callback<SinglePostData>() {
            @Override
            public void onResponse(Call<SinglePostData> call, Response<SinglePostData> response) {
                if (response.isSuccessful() && response.body() != null){
                    SinglePostData singlePostData = response.body();
                    if (singlePostData.getStatus().equalsIgnoreCase("success")){
                        PostResponse.PostData postModel = singlePostData.getData();

                        List<String> links = TextUtils.extractLinks(postModel.getCaption());
                        if (!links.isEmpty()){
                            if (postModel.getImage() == null || postModel.getImage().isEmpty()){
                                binding.richLink.setLink(links.get(0), activity, new ViewListener() {
                                    @Override
                                    public void onSuccess(boolean b) {
                                        binding.richLink.setVisibility(View.VISIBLE);
                                        binding.postedImage.setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onError(@NonNull Exception e) {
                                        binding.richLink.setVisibility(View.GONE);
                                        binding.postedImage.setImageDrawable(activity.getDrawable(R.drawable.link_broken));
                                        binding.postedImage.setVisibility(View.VISIBLE);
                                    }
                                });
                            }
                        }else {
                            binding.richLink.setVisibility(View.GONE);
                        }

                        if (postModel.getImage() != null && !postModel.getImage().isEmpty() && !activity.isDestroyed()){
                            binding.postedImage.setVisibility(View.VISIBLE);
                            Glide.with(activity)
                                    .load(postModel.getImage())
                                    .placeholder(R.drawable.placeholder)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .skipMemoryCache(false)
                                    .into(binding.postedImage);
                        }else {
                            binding.bottomView.setVisibility(View.GONE);
                        }

                        comments(postId, postModel.getUploader());
                        LikeMethod(postModel);

                        binding.uploaderProfileImage.setOnClickListener(v->{
                            if (postModel.getUploader() != null && !postModel.getUploader().equalsIgnoreCase("admin")){
                                Intent i = new Intent(activity.getApplicationContext(), ProfileActivity.class);
                                i.putExtra("profileId", postModel.getUploader());
                                activity.startActivity(i);
                            }else {
                                Helper.showAlertNoAction(See_Post.this, "Admin Profile",
                                        "This profile can't be check by you. Have a good day!", "Thank You");
                            }
                        });

                        //setting all details about post
                        binding.commentText.setText(String.valueOf(postModel.getCommentsCount()));
                        binding.likeTextCount.setText(String.valueOf(postModel.getLikesCount()));


                        long time = Helper.convertToLongTime(postModel.getTime());
                        String timeAgo = (time == -1) ? Helper.formatDate("yyyy-MM-dd HH:mm:ss",
                                "dd LLL yyyy", postModel.getTime()) : TimeAgo.using(time);

                        if (postModel.getUploader().equalsIgnoreCase("admin")){
                            binding.postedBy.setText("Admin Post");
                            binding.verifiedProfile.setVisibility(View.VISIBLE);
                            binding.postedProfile.setText("App Admin" + " • " + timeAgo + " •");
                            binding.uploaderProfileImage.setImageDrawable(activity.getDrawable(R.drawable.aimalogo));
                        }else {
                            binding.postedBy.setText(postModel.getName());
                            if (postModel.getType() != null){
                                binding.postedProfile.setText(postModel.getType() + " • " + timeAgo + " •");
                            }else {
                                binding.postedProfile.setText("Public Profile" + " • " + timeAgo + " •");
                            }
                            if (postModel.getVerified() != 0){
                                binding.verifiedProfile.setVisibility(View.VISIBLE);
                            }else {
                                binding.verifiedProfile.setVisibility(View.GONE);
                            }
                            if (postModel.getUser_image() != null && !activity.isDestroyed()){
                                Glide.with(activity.getApplicationContext())
                                        .load(postModel.getUser_image())
                                        .placeholder(activity.getDrawable(R.drawable.profileplaceholder))
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .override(50, 50)
                                        .priority(Priority.HIGH)
                                        .into(binding.uploaderProfileImage);
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

                            SpannableString spannableString = TextUtils.applySpannable(activity, displayText, fullCaption, binding.uploaderCaption);
                            binding.uploaderCaption.setText(spannableString);
                            binding.uploaderCaption.setMovementMethod(LinkMovementMethod.getInstance());
                            binding.uploaderCaption.setHighlightColor(Color.TRANSPARENT);
                        } else {
                            binding.uploaderCaption.setVisibility(View.GONE);
                        }


                        binding.spinKit.setVisibility(View.GONE);





                        //get already liked or not?
                        database.getReference().child("likes")
                                .child(postModel.getId())
                                .child(auth.getCurrentUser().getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            boolean like = snapshot.getValue(Boolean.class);
                                            if (like){
                                                binding.discoverLike.setLiked(true);
                                                binding.likeText.setText("Liked");
                                            }else {
                                                binding.discoverLike.setLiked(false);
                                                binding.likeText.setText("Like");
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                        //sharing
                        binding.share01.setOnClickListener(c->{
                            Toast.makeText(See_Post.this, "loading request...", Toast.LENGTH_SHORT).show();
                            binding.discoverShare.performClick();
                            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                            sharingIntent.setType("text/html");
                            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, Helper.generateShareText(postId));
                            if (sharingIntent.resolveActivity(getPackageManager()) != null) {
                                startActivity(Intent.createChooser(sharingIntent,"Share using"));
                            }

                        });
                    }else {
                        Helper.showAlertNoAction(See_Post.this, "Failed",
                                singlePostData.getMessage(), "Okay");
                    }
                }
            }

            @Override
            public void onFailure(Call<SinglePostData> call, Throwable t) {
                Helper.showAlertNoAction(See_Post.this, "Error 404",
                        t.getLocalizedMessage(), "Okay");
            }
        });

        binding.postedImage.setOnClickListener(v->{
            binding.container.performClick();
        });
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        sharedPreferences = getSharedPreferences("UsageTime", MODE_PRIVATE); //creating database
//        totalSeconds = sharedPreferences.getLong("total_seconds", 0);  //getting previous value
//        startTime = System.currentTimeMillis();  //get start time for counting
//    }
//
//    @Override
//    protected void onPause() {
//        long currentTime = System.currentTimeMillis();  //get stop time for counting
//        long totalTime = currentTime - startTime;   //calculating watch time
//        long newTime = totalSeconds + (totalTime/1000);    //add previous sec and now time converting in sec
//
//        SharedPreferences.Editor editor = sharedPreferences.edit();  // updating in database
//        editor.putLong("total_seconds", newTime);
//        editor.apply();
//
//        ArrayList<UsagesModel> arrayList = CommonFeatures.readListFromPref(this);
//        UsagesModel usagesModel = new UsagesModel("See Posts", startTime, currentTime);
//        arrayList.add(usagesModel);
//        CommonFeatures.writeListInPref(See_Post.this, arrayList);
//
//        super.onPause();
//    }

    private void comments(String postIdm, String uploader){
        binding.discoverComment.setOnClickListener(v->{
            Intent i = new Intent(See_Post.this, Comments_Post.class);
            i.putExtra("postId", postIdm);
            i.putExtra("uploaderId", uploader);
            startActivity(i);
        });
    }

    private void LikeMethod(PostResponse.PostData postModel) {
        binding.like01.setOnClickListener(c->{
            binding.discoverLike.performClick();
        });

        binding.discoverLike.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                binding.discoverLike.setEnabled(false);
                int k = postModel.getLikesCount() + 1;
                binding.likeTextCount.setText(String.valueOf(k));

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
                                startCall(call, true);
                            }
                        });
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                binding.discoverLike.setEnabled(false);
                int k = postModel.getLikesCount() - 1;
                binding.likeTextCount.setText(String.valueOf(k));

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
                                startCall(call, true);
                            }
                        });
            }
        });
    }

    public void startCall(Call<ApiResponse> call, boolean liked) {
        if (liked){
            binding.likeText.setText("liked");
            binding.discoverLike.setEnabled(true);
        }else {
            binding.likeText.setText("like");
            binding.discoverLike.setEnabled(true);
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

//    private void getUserDetails(String uploader) {
//
//        Call<UserDetails> call = apiService.getUserDetails(
//                "getUserDetails", uploader
//        );
//
//        call.enqueue(new Callback<UserDetails>() {
//            @Override
//            public void onResponse(Call<UserDetails> call, Response<UserDetails> response) {
//                if (response.isSuccessful() && response.body() != null){
//                    UserDetails apiResponse = response.body();
//                    if (apiResponse.getStatus().equalsIgnoreCase("success")){
//                        UserModel userModel = apiResponse.getData();
//
//                        //String timeAgo = TimeAgo.using(timestamp);
//                        binding.postedBy.setText(userModel.getName());
//                        //binding.postedProfile.setText(userModel.getType() + " • " + timeAgo + " •");
//
//                        if (userModel.getVerified() != 0){
//                            binding.verifiedProfile.setVisibility(View.VISIBLE);
//                        }else {
//                            binding.verifiedProfile.setVisibility(View.GONE);
//                        }
//
//                        if (userModel.getImage() != null && !activity.isDestroyed() &&
//                                !userModel.getImage().isEmpty()){
//                            Glide.with(getApplicationContext())
//                                    .load(userModel.getImage())
//                                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
//                                    .placeholder(getDrawable(R.drawable.placeholder))
//                                    .into(binding.uploaderProfileImage);
//                        }
//
//                        binding.seePostProfile.setOnClickListener(c->{
//                            if (!userModel.getUserId().equalsIgnoreCase("admin")){
//                                Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
//                                i.putExtra("profileId", userModel.getUserId());
//                                startActivity(i);
//                            }else {
//                                Toast.makeText(activity, "not possible...", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<UserDetails> call, Throwable t) {
//            }
//        });
//    }

    public String extractLink (Uri link){

        List<String> pathSegments = link.getPathSegments();

        if (pathSegments.size() >= 1){
            String value = pathSegments.get(0);
            return value;
        }
        return "null";
    }
}
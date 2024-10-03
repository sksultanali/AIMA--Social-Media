package com.developerali.aima.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developerali.aima.CommonFeatures;
import com.developerali.aima.Interfaces.DoubleClickHandler;
import com.developerali.aima.Interfaces.DoubleClickListener;
import com.developerali.aima.MainActivity;
import com.developerali.aima.Models.NotificationModel;
import com.developerali.aima.Models.PostModel;
import com.developerali.aima.Models.UsagesModel;
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
import com.like.LikeButton;
import com.like.OnLikeListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySeePostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
            binding.backBtn.setOnClickListener(v->{
                Intent i = new Intent(See_Post.this, MainActivity.class);
                i.setFlags(i.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            });

        }


        binding.container.setOnClickListener(v->{
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
            i.setFlags(i.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });

        dialog1.show();
    }

    private void loadAllData(String postId) {

        firebaseFirestore.collection("post")
                .document(postId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        PostModel postModel = documentSnapshot.toObject(PostModel.class);

                        comments(postId, postModel.getUploader());
                        //setting all details about post
                        if (postModel.getCaption() != null){
                            if (postModel.getCaption().length() > 150){
                                binding.uploaderCaption.setText(postModel.getCaption().substring(0, 135) + "...Read More");
                                binding.uploaderCaption.setOnClickListener(c->{
                                    binding.uploaderCaption.setText(postModel.getCaption());
                                    binding.container.performClick();
                                });
                            }else {
                                binding.uploaderCaption.setText(postModel.getCaption());
                            }
                        }else {
                            binding.uploaderCaption.setVisibility(View.GONE);
                        }

                        if (postModel.getImage() != null && !activity.isDestroyed() ){
                            Glide.with(See_Post.this)
                                    .load(postModel.getImage())
                                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                    .placeholder(R.drawable.placeholder)
                                    .into(binding.postedImage);
                        }else {
                            binding.bottomView.setVisibility(View.GONE);
                        }
//                        else {
//                            binding.postedImage.setVisibility(View.GONE);
//                            binding.container.setBackgroundColor(getColor(R.color.darkGray));
//                            binding.profileSection.setBackgroundColor(getColor(R.color.white));
//                            Drawable drawable = ContextCompat.getDrawable(See_Post.this, R.drawable.public_24);
//                            drawable.setColorFilter(getColor(R.color.black), PorterDuff.Mode.MULTIPLY);
//                            binding.postedProfile.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
//                            binding.postedBy.setTextColor(getColor(R.color.black));
//                            binding.postedProfile.setTextColor(getColor(R.color.black));
//                            binding.textCommentFalse.setTextColor(getColor(R.color.black));
//                            binding.textLikeFalse.setTextColor(getColor(R.color.black));
//                            binding.likeTextCount.setTextColor(getColor(R.color.black));
//                            binding.commentText.setTextColor(getColor(R.color.black));
//                            binding.backBtn.setColorFilter(getColor(R.color.black));
//                            binding.uploaderCaption.setTextColor(getColor(R.color.black));
//                        }

                        binding.commentText.setText(postModel.getCommentsCount()+"");
                        binding.likeTextCount.setText(postModel.getLikesCount()+"");
                        binding.spinKit.setVisibility(View.GONE);


                        //getting all data about uploader!
                        getUserDetails(postModel.getUploader(), postModel.getTime());


                        //get already liked or not?
                        database.getReference().child("likes")
                                .child(documentSnapshot.getId())
                                .child(auth.getCurrentUser().getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            boolean like = snapshot.getValue(Boolean.class);
                                            if (like){
                                                binding.discoverLike.setLiked(true);
                                                binding.likeText.setText("Liked");
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });


                        //get like workings
                        LikeMethod(documentSnapshot.getId(), postModel.getUploader());


                        //sharing
                        binding.share01.setOnClickListener(c->{
                            Toast.makeText(See_Post.this, "loading request...", Toast.LENGTH_SHORT).show();
                            binding.discoverShare.performClick();
                            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                            sharingIntent.setType("text/html");
                            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                                    "This post is shared from AIMA App. Check using app only ! \n\n" +
                                            " link: https://i.aima.post/" + documentSnapshot.getId());
                            if (sharingIntent.resolveActivity(getPackageManager()) != null) {
                                startActivity(Intent.createChooser(sharingIntent,"Share using"));
                            }

                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(See_Post.this, "post not exist !", Toast.LENGTH_SHORT).show();
                        binding.spinKit.setVisibility(View.GONE);
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
            i.setFlags(i.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        });
    }

    private void LikeMethod(String postId, String uploader) {
        binding.like01.setOnClickListener(c->{
            binding.discoverLike.performClick();
        });

        binding.discoverLike.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                binding.discoverLike.setEnabled(false);
                int k = Integer.parseInt(binding.likeTextCount.getText().toString()) + 1;
                binding.likeTextCount.setText(k+"");
                database.getReference().child("likes")
                        .child(postId)
                        .child(auth.getCurrentUser().getUid())
                        .setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                firebaseFirestore.collection("post")
                                        .document(postId)
                                        .update("likesCount", FieldValue.increment(1))
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        NotificationModel notificationModel = new NotificationModel(auth.getCurrentUser().getUid(),
                                                                "like", postId, new Date().getTime(), false);
                                                        database.getReference().child("notification")
                                                                .child(uploader)
                                                                .push()
                                                                .setValue(notificationModel);

                                                        binding.likeText.setText("liked");
                                                        binding.discoverLike.setEnabled(true);
                                                    }
                                                });
                            }
                        });
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                binding.discoverLike.setEnabled(false);
                int k = Integer.parseInt(binding.likeTextCount.getText().toString()) - 1;
                binding.likeTextCount.setText(k+"");
                database.getReference().child("likes")
                        .child(postId)
                        .child(auth.getCurrentUser().getUid())
                        .removeValue()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                firebaseFirestore.collection("post")
                                        .document(postId)
                                        .update("likesCount", FieldValue.increment(-1));

                                binding.likeText.setText("like");
                                binding.discoverLike.setEnabled(true);
                            }
                        });
            }
        });
    }

    private void getUserDetails(String uploader, long timestamp) {

        database.getReference().child("users")
                .child(uploader)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            UserModel userModel = snapshot.getValue(UserModel.class);

                            String timeAgo = TimeAgo.using(timestamp);
                            binding.postedBy.setText(userModel.getName());

                            if (userModel.isVerified()){
                                binding.verifiedProfile.setVisibility(View.VISIBLE);
                            }
                            if (userModel.getType() != null){
                                binding.postedProfile.setText(userModel.getType() + " • " + timeAgo + " •");
                            }else {
                                binding.postedProfile.setText("Public Profile" + " • " + timeAgo + " •");
                            }
                            if (userModel.getImage() != null && !activity.isDestroyed()){
                                Glide.with(getApplicationContext())
                                                .load(userModel.getImage())
                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                                        .placeholder(getDrawable(R.drawable.placeholder))
                                                                .into(binding.uploaderProfileImage);
                            }

                            binding.seePostProfile.setOnClickListener(c->{
                                if (!snapshot.getKey().equalsIgnoreCase("admin")){
                                    Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
                                    i.putExtra("profileId", snapshot.getKey());
                                    i.setFlags(i.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(i);
                                }else {
                                    Toast.makeText(activity, "not possible...", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public String extractLink (Uri link){

        List<String> pathSegments = link.getPathSegments();

        if (pathSegments.size() >= 1){
            String value = pathSegments.get(0);
            return value;
        }
        return "null";
    }
}
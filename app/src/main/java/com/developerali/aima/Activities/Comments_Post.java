package com.developerali.aima.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developerali.aima.Adapters.CommentAdapter;
import com.developerali.aima.Adapters.PostAdapter;
import com.developerali.aima.Helpers.CommonFeatures;
import com.developerali.aima.Helpers.Helper;
import com.developerali.aima.MainActivity;
import com.developerali.aima.Model_Apis.ApiResponse;
import com.developerali.aima.Model_Apis.ApiService;
import com.developerali.aima.Model_Apis.RetrofitClient;
import com.developerali.aima.Model_Apis.UserDetails;
import com.developerali.aima.Models.CommentModel;
import com.developerali.aima.Models.NotificationModel;
import com.developerali.aima.Models.UserModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ActivityCommentsPostBinding;
import com.developerali.aima.databinding.CommentBottomNavigationBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.rpc.Help;

import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Comments_Post extends AppCompatActivity {

    ActivityCommentsPostBinding binding;
    //PostModel model;
    ArrayList<CommentModel> arrayList;
    ProgressDialog progressDialog;
    FirebaseDatabase database;
    FirebaseFirestore firebaseFirestore;
    CommentAdapter adapter;
    FirebaseAuth auth;
    String postId, uploader, uploaderToken;
    private long startTime;
    Long totalSeconds;
    Activity activity;
    SharedPreferences sharedPreferences;
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommentsPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        apiService = RetrofitClient.getClient().create(ApiService.class);

        CommonFeatures.lowerColour(getWindow(), getResources());
        activity = Comments_Post.this;

        database = FirebaseDatabase.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        postId = getIntent().getStringExtra("postId");
        uploader = getIntent().getStringExtra("uploaderId");

        getRefresh();

        progressDialog = new ProgressDialog(Comments_Post.this);
        progressDialog.setTitle("Comment Uploading");
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        binding.backComments.setOnClickListener(v->{
            onBackPressed();
        });

        binding.commentInput.setOnClickListener(v->{
            showBottomBar();
        });

        Call<UserDetails> call = apiService.getUserDetails(
                "getUserDetails", uploader
        );

        call.enqueue(new Callback<UserDetails>() {
            @Override
            public void onResponse(Call<UserDetails> call, Response<UserDetails> response) {
                if (response.isSuccessful() && response.body() != null){
                    UserDetails apiResponse = response.body();
                    if (apiResponse.getStatus().equalsIgnoreCase("success")){
                        uploaderToken = apiResponse.getData().getToken();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserDetails> call, Throwable t) {
            }
        });


        if (Helper.userDetails != null){
            if (Helper.userDetails.getImage() != null  && !Helper.userDetails.getImage().isEmpty() &&
                    !activity.isDestroyed()){
                Glide.with(Comments_Post.this)
                    .load(Helper.userDetails.getImage())
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .placeholder(R.drawable.profileplaceholder)
                    .into(binding.commenterProfileImage);
            }
        }



    }

    public void showBottomBar(){

        CommentBottomNavigationBinding dialogBinding = CommentBottomNavigationBinding.inflate(getLayoutInflater());

        // Create a new dialog and set the custom layout
        Dialog dialog = new Dialog(this);
        dialog.setContentView(dialogBinding.getRoot());
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        dialogBinding.postBtn.setEnabled(false);
        dialogBinding.commentInput.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialogBinding.commentInput.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(dialogBinding.commentInput, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 200);

        dialogBinding.postBtn.setOnClickListener(v->{

            progressDialog.show();

            CommentModel commentModel = new CommentModel();
            commentModel.setCommentedAt(new Date().getTime());
            commentModel.setPostId(postId);
            commentModel.setCommentedBy(auth.getCurrentUser().getUid());
            commentModel.setComment(dialogBinding.commentInput.getText().toString());

            database.getReference().child("comments").child(postId)
                    .child(database.getReference().push().getKey())
                    .setValue(commentModel)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Call<ApiResponse> call = apiService.updatePostField(
                                    "updatePostField", postId, "commentsCount", String.valueOf(1)
                            );
                            call.enqueue(new Callback<ApiResponse>() {
                                @Override
                                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {

                                }

                                @Override
                                public void onFailure(Call<ApiResponse> call, Throwable t) {

                                }
                            });
                            NotificationModel notificationModel = new NotificationModel(auth.getCurrentUser().getUid(),
                                    "comment", postId, new Date().getTime(), false);
                            if (uploaderToken != null && !uploaderToken.isEmpty() &&
                                    !uploaderToken.equalsIgnoreCase("NA")){
                                MainActivity.sendNotification(uploaderToken, "Post Comment",
                                        "Someone commented on your post!");
                            }
                            database.getReference().child("notification")
                                    .child(uploader)
                                    .push()
                                    .setValue(notificationModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            dialog.dismiss();
                                            progressDialog.dismiss();
                                        }
                                    });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(Comments_Post.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                            dialog.dismiss();
                        }
                    });
        });

        dialogBinding.commentInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                dialogBinding.commentCounter.setText(charSequence.toString().length() +"/2000");
                if (charSequence.toString().length() > 1){
                    dialogBinding.postBtn.setEnabled(true);
                    dialogBinding.postBtn.setBackground(getDrawable(R.drawable.button_follow_background));
                }
                if (charSequence.toString().length() < 1){
                    dialogBinding.postBtn.setEnabled(false);
                    dialogBinding.postBtn.setBackground(getDrawable(R.drawable.button_already_followd));
                }
                if (charSequence.toString().length() > 1950){
                    dialogBinding.commentCounter.setTextColor(getColor(R.color.red_colour));
                }else {
                    dialogBinding.commentCounter.setTextColor(getColor(R.color.black));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });




        // Show the dialog
        dialog.show();
    }


    public void getRefresh(){
        arrayList = new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(Comments_Post.this);
        binding.commentRecycler.setLayoutManager(linearLayoutManager);

        database.getReference().child("comments").child(postId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){

                            arrayList.clear();

                            for (DataSnapshot snapshot1 : snapshot.getChildren()){
                                CommentModel commentModel = snapshot1.getValue(CommentModel.class);

                                if (commentModel != null){
                                    commentModel.setCommentId(snapshot1.getKey());
                                    commentModel.setCommentedBy(commentModel.getCommentedBy());
                                    commentModel.setPostId(postId);
                                    arrayList.add(commentModel);
                                }
                            }

                            adapter = new CommentAdapter(Comments_Post.this, arrayList);
                            binding.commentRecycler.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                            binding.textNoComments.setVisibility(View.GONE);
                        }else {
                            binding.textNoComments.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        sharedPreferences = getSharedPreferences("UsageTime", MODE_PRIVATE); //creating database
//        totalSeconds = sharedPreferences.getLong("total_seconds", 0);  //getting previous value
//        startTime = System.currentTimeMillis();  //get start time for counting
//    }

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
//        UsagesModel usagesModel = new UsagesModel("Commenting on Post", startTime, currentTime);
//        arrayList.add(usagesModel);
//        CommonFeatures.writeListInPref(Comments_Post.this, arrayList);
//
//        super.onPause();
//    }

}
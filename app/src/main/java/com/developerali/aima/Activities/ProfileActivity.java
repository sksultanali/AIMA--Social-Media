package com.developerali.aima.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developerali.aima.Adapters.FriendsAdapter;
import com.developerali.aima.Adapters.PostAdapter;
import com.developerali.aima.Adapters.ProfilePostAdapter;
import com.developerali.aima.Forms.verifiedBadgeActivity;
import com.developerali.aima.Helpers.Helper;
import com.developerali.aima.Helpers.TextUtils;
import com.developerali.aima.Helpers.UserDataUpdate;
import com.developerali.aima.HomeBottomBar.AimaAdmin;
import com.developerali.aima.MainActivity;
import com.developerali.aima.Model_Apis.ApiService;
import com.developerali.aima.Model_Apis.PostResponse;
import com.developerali.aima.Model_Apis.RetrofitClient;
import com.developerali.aima.Model_Apis.UserDetails;
import com.developerali.aima.Models.FollowModel;
import com.developerali.aima.Models.NotificationModel;
import com.developerali.aima.Models.PostModel;
import com.developerali.aima.Models.UserModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ActivityProfileBinding;
import com.developerali.aima.databinding.DialogNotLoginBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.like.LikeButton;
import com.like.OnLikeListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    ActivityProfileBinding binding;
    Uri selectedImageUri;
    FirebaseDatabase database;
    Activity activity;
    FirebaseAuth auth;
    FirebaseStorage storage;
    FirebaseFirestore firebaseFirestore;
    ProgressDialog dialog;
    UserModel userModel;
    String imageUrl, profileUserId;
    ArrayList<FollowModel> followModels;
    ApiService apiService;
    ProfilePostAdapter postAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        apiService = RetrofitClient.getClient().create(ApiService.class);

        dialog = new ProgressDialog(ProfileActivity.this);
        dialog.setMessage("loading request...");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        activity = ProfileActivity.this;

        Intent intent = getIntent();
        if (intent != null && intent.getData() != null && auth.getCurrentUser() != null){
            Uri data = intent.getData();
            profileUserId = extractLink(data);
        }else if (intent != null && intent.getData() != null && auth.getCurrentUser() == null){
            showLoginDialog();
            Uri data = intent.getData();
            profileUserId = extractLink(data);
        }else {
            profileUserId = intent.getStringExtra("profileId");
        }



        if (profileUserId != null){
            profIdTrue(profileUserId);
            copyProfileLink(profileUserId);
            if (!activity.isDestroyed()){
                fetchAllPost(profileUserId);
            }
        }else {
            if (!activity.isDestroyed()){
                fetchAllPost(auth.getCurrentUser().getUid());
            }

            Call<UserDetails> call = apiService.getUserDetails(
                    "getUserDetails", auth.getCurrentUser().getUid()
            );

            call.enqueue(new Callback<UserDetails>() {
                @Override
                public void onResponse(Call<UserDetails> call, Response<UserDetails> response) {
                    if (response.isSuccessful() && response.body() != null){
                        UserDetails apiResponse = response.body();
                        if (apiResponse.getStatus().equalsIgnoreCase("success")){
                            userModel = apiResponse.getData();
                            if (userModel.getImage() != null && !activity.isDestroyed() && !userModel.getImage().isEmpty()){
                                Glide.with(getApplicationContext())
                                        .load(userModel.getImage())
                                        .placeholder(getDrawable(R.drawable.profileplaceholder))
                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                        .into(binding.myProfile);
                            }

                            if (userModel.getCover() != null && !activity.isDestroyed() && !userModel.getCover().isEmpty()){
                                Glide.with(getApplicationContext())
                                        .load(userModel.getCover())
                                        .placeholder(getDrawable(R.drawable.profileback))
                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                        .into(binding.dashboardImage);
                            }

                            binding.dashBoardName.setText(userModel.getName());
                            binding.dashBoardProfileType.setText(userModel.getType());

                            Helper.showBadge(userModel.getVerified(), userModel.getVerified_valid(), binding.verifiedProfile);

                            if (userModel.getBio() == null || userModel.getBio().isEmpty()){
                                binding.dashBoardBio.setVisibility(View.GONE);
                            }else {
                                binding.dashBoardBio.setText(userModel.getBio());
                                binding.dashBoardBio.setVisibility(View.VISIBLE);
                            }
                            if (userModel.getAbout() == null || userModel.getAbout().isEmpty()){
                                binding.dashBoardAbout.setVisibility(View.GONE);
                            }else {
                                binding.dashBoardAbout.setText(userModel.getAbout());
                                binding.dashBoardAbout.setVisibility(View.VISIBLE);
                            }

                            TextUtils.startCounter(userModel.getFollower(), binding.followers, "", "+");
                            TextUtils.startCounter(userModel.getFollowing(), binding.following, "", "+");
                            TextUtils.startCounter(userModel.getPosts(), binding.posts, "", "+");
                            binding.stars.setText(String.valueOf(userModel.getStars()));

                            showImage(userModel.getImage(), userModel.getCover());
                            SocialMediaClicks(userModel);
                            if (userModel.getImage() ==null || userModel.getBio() == null || userModel.getAbout() == null
                                    || userModel.getName() == null){
//                                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        showCompleteProfile(userModel);
//                                    }
//                                }, 5000);
                                binding.editBtn.setAnimation(AnimationUtils.loadAnimation(ProfileActivity.this, R.anim.blink));

                            }else if (userModel.getFollower() > 200 && userModel.getVerified() == 0){
                                database.getReference().child("verified")
                                        .child(auth.getCurrentUser().getUid())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (!snapshot.exists() && activity != null && !activity.isDestroyed()) {
                                                    // Post a delayed task to the main thread
                                                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                                        if (!activity.isDestroyed()) {
                                                            showVerified();
                                                        }
                                                    }, 5000);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                            }
                            deferemciateProfile(userModel.getUserId());
                            shareProfile(userModel.getUserId());
                            copyProfileLink(userModel.getUserId());
                            binding.spinKitProfile.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onFailure(Call<UserDetails> call, Throwable t) {
                }
            });
        }






        binding.profileDashboard.setOnClickListener(v->{
            finish();
        });





    }

    private void copyProfileLink(String profId) {
        String link = "https://i.aima.profile/" + profId;
        binding.profileLink.setText(link);

        binding.profileLink.setOnClickListener(v->{
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Profile Link Copied", link);
            Toast.makeText(ProfileActivity.this, "Profile Link Copied", Toast.LENGTH_LONG).show();
            clipboard.setPrimaryClip(clip);
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
//
//        UsagesModel usagesModel = new UsagesModel("Checked Profile", startTime, currentTime);
//        arrayList.add(usagesModel);
//        CommonFeatures.writeListInPref(ProfileActivity.this, arrayList);
//
//        super.onPause();
//    }
    private void showVerified() {
        DialogNotLoginBinding dialogNotLoginBinding = DialogNotLoginBinding.inflate(getLayoutInflater());
        Dialog dialog1 = new Dialog(this);
        dialog1.setContentView(dialogNotLoginBinding.getRoot());

        dialog1.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog1.getWindow().setGravity(Gravity.CENTER_VERTICAL);

        dialog1.setCancelable(false);
        dialog1.setCanceledOnTouchOutside(false);

        dialogNotLoginBinding.titleText.setText("Congratulations!");
        dialogNotLoginBinding.messageText.setText("You are eligible for verified badge. Fill below form now- ");
        dialogNotLoginBinding.yesBtnText.setText("Fill Now");
        dialogNotLoginBinding.noBtnText.setText("Later");
        dialogNotLoginBinding.noBtn.setVisibility(View.VISIBLE);

        dialogNotLoginBinding.loginBtn.setOnClickListener(c->{
//            Intent j = new Intent(ProfileActivity.this, verifiedBadgeActivity.class);
//            startActivity(j);
            Helper.openLink(ProfileActivity.this, "https://aima.developerali.in/verified_badge.php");
            dialog1.dismiss();
        });

        dialogNotLoginBinding.noBtn.setOnClickListener(c->{
            dialog1.dismiss();
        });

        if (!activity.isDestroyed()){
            dialog1.show();
        }
    }

    private void showCompleteProfile(UserModel userModel) {
        DialogNotLoginBinding dialogNotLoginBinding = DialogNotLoginBinding.inflate(getLayoutInflater());
        Dialog dialog1 = new Dialog(this);
        dialog1.setContentView(dialogNotLoginBinding.getRoot());

        dialog1.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog1.getWindow().setGravity(Gravity.CENTER_VERTICAL);

        dialog1.setCancelable(false);
        dialog1.setCanceledOnTouchOutside(false);

        dialogNotLoginBinding.titleText.setText("Complete Profile");
        dialogNotLoginBinding.messageText.setText("Complete your profile information to get more followers!");
        dialogNotLoginBinding.yesBtnText.setText("Edit Now");
        dialogNotLoginBinding.noBtnText.setText("Later");
        dialogNotLoginBinding.noBtn.setVisibility(View.VISIBLE);

        dialogNotLoginBinding.loginBtn.setOnClickListener(c->{
            Intent j = new Intent(ProfileActivity.this, EditProfile.class);
            j.putExtra("userModel", userModel);
            startActivity(j);
            dialog1.dismiss();
        });

        dialogNotLoginBinding.noBtn.setOnClickListener(c->{
            dialog1.dismiss();
        });

        if (!activity.isDestroyed()){
            dialog1.show();
        }
    }

    private void fetchAllPost(String profileId){
        GridLayoutManager gridLayoutManager = new GridLayoutManager(ProfileActivity.this, 3);
        binding.postRecyclerView.setLayoutManager(gridLayoutManager);
        Call<PostResponse> call = apiService.getAllAdminPost(
                "getAllPost", "Approved", profileId, 0
        );
        call.enqueue(new Callback<PostResponse>() {
            @Override
            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                if (response.isSuccessful() && response.body() != null){
                    PostResponse apiResponse = response.body();
                    if (apiResponse.getStatus().equalsIgnoreCase("success") && apiResponse.getData() != null
                            && !apiResponse.getData().isEmpty()){
                        if (postAdapter == null) {
                            postAdapter = new ProfilePostAdapter(
                                    apiResponse.getData(), ProfileActivity.this);
                            binding.postRecyclerView.setAdapter(postAdapter);
                        }
                        binding.noPosts.setVisibility(View.GONE);
                        binding.viewAll.setVisibility(View.VISIBLE);
                    }else {
                        binding.noPosts.setVisibility(View.VISIBLE);
                        if (!profileId.equalsIgnoreCase(auth.getCurrentUser().getUid())){
                            binding.viewAll.setVisibility(View.GONE);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<PostResponse> call, Throwable t) {
                binding.noPosts.setVisibility(View.VISIBLE);
                if (!profileId.equalsIgnoreCase(auth.getCurrentUser().getUid())){
                    binding.viewAll.setVisibility(View.GONE);
                }
            }
        });

        binding.viewAll.setOnClickListener(v->{
            Intent i = new Intent(ProfileActivity.this, MyPostActivity.class);
            Helper.tempId = profileId;
            i.putExtra("profileId", profileId);
            startActivity(i);
        });

//        ArrayList<PostModel> postModelArrayList = new ArrayList<>();
//        firebaseFirestore.collection("post")
//                .whereEqualTo("uploader", profileId)
//                .orderBy("time", Query.Direction.DESCENDING)
//                .limit(30)
//                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        if (!queryDocumentSnapshots.isEmpty()) {
//
//                            for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments()){
//                                PostModel postModel = snapshot.toObject(PostModel.class);
//
//                                postModel.setId(snapshot.getId());
//                                postModel.setImage(snapshot.getString("image"));
//                                postModelArrayList.add(postModel);
//                            }
//                            ProfilePostAdapter postAdapter = new ProfilePostAdapter(
//                                    postModelArrayList, ProfileActivity.this);
//                            GridLayoutManager gridLayoutManager = new GridLayoutManager(ProfileActivity.this, 3);
//                            binding.postRecyclerView.setLayoutManager(gridLayoutManager);
//                            binding.postRecyclerView.setAdapter(postAdapter);
//                            postAdapter.notifyDataSetChanged();
//                        }else {
//                            binding.noPosts.setVisibility(View.VISIBLE);
//                        }
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        binding.noPosts.setVisibility(View.VISIBLE);
//                        Toast.makeText(ProfileActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
    }

    private void checkedlike(String profileUserId, int follower) {
        database.getReference().child("users").child(profileUserId)
                .child("follows").child(auth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            binding.followBtnLike.setLiked(true);
                            binding.followText.setText("Unfollow");
                            binding.followText.setTextColor(getColor(R.color.black));
                            binding.followBtn
                                    .setBackground(getDrawable(R.drawable.button_already_followd));
                        }else {
                            binding.followBtnLike.setLiked(false);

                            binding.followText.setText("Follow");
                            binding.followText.setTextColor(getColor(R.color.white));
                            binding.followBtn
                                    .setBackground(getDrawable(R.drawable.button_follow_background));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

    }

    public void LogOut(){
        DialogNotLoginBinding dialogNotLoginBinding = DialogNotLoginBinding.inflate(getLayoutInflater());
        Dialog dialog1 = new Dialog(this);
        dialog1.setContentView(dialogNotLoginBinding.getRoot());

        dialog1.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog1.getWindow().setGravity(Gravity.CENTER_VERTICAL);

        dialog1.setCancelable(false);
        dialog1.setCanceledOnTouchOutside(false);

        dialogNotLoginBinding.titleText.setText("Sign Out");
        dialogNotLoginBinding.messageText.setText("Are you sure you want to Log out?");
        dialogNotLoginBinding.yesBtnText.setText("Yes");
        dialogNotLoginBinding.noBtn.setVisibility(View.VISIBLE);

        dialogNotLoginBinding.loginBtn.setOnClickListener(c->{
            auth.signOut();
            Intent j = new Intent(ProfileActivity.this, Login.class);
            Toast.makeText(ProfileActivity.this, "You are logged out Successfully", Toast.LENGTH_SHORT).show();
            startActivity(j);
            finish();
        });

        dialogNotLoginBinding.noBtn.setOnClickListener(c->{
            dialog1.dismiss();
        });

        dialog1.show();
    }

    private void profIdTrue(String profileUserId){
        Call<UserDetails> call = apiService.getUserDetails(
                "getUserDetails", profileUserId
        );

        call.enqueue(new Callback<UserDetails>() {
            @Override
            public void onResponse(Call<UserDetails> call, Response<UserDetails> response) {
                if (response.isSuccessful() && response.body() != null){
                    UserDetails apiResponse = response.body();
                    if (apiResponse.getStatus().equalsIgnoreCase("success")){
                        userModel = apiResponse.getData();

                        if (userModel.getImage() != null && !activity.isDestroyed() && !userModel.getImage().isEmpty()){
                            Glide.with(getApplicationContext())
                                    .load(userModel.getImage())
                                    .placeholder(getDrawable(R.drawable.profileplaceholder))
                                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                    .into(binding.myProfile);
                        }

                        if (userModel.getCover() != null && !activity.isDestroyed() && !userModel.getCover().isEmpty()){
                            Glide.with(getApplicationContext())
                                    .load(userModel.getCover())
                                    .placeholder(getDrawable(R.drawable.profileback))
                                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                    .into(binding.dashboardImage);
                        }

                        binding.dashBoardName.setText(userModel.getName());
                        binding.dashBoardProfileType.setText(userModel.getType());

                        Helper.showBadge(userModel.getVerified(), userModel.getVerified_valid(), binding.verifiedProfile);

                        if (userModel.getBio() == null || userModel.getBio().isEmpty()){
                            binding.dashBoardBio.setVisibility(View.GONE);
                        }else {
                            binding.dashBoardAbout.setText(userModel.getAbout());
                            binding.dashBoardBio.setVisibility(View.VISIBLE);
                        }
                        if (userModel.getAbout() == null || userModel.getAbout().isEmpty()){
                            binding.dashBoardAbout.setVisibility(View.GONE);
                        }else {
                            binding.dashBoardBio.setText(userModel.getBio());
                            binding.dashBoardAbout.setVisibility(View.VISIBLE);
                        }

                        TextUtils.startCounter(userModel.getFollower(), binding.followers, "", "+");
                        TextUtils.startCounter(userModel.getFollowing(), binding.following, "", "+");
                        TextUtils.startCounter(userModel.getPosts(), binding.posts, "", "+");
                        binding.stars.setText(String.valueOf(userModel.getStars()));

                        SocialMediaClicks(userModel);
                        deferemciateProfile(userModel.getUserId());
                        showImage(userModel.getImage(), userModel.getCover());
                        shareProfile(userModel.getUserId());
                        checkedlike(profileUserId, userModel.getFollower());

                        binding.followBtn.setOnClickListener(v->{
                            binding.followBtnLike.performClick();
                        });

                        binding.followBtnLike.setOnLikeListener(new OnLikeListener() {
                            @Override
                            public void liked(LikeButton likeButton) {
                                dialog.show();
                                FollowModel followModel = new FollowModel();
                                followModel.setFollowBy(auth.getCurrentUser().getUid());
                                followModel.setFollowAt(new Date().getTime());

                                database.getReference().child("users").child(profileUserId)
                                        .child("follows").child(auth.getCurrentUser().getUid())
                                        .setValue(followModel)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                //int newFollower = userModel.getFollower() + 1;
                                                UserDataUpdate userDataUpdate = new UserDataUpdate();
                                                userDataUpdate.enqueueUpdateTask(profileUserId,
                                                        "follower", "1", ()->{
                                                            userDataUpdate.enqueueUpdateTask(auth.getCurrentUser().getUid(),
                                                                    "following", "1", ()->{
                                                                        profIdTrue(profileUserId);
                                                                        dialog.dismiss();
                                                                    });
                                                        });
                                                MainActivity.sendNotification(userModel.getToken(), "Follower",
                                                        "Someone started following you!");
                                            }
                                        });

                            }

                            @Override
                            public void unLiked(LikeButton likeButton) {
                                dialog.show();
                                FollowModel followModel = new FollowModel();
                                followModel.setFollowBy(auth.getCurrentUser().getUid());
                                followModel.setFollowAt(new Date().getTime());

                                database.getReference().child("users").child(profileUserId)
                                        .child("follows").child(auth.getCurrentUser().getUid()).removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {

                                                UserDataUpdate userDataUpdate = new UserDataUpdate();
                                                userDataUpdate.enqueueUpdateTask(profileUserId,
                                                        "follower", "-1", ()->{
                                                            userDataUpdate.enqueueUpdateTask(auth.getCurrentUser().getUid(),
                                                                    "following", "-1", ()->{
                                                                        profIdTrue(profileUserId);
                                                                        dialog.dismiss();
                                                                    });
                                                        });
                                            }
                                        });

                            }
                        });

                        binding.spinKitProfile.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<UserDetails> call, Throwable t) {
            }
        });
    }

    private void showLoginDialog() {
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
            Intent i = new Intent(ProfileActivity.this, Login.class);
            startActivity(i);
            finish();
        });

        dialog1.show();
    }

    private String extractLink(Uri data) {
        Toast.makeText(this, "loading request...", Toast.LENGTH_SHORT).show();
        List<String> pathSegments = data.getPathSegments();
        if (pathSegments.size() >= 1){
            String value = pathSegments.get(0);
            return value;
        }
        return "null";
    }

    private void showImage(String profileImg, String coverImage){
        binding.myProfile.setOnClickListener(c->{
            Intent i = new Intent(ProfileActivity.this, ImageShow.class);
            i.putExtra("image", profileImg);
            startActivity(i);
        });

        binding.dashboardImage.setOnClickListener(c->{
            Intent i = new Intent(ProfileActivity.this, ImageShow.class);
            i.putExtra("image", coverImage);
            startActivity(i);
        });
    }

    @SuppressLint("NewApi")
    private void deferemciateProfile(String profId) {
        if ( profId != null && profId.equalsIgnoreCase(auth.getCurrentUser().getUid())){
            binding.editBtn.setVisibility(View.VISIBLE);
            binding.dotsProfile.setVisibility(View.VISIBLE);
            binding.followBtn.setVisibility(View.GONE);


            binding.dotsProfile.setOnClickListener(v -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                builder.setTitle("Select an Action")
                        .setItems(new CharSequence[]{"Copy Profile Link", "Log Out", "View Usage"}, (dialog, which) -> {
                            switch (which) {
                                case 0: // Copy Profile Link
                                    String link = "https://i.aima.profile/" + profId;
                                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("Profile Link Copied", link);
                                    Toast.makeText(ProfileActivity.this, "Profile Link Copied", Toast.LENGTH_LONG).show();
                                    clipboard.setPrimaryClip(clip);
                                    break;

                                case 1: // Log Out
                                    LogOut();
                                    break;

                                case 2: // View Usage
                                    Toast.makeText(activity, "Under maintenance...", Toast.LENGTH_SHORT).show();
//                                    Intent i = new Intent(ProfileActivity.this, UsagesActivity.class);
//                                    startActivity(i);
                                    break;
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
//                PopupMenu popupMenu = new PopupMenu(this, binding.dotsProfile); // 'view' is the anchor view for the PopupMenu
//
//                popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());
//
//                // Check for API level before calling setForceShowIcon
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    popupMenu.setForceShowIcon(true);
//                }
//
//                Menu menu = popupMenu.getMenu();
//                menu.findItem(R.id.action_copy).setIcon(R.drawable.copy_24);
//                menu.findItem(R.id.action_stars).setIcon(R.drawable.data_usage_24);
//
//                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                    @Override
//                    public boolean onMenuItemClick(MenuItem item) {
//                        int itemId = item.getItemId();
//
//                        if (itemId == R.id.action_copy) {
//                            String link = "https://i.aima.profile/" + profId;
//                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//                            ClipData clip = ClipData.newPlainText("Profile Link Copied", link);
//                            Toast.makeText(ProfileActivity.this, "Profile Link Copied", Toast.LENGTH_LONG).show();
//                            clipboard.setPrimaryClip(clip);
//
//                        } else if (itemId == R.id.action_log_out) {
//                            LogOut();
//
//                        } else if (itemId == R.id.action_stars) {
//                            Intent i = new Intent(ProfileActivity.this, UsagesActivity.class);
//                            startActivity(i);
//                        }
//
//                        popupMenu.dismiss();
//                        return true;
//                    }
//                });
//
//                popupMenu.show();
            });



        }

        followModels = new ArrayList<>();
        binding.followersRecyclerView.showShimmerAdapter();
        database.getReference().child("users").child(profId)
                .child("follows")
                .limitToLast(30)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        followModels.clear();
                        if (snapshot.exists()){
                            for (DataSnapshot snapshot1 : snapshot.getChildren()){
                                FollowModel followModel = snapshot1.getValue(FollowModel.class);

                                if (followModel != null){
                                    followModel.setFollowBy(followModel.getFollowBy());
                                    followModels.add(followModel);
                                }
                            }
                            Collections.reverse(followModels);
                            LinearLayoutManager layoutManager = new LinearLayoutManager(ProfileActivity.this);
                            layoutManager.setOrientation(RecyclerView.HORIZONTAL);
                            binding.noFollowMessage.setVisibility(View.GONE);
                            FriendsAdapter adapter = new FriendsAdapter(followModels, ProfileActivity.this);
                            binding.followersRecyclerView.hideShimmerAdapter();
                            binding.followersRecyclerView.setLayoutManager(layoutManager);
                            binding.followersRecyclerView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        }else {
                            binding.noFollowMessage.setVisibility(View.VISIBLE);
                            binding.followersRecyclerView.hideShimmerAdapter();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    public void SocialMediaClicks(UserModel userModel){

        String fbLink = userModel.getFacebook();
        if (fbLink == null || fbLink.isEmpty()){
            binding.facebookImage.setVisibility(View.GONE);
        }else {
            binding.facebookImage.setOnClickListener(v->{
                try {
                    Intent facebookIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fbLink));
                    PackageManager packageManager = getPackageManager();
                    if (packageManager != null && facebookIntent.resolveActivity(packageManager) != null) {
                        startActivity(facebookIntent);
                    } else {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(fbLink)));
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Error opening Facebook: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }


        String whatsapp = userModel.getWhatsapp();
        if (whatsapp == null || whatsapp.isEmpty() || whatsapp.length() < 9){
            binding.whatsappImage.setVisibility(View.GONE);
        }else {
            binding.whatsappImage.setOnClickListener(v->{
                String message = "Hi, I am " + binding.dashBoardName.getText().toString()+
                        ". I get your number from *AIMA App*. Should we talk now?";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + "+91" + whatsapp + "&text=" + message));
                startActivity(intent);
            });
        }


        String phoneNo = userModel != null ? userModel.getPhone() : null;
        if (phoneNo == null || phoneNo.trim().isEmpty() || phoneNo.length() < 9) {
            binding.callImage.setVisibility(View.GONE);
        } else {
            binding.callImage.setVisibility(View.VISIBLE);
            binding.callImage.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", "+91" + phoneNo, null));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(binding.getRoot().getContext(), "Error opening dialer: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }


        String email = userModel.getEmail();
        if (email == null || email.isEmpty()){
            binding.mailImage.setVisibility(View.GONE);
        }else {
            binding.mailImage.setOnClickListener(v->{
                Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
                startActivity(i);
            });
        }

        binding.editBtn.setOnClickListener(v->{
            Intent i = new Intent(ProfileActivity.this, EditProfile.class);
            i.putExtra("userModel", userModel);
            startActivity(i);
        });
    }

    public void shareProfile(String profileUserId){
        binding.shareProfile.setOnClickListener(v->{
            Toast.makeText(this, "loading request...", Toast.LENGTH_SHORT).show();
            String text = "This profile is shared from AIMA App. Check using app only ! \n\n" +
                    "link: https://i.aima.profile/" + profileUserId;
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/html");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
            if (sharingIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(Intent.createChooser(sharingIntent,"Share using"));
            }
        });
    }
}
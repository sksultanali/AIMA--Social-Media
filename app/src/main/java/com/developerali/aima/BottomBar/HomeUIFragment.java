package com.developerali.aima.BottomBar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.os.Parcelable;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.interfaces.ItemClickListener;
import com.denzcoskun.imageslider.models.SlideModel;
import com.developerali.aima.Activities.CheckMapActivity;
import com.developerali.aima.Activities.DonationPage;
import com.developerali.aima.Activities.MemberShip_Act;
import com.developerali.aima.Activities.NotificationAct;
import com.developerali.aima.Activities.PostActivity;
import com.developerali.aima.Activities.ProfileActivity;
import com.developerali.aima.Activities.SearchActivity;
import com.developerali.aima.Activities.SearchActivity2;
import com.developerali.aima.Helpers.Helper;
import com.developerali.aima.Helpers.TextUtils;
import com.developerali.aima.Helpers.UserDataUpdate;
import com.developerali.aima.HomeBottomBar.AimaAdmin;
import com.developerali.aima.HomeBottomBar.AimaGallery;
import com.developerali.aima.HomeBottomBar.AimaPdfs;
import com.developerali.aima.HomeBottomBar.AimaPosts;
import com.developerali.aima.HomeBottomBar.AimaVideos;
import com.developerali.aima.Model_Apis.ApiService;
import com.developerali.aima.Model_Apis.BoundaryData;
import com.developerali.aima.Model_Apis.CountResponse;
import com.developerali.aima.Model_Apis.MapPointerResponse;
import com.developerali.aima.Model_Apis.RetrofitClient;
import com.developerali.aima.Model_Apis.UserDetails;
import com.developerali.aima.Models.BannerModel;
import com.developerali.aima.Models.UserModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.DialogNotLoginBinding;
import com.developerali.aima.databinding.FragmentHomeUIBinding;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import me.ibrahimsn.lib.SmoothBottomBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeUIFragment extends Fragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    FragmentHomeUIBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    Boolean isOpen;
    private SmoothBottomBar bottomBar;
    Uri imageUri;
    UserDataUpdate userDataUpdate;
    ApiService apiService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeUIBinding.inflate(inflater, container, false);
        Helper.changeStatusBarColor(getActivity(), R.color.white);
        bottomBar = getActivity().findViewById(R.id.bottomBar);
        bottomBar.setItemActiveIndex(0);
        userDataUpdate = new UserDataUpdate();
        apiService = RetrofitClient.getClient().create(ApiService.class);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        isOpen = false;
        checkAndAddPointers();

        //ImageSlider
        final ArrayList<BannerModel> links = new ArrayList<>();
        database.getReference().child("homeBanners")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            for (DataSnapshot ds:snapshot.getChildren()){
                                BannerModel bannerModel = ds.getValue(BannerModel.class);
                                if (ds.child("link").exists()){
                                    bannerModel.setLink(ds.child("link").getValue(String.class));
                                }
                                if (ds.child("message").exists()){
                                    bannerModel.setMessage(ds.child("message").getValue(String.class));
                                }
                                links.add(bannerModel);
                            }

                            try {
                                if (links == null || links.isEmpty()) {
                                    binding.bannerLayout.setVisibility(View.GONE);
                                    return;
                                }else {
                                    binding.bannerLayout.setVisibility(View.VISIBLE);
                                }
                                binding.viewFlipper.removeAllViews();
                                for (BannerModel bannerModel : links) {
                                    ImageView imageView = new ImageView(getActivity());
                                    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                                    if (!getActivity().isDestroyed()){
                                        Glide.with(getActivity())
                                                .load(bannerModel.getLink())
                                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                                .placeholder(R.drawable.placeholder) // Optional placeholder
                                                .into(imageView);
                                    }

                                    imageView.setOnClickListener(view -> {
                                        int currentIndex = binding.viewFlipper.getDisplayedChild(); // Get current displayed child index
                                        BannerModel activeBanner = links.get(currentIndex);
                                        if (activeBanner.getMessage() == null && activeBanner.getLink() == null){
                                            Toast.makeText(getActivity(), "No link attached!", Toast.LENGTH_SHORT).show();
                                        }else {
                                            showBannerDialog(activeBanner.getMessage(), activeBanner.getLink());
                                        }
                                    });
                                    binding.viewFlipper.addView(imageView);
                                }

                                binding.viewFlipper.setFlipInterval(5000);
                                binding.viewFlipper.startFlipping();

                            } catch (Exception e) {
                                e.printStackTrace();
                                binding.viewFlipper.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //getting user information
        if (auth.getCurrentUser() != null){
            String myId = auth.getCurrentUser().getUid();
            Call<UserDetails> call = apiService.getUserDetails(
                    "getUserDetails", myId
            );

            call.enqueue(new Callback<UserDetails>() {
                @Override
                public void onResponse(Call<UserDetails> call, Response<UserDetails> response) {
                    if (response.isSuccessful() && response.body() != null){
                        UserDetails apiResponse = response.body();
                        if (apiResponse.getStatus().equalsIgnoreCase("success")){
                            UserModel userModel = apiResponse.getData();
                            Helper.userDetails = userModel;
                            Helper.saveUserDetailsToSharedPref(getActivity(), userModel);

                            if (userModel.getImage() != null && !userModel.getImage().isEmpty()){
                                Glide.with(getActivity())
                                        .load(userModel.getImage())
                                        .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache both original & resized versions
                                        .placeholder(R.drawable.profileplaceholder)
                                        .into(binding.myProfile);
                            }

                            Helper.showBadge(userModel.getVerified(), userModel.getVerified_valid(), binding.verifiedProfile);

                            binding.type.setText(apiResponse.getData().getType());
                        }
                    }
                }

                @Override
                public void onFailure(Call<UserDetails> call, Throwable t) {
                }
            });
        }

        binding.postLayout.setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_from_right));
        binding.adminLayout.setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_from_left));
        binding.videosLayout.setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_from_top_left));
        binding.galeryLayout.setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up_animation));
        binding.pdfsLayout.setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_from_bottom_right));

        //toolbar clicks
        binding.myProfile.setOnClickListener(v->{
            Intent i = new Intent(getActivity().getApplicationContext(), ProfileActivity.class);
            getActivity().startActivity(i);
        });

        binding.search.setOnClickListener(v->{
            Intent i = new Intent(getActivity().getApplicationContext(), SearchActivity2.class);
            getActivity().startActivity(i);
        });

        binding.openClose.setOnClickListener(c->{
            if (isOpen) {
                binding.closeBtn.setVisibility(View.GONE);
                binding.openBtn.setVisibility(View.VISIBLE);
                binding.featuresOut.setVisibility(View.GONE);
                isOpen = false;
            }else {
                binding.openBtn.setVisibility(View.GONE);
                binding.closeBtn.setVisibility(View.VISIBLE);
                binding.featuresOut.setVisibility(View.VISIBLE);
                isOpen = true;
            }
        });

        binding.closeFeature.setOnClickListener(v->{
            binding.openClose.setVisibility(View.GONE);
        });

        binding.addDonation.setOnClickListener(v->{
            Intent intent = new Intent(getActivity().getApplicationContext(), DonationPage.class);
            getActivity().startActivity(intent);
        });

        binding.addMembership.setOnClickListener(c->{
            Intent i = new Intent(getActivity().getApplicationContext(), MemberShip_Act.class);
            getActivity().startActivity(i);
        });
        binding.addPost.setOnClickListener(c->{
            Intent i = new Intent(getActivity().getApplicationContext(), PostActivity.class);
            getActivity().startActivity(i);
        });

        binding.memberFeature.setOnClickListener(v->{
            Toast.makeText(getActivity(), "loading...", Toast.LENGTH_SHORT).show();
            binding.addMembership.performClick();
        });
        binding.donationFeature.setOnClickListener(v->{
            Toast.makeText(getActivity(), "loading...", Toast.LENGTH_SHORT).show();
            binding.addDonation.performClick();
        });

        binding.notification.setOnClickListener(v->{
            Intent intent = new Intent(getActivity().getApplicationContext(), NotificationAct.class);
            getActivity().startActivity(intent);
        });

        Call<CountResponse> call = apiService.getCounts("getCounts");
        call.enqueue(new Callback<CountResponse>() {
            @Override
            public void onResponse(Call<CountResponse> call, Response<CountResponse> response) {
                if (response.isSuccessful() && response.body() != null){
                    CountResponse countResponse = response.body();

                    if (countResponse.getStatus().equalsIgnoreCase("success")){
                        CountResponse.Data countData = countResponse.getData();
                        checkNewPosts(countData);
                    }
                }
            }

            @Override
            public void onFailure(Call<CountResponse> call, Throwable t) {

            }
        });

        binding.camera.setOnClickListener(v->{
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                // Request the necessary permissions
                ActivityCompat.requestPermissions(getActivity(), new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 100);
            }else {
                ImagePicker.with(this)
                        .crop()//Crop image(Optional), Check Customization for more option
                        .compress(1024) //Final image resolution will be less than 1080 x 1080(Optional)
                        .start(85);
            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                ExitApp();
            }
        });

        binding.postLayout.setOnClickListener(v->{
            startActivity(new Intent(getActivity().getApplicationContext(), AimaPosts.class));
        });
        binding.adminLayout.setOnClickListener(v->{
            startActivity(new Intent(getActivity().getApplicationContext(), AimaAdmin.class));
        });
        binding.pdfsLayout.setOnClickListener(v->{
            startActivity(new Intent(getActivity().getApplicationContext(), AimaPdfs.class));
        });
        binding.videosLayout.setOnClickListener(v->{
            startActivity(new Intent(getActivity().getApplicationContext(), AimaVideos.class));
        });
        binding.galeryLayout.setOnClickListener(v->{
            startActivity(new Intent(getActivity().getApplicationContext(), AimaGallery.class));
        });

        //checkNotification();
        return binding.getRoot();
    }

    private void checkAndAddPointers() {
        Call<BoundaryData> call = apiService.fetchBoundaryData(
                "fetchBoundaryData");
        call.enqueue(new Callback<BoundaryData>() {
            @Override
            public void onResponse(Call<BoundaryData> call, Response<BoundaryData> response) {
                if (response.isSuccessful() && response.body() != null){
                    BoundaryData apiResponse = response.body();
                    if (apiResponse.getStatus().equalsIgnoreCase("success")){
                        if (apiResponse.getShow().equalsIgnoreCase("yes")){
                            binding.mapLayout.setVisibility(View.VISIBLE);
                            binding.mapLayout.setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.top_to_bottom));
                            Glide.with(getActivity())
                                    .load(apiResponse.getImgData())
                                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                    .skipMemoryCache(false)
                                    .placeholder(getActivity().getDrawable(R.drawable.placeholder))
                                    .into(binding.mapImg);
                            binding.mapLayout.setOnClickListener(v->{
                                Intent i = new Intent(getActivity().getApplicationContext(), CheckMapActivity.class);
                                //i.putExtra("data", (Parcelable) apiResponse.getData());
                                getActivity().startActivity(i);
                            });
                        }else {
                            binding.mapLayout.setVisibility(View.GONE);
                            Toast.makeText(getActivity(), "List is empty! ", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        binding.mapLayout.setVisibility(View.GONE);
                        Toast.makeText(getActivity(), apiResponse.getStatus(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<BoundaryData> call, Throwable t) {
                binding.mapLayout.setVisibility(View.GONE);
                Toast.makeText(getActivity(), t.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @SuppressLint("ResourceAsColor")
    private void showBannerDialog(String message, String link) {
        DialogNotLoginBinding dialogBinding = DialogNotLoginBinding.inflate(LayoutInflater.from(getActivity()));

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(dialogBinding.getRoot())
                .create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));

        if (message != null && !message.isEmpty()){
            dialogBinding.titleText.setText("Message");
            dialogBinding.messageText.setText(Html.fromHtml(message));
        }else {
            dialogBinding.titleText.setText("Link Found");
            dialogBinding.messageText.setText("For opening attach link, click below button. It will redirect you to the linked page!");
        }

        dialogBinding.noBtn.setVisibility(View.GONE);

        if (link != null && !link.isEmpty()){
            dialogBinding.yesBtnText.setText("Open Link");
            dialogBinding.loginBtn.setOnClickListener(v->{
                dialog.dismiss();
                Helper.openLink(getActivity(), link);
            });
        }else {
            dialogBinding.yesBtnText.setText("Okay");
            dialogBinding.loginBtn.setOnClickListener(v->{
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void checkNewPosts(CountResponse.Data countData) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        int publicPosts = sharedPreferences.getInt("PublicPosts", 0);
        int adminPosts = sharedPreferences.getInt("AdminPosts", 0);
        int videos = sharedPreferences.getInt("Videos", 0);

        int newPublicPost = countData.getPublicPosts() - publicPosts;
        int newAdminPost = countData.getAdminPosts() - adminPosts;
        int newVideoPost = countData.getVideos() - videos;

        if (newPublicPost > 0){
            binding.postIcon.setVisibility(View.VISIBLE);
            binding.publicCounter.setVisibility(View.VISIBLE);
            TextUtils.startCounter(newPublicPost, binding.publicCounter, "", "+");
        }else {
            binding.publicCounter.setVisibility(View.INVISIBLE);
            binding.postIcon.setVisibility(View.GONE);
        }

        if (newAdminPost > 0){
            binding.adminNewIcon.setVisibility(View.VISIBLE);
            binding.adminCounter.setVisibility(View.VISIBLE);
            TextUtils.startCounter(newAdminPost, binding.adminCounter, "", "+");
        }else {
            binding.adminCounter.setVisibility(View.INVISIBLE);
            binding.adminNewIcon.setVisibility(View.GONE);
        }

        if (newVideoPost > 0){
            binding.videoIcon.setVisibility(View.VISIBLE);
            binding.videosCounter.setVisibility(View.VISIBLE);
            TextUtils.startCounter(newVideoPost, binding.videosCounter, "", "+");
        }else {
            binding.videosCounter.setVisibility(View.INVISIBLE);
            binding.videoIcon.setVisibility(View.GONE);
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getData() != null && requestCode == 85){
            Toast.makeText(getActivity(), "please wait...", Toast.LENGTH_SHORT).show();
            imageUri = data.getData();
            Intent intent = new Intent(getActivity().getApplicationContext(), PostActivity.class);
            intent.putExtra("uri", imageUri.toString());
            getActivity().startActivity(intent);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can proceed with the camera action
                ImagePicker.with(this)
                        .crop()//Crop image(Optional), Check Customization for more option
                        .compress(1024)			//Final image size will be less than 3 MB(Optional)//Final image resolution will be less than 1080 x 1080(Optional)
                        .start(85);
            } else {
                // Permission denied, handle accordingly
                Toast.makeText(getActivity(), "Camera and Storage permission are required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void checkNotification(){
        database.getReference().child("notification")
                .child(auth.getCurrentUser().getUid())
                .orderByChild("notifyAt")
                .limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            for (DataSnapshot snapshot1 : snapshot.getChildren()){

                                Boolean seen = snapshot1.child("seen").getValue(Boolean.class);
                                if (!seen){
                                    binding.notificationBadge.setVisibility(View.VISIBLE);
                                }

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void ExitApp(){
        DialogNotLoginBinding dialogNotLoginBinding = DialogNotLoginBinding.inflate(getLayoutInflater());
        Dialog dialog1 = new Dialog(getActivity());
        dialog1.setContentView(dialogNotLoginBinding.getRoot());

        dialog1.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog1.getWindow().setGravity(Gravity.CENTER_VERTICAL);

        dialog1.setCancelable(false);
        dialog1.setCanceledOnTouchOutside(false);

        dialogNotLoginBinding.titleText.setText("Leave App");
        dialogNotLoginBinding.messageText.setText("Are you sure you want to quit this app?");
        dialogNotLoginBinding.yesBtnText.setText("Yes");
        dialogNotLoginBinding.noBtn.setVisibility(View.VISIBLE);

        dialogNotLoginBinding.loginBtn.setOnClickListener(c->{
            getActivity().finish();
        });

        dialogNotLoginBinding.noBtn.setOnClickListener(c->{
            dialog1.dismiss();
        });

        dialog1.show();
    }

}
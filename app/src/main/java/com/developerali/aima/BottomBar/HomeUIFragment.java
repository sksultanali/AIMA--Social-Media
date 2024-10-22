package com.developerali.aima.BottomBar;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developerali.aima.Activities.DonationPage;
import com.developerali.aima.Activities.MemberShip_Act;
import com.developerali.aima.Activities.NotificationAct;
import com.developerali.aima.Activities.PostActivity;
import com.developerali.aima.Activities.ProfileActivity;
import com.developerali.aima.Activities.SearchActivity;
import com.developerali.aima.HomeBottomBar.AimaAdmin;
import com.developerali.aima.HomeBottomBar.AimaGallery;
import com.developerali.aima.HomeBottomBar.AimaPdfs;
import com.developerali.aima.HomeBottomBar.AimaPosts;
import com.developerali.aima.HomeBottomBar.AimaVideos;
import com.developerali.aima.Models.UserModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.DialogNotLoginBinding;
import com.developerali.aima.databinding.FragmentHomeUIBinding;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import me.ibrahimsn.lib.SmoothBottomBar;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeUIBinding.inflate(inflater, container, false);
        bottomBar = getActivity().findViewById(R.id.bottomBar);
        bottomBar.setItemActiveIndex(0);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        isOpen = false;

        //getting user information
//        database.getReference().child("users").child(auth.getCurrentUser().getUid())
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if (snapshot.exists() && getActivity() != null){
//                            UserModel userModel = snapshot.getValue(UserModel.class);
//                            if (userModel.getImage() != null && !getActivity().isDestroyed()){
//                                Glide.with(getActivity())
//                                        .load(userModel.getImage())
//                                        .placeholder(getActivity().getDrawable(R.drawable.profileplaceholder))
//                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
//                                        .into(binding.myProfile);
//                            }
//                            long verifiedValid = userModel.getVerifiedValid();
//                            if (verifiedValid < new Date().getTime() && userModel.isVerified()){
//                                database.getReference().child("users").child(auth.getCurrentUser().getUid())
//                                        .child("verified")
//                                        .setValue(false);
//                                database.getReference().child("users").child(auth.getCurrentUser().getUid())
//                                        .child("verifiedValid").removeValue();
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                });


        //toolbar clicks
        binding.myProfile.setOnClickListener(v->{
            Intent i = new Intent(getActivity().getApplicationContext(), ProfileActivity.class);
            getActivity().startActivity(i);
        });

        binding.search.setOnClickListener(v->{
            Intent i = new Intent(getActivity().getApplicationContext(), SearchActivity.class);
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
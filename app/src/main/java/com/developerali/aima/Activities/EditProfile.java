
package com.developerali.aima.Activities;


import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developerali.aima.CommonFeatures;
import com.developerali.aima.MainActivity;
import com.developerali.aima.Models.UserModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ActivityEditProfileBinding;
import com.developerali.aima.databinding.CommentBottomNavigationBinding;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


public class EditProfile extends AppCompatActivity {

    ActivityEditProfileBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ProgressDialog progressDialog;
    Uri selectedImageUri;
    String imageUrl;
    FirebaseStorage storage;
    Activity activity;
    UserModel userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        CommonFeatures.lowerColour(getWindow(), getResources());
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        activity = EditProfile.this;

        userModel = getIntent().getParcelableExtra("userModel");

        if (userModel.getName() != null && !userModel.getName().isEmpty()){
            binding.dashName.setText(userModel.getName());
        }else {
            binding.dashName.setText("update name :(");
        }

        if (userModel.getBio() != null && !userModel.getBio().isEmpty()){
            binding.bioName.setText(userModel.getBio());
        }else if (userModel.getBio() == null){
            binding.bioName.setText("update bio :(");
        }

        if (userModel.getAbout() != null && !userModel.getAbout().isEmpty()){
            binding.dashAbout.setText(userModel.getAbout());
        }else {
            binding.dashAbout.setText("update about :(");
        }
        if (userModel.getFacebook() != null && !userModel.getFacebook().isEmpty()){
            binding.fbLink.setText(userModel.getFacebook());
        }else {
            binding.fbLink.setText("update facebook link :(");
        }
        if (userModel.getPhone() != null && !userModel.getPhone().isEmpty()){
            binding.phoneNoText.setText("+91 " + userModel.getPhone());
        }else {
            binding.phoneNoText.setText("update phone number :(");
        }
        if (userModel.getWhatsapp() != null && !userModel.getWhatsapp().isEmpty()){
            binding.whatsappLink.setText("+91 " + userModel.getWhatsapp());
        }else {
            binding.whatsappLink.setText("update whatsapp number :(");
        }

        if (userModel.getImage() != null && !activity.isDestroyed()){
            Glide.with(EditProfile.this)
                    .load(userModel.getImage())
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .placeholder(getDrawable(R.drawable.profileplaceholder))
                    .into(binding.myProfile);
        }

        if (userModel.getCover() != null && !activity.isDestroyed()){
            Glide.with(EditProfile.this)
                    .load(userModel.getCover())
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .placeholder(getDrawable(R.drawable.profileback))
                    .into(binding.dashboardImage);
        }


        progressDialog = new ProgressDialog(EditProfile.this);
        progressDialog.setMessage("updating profile");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        binding.editDashboard.setOnClickListener(v->{
            finish();
        });


        binding.coverUpload.setOnClickListener(v->{
            if (ContextCompat.checkSelfPermission(EditProfile.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(EditProfile.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                // Request the necessary permissions
                ActivityCompat.requestPermissions(EditProfile.this, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 100);
            }else{
                ImagePicker.with(this)
                        .crop()	    			//Crop image(Optional), Check Customization for more option
                        .compress(1024)			//Final image size will be less than 3 MB(Optional)
                        .start(25);
            }
        });

        binding.myProfile.setOnClickListener(v->{
            if (ContextCompat.checkSelfPermission(EditProfile.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(EditProfile.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                // Request the necessary permissions
                ActivityCompat.requestPermissions(EditProfile.this, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 100);
            }else{
                ImagePicker.with(this)
                        .crop()	    			//Crop image(Optional), Check Customization for more option
                        .compress(1024)			//Final image size will be less than 3 MB(Optional)
                        .maxResultSize(512, 512)	//Final image resolution will be less than 1080 x 1080(Optional)
                        .start(75);
            }

        });

        binding.editName.setOnClickListener(v->{
            showBottomBar("name", binding.dashName);
        });
        binding.editBio.setOnClickListener(v->{
            showBottomBar("bio", binding.bioName);
        });
        binding.editAbout.setOnClickListener(v->{
            showBottomBar("about", binding.dashAbout);
        });
        binding.editFb.setOnClickListener(v->{
            showBottomBar("facebook", binding.fbLink);
        });
        binding.editWhatsapp.setOnClickListener(v->{
            showBottomBar("whatsapp", binding.whatsappLink);
        });
        binding.editPhone.setOnClickListener(v->{
            showBottomBar("phone", binding.phoneNoText);
        });

        binding.saveProfile.setOnClickListener(v->{
            Intent i = new Intent(EditProfile.this, MainActivity.class);
            startActivity(i);
            finish();
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can proceed with the camera action
                ImagePicker.with(this)
                        .crop()//Crop image(Optional), Check Customization for more option
                        .cameraOnly()
                        .compress(1024)            //Final image size will be less than 3 MB(Optional)
                        .maxResultSize(512, 512)    //Final image resolution will be less than 1080 x 1080(Optional)
                        .start(85);
            } else {
                // Permission denied, handle accordingly
                Toast.makeText(EditProfile.this, "Camera and Storage permission are required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void showBottomBar(String filed, TextView textView) {

        CommentBottomNavigationBinding dialogBinding = CommentBottomNavigationBinding.inflate(getLayoutInflater());

        // Create a new dialog and set the custom layout
        Dialog dialog = new Dialog(this);
        dialog.setContentView(dialogBinding.getRoot());
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        dialogBinding.postBtn.setEnabled(false);
        dialogBinding.commentCounter.setText(filed);
        dialogBinding.postBtn.setText("Save");

        dialogBinding.textLebel.setHint("Write here");
        dialogBinding.commentInput.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialogBinding.commentInput.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(dialogBinding.commentInput, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 200);

        if (filed.equalsIgnoreCase("whatsapp")){
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.prefix);
            dialogBinding.commentInput.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            dialogBinding.commentInput.setCompoundDrawablePadding(8);
        }
        if (filed.equalsIgnoreCase("phone")){
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.prefix);
            dialogBinding.commentInput.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            dialogBinding.commentInput.setCompoundDrawablePadding(8);
        }

        dialogBinding.commentInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 1){
                    dialogBinding.postBtn.setEnabled(true);
                    dialogBinding.postBtn.setBackground(getDrawable(R.drawable.button_follow_background));
                }else {
                    dialogBinding.postBtn.setEnabled(false);
                    dialogBinding.postBtn.setBackground(getDrawable(R.drawable.button_already_followd));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        dialogBinding.postBtn.setOnClickListener(v -> {
            progressDialog.show();
            String value = dialogBinding.commentInput.getText().toString();
            textView.setText(value);
            database.getReference().child("users")
                    .child(userModel.getUserId())
                    .child(filed)
                    .setValue(value)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            progressDialog.dismiss();
                            dialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            dialog.dismiss();
                        }
                    });
        });

        dialog.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getData() != null && requestCode == 25){
            selectedImageUri = data.getData();
            binding.dashboardImage.setImageURI(selectedImageUri);
            progressDialog.show();
            uploadingImage(selectedImageUri,  "cover", "cover");
        }
        if (data != null && data.getData() != null && requestCode == 75){
            selectedImageUri = data.getData();
            binding.myProfile.setImageURI(selectedImageUri);
            progressDialog.show();
            uploadingImage(selectedImageUri, "profiles", "image");
        }
    }

    public void uploadingImage(Uri selectedUri, String childName, String type){
        StorageReference reference = storage.getReference().child(childName)
                .child(auth.getCurrentUser().getUid());
        reference.putFile(selectedUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){
                    reference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()){
                                imageUrl = task.getResult().toString();
                                profileUpdate(type, imageUrl);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(EditProfile.this, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditProfile.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void profileUpdate(String childNode, String url){
        database.getReference().child("users")
                .child(auth.getCurrentUser().getUid())
                .child(childNode)
                .setValue(url)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        if (activity != null){
                            Toast.makeText(EditProfile.this, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}

package com.developerali.aima.Activities;


import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
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
import com.developerali.aima.Helpers.CommonFeatures;
import com.developerali.aima.Helpers.UserDataUpdate;
import com.developerali.aima.MainActivity;
import com.developerali.aima.Model_Apis.ApiService;
import com.developerali.aima.Model_Apis.ImageUploadResponse;
import com.developerali.aima.Model_Apis.RetrofitClient;
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
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


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
    UserDataUpdate userDataUpdate = new UserDataUpdate();

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
            binding.dashName.setText("Update name :(");
        }

        if (userModel.getBio() != null && !userModel.getBio().isEmpty()){
            binding.bioName.setText(userModel.getBio());
        }else if (userModel.getBio() == null){
            binding.bioName.setText("Update bio :(");
        }

        if (userModel.getAbout() != null && !userModel.getAbout().isEmpty()){
            binding.dashAbout.setText(userModel.getAbout());
        }else {
            binding.dashAbout.setText("Update about :(");
        }
        if (userModel.getFacebook() != null && !userModel.getFacebook().isEmpty()){
            binding.fbLink.setText(userModel.getFacebook());
        }else {
            binding.fbLink.setText("Update facebook link :(");
        }
        if (userModel.getPhone() != null && !userModel.getPhone().isEmpty()){
            binding.phoneNoText.setText("+91 " + userModel.getPhone());
        }else {
            binding.phoneNoText.setText("Update phone number :(");
        }
        if (userModel.getWhatsapp() != null && !userModel.getWhatsapp().isEmpty()){
            binding.whatsappLink.setText("+91 " + userModel.getWhatsapp());
        }else {
            binding.whatsappLink.setText("Update whatsapp number :(");
        }

        if (userModel.getToken() == null){
            binding.tokenText.setText("Token expired!");
            binding.tokenText.setTextColor(getColor(R.color.red_colour));
            binding.tokenText.setAnimation(AnimationUtils.loadAnimation(EditProfile.this, R.anim.blink));
        }else {
            if (userModel.getToken().equalsIgnoreCase("NA")){
                binding.tokenText.setText("Token expired!");
                binding.tokenText.setTextColor(getColor(R.color.red_colour));
                binding.tokenText.setAnimation(AnimationUtils.loadAnimation(EditProfile.this, R.anim.blink));
            }else {
                binding.tokenText.setText("No issue found!");
                binding.tokenText.setTextColor(getColor(R.color.black));
            }
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
                        .compress(200)			//Final image size will be less than 3 MB(Optional)
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
                        .compress(200)			//Final image size will be less than 3 MB(Optional)
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


        binding.updateBn.setOnClickListener(v->{
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.developerali.aima"));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        });

        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            Long versionCode = (long) packageInfo.versionCode;
            String versionName = packageInfo.versionName;
            binding.textUpdate.setText("App Version : " + versionName + "(" + versionCode + ")");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        binding.tokenBn.setOnClickListener(v->{
            progressDialog.setMessage("Creating new token for you...");
            progressDialog.show();
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String token = task.getResult();
                            userDataUpdate.enqueueUpdateTask(auth.getCurrentUser().getUid(), "token", token, ()->{
                                progressDialog.dismiss();
                                Toast.makeText(activity, "Updated!", Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
        });

//        binding.saveProfile.setOnClickListener(v->{
//            Intent i = new Intent(EditProfile.this, MainActivity.class);
//            startActivity(i);
//            finish();
//        });

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
                        .compress(200)            //Final image size will be less than 3 MB(Optional)
                        .maxResultSize(512, 512)    //Final image resolution will be less than 1080 x 1080(Optional)
                        .start(25);
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
        //dialogBinding.commentCounter.setText(filed.substring(0, 1).toUpperCase() + filed.substring(1, filed.length()));
        //dialogBinding.commentCounter.setVisibility(View.GONE);
        dialogBinding.postBtn.setText("Save");

        String label = filed.substring(0, 1).toUpperCase() + filed.substring(1, filed.length());
        dialogBinding.textLebel.setHint(label);
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
            userDataUpdate.enqueueUpdateTask(auth.getCurrentUser().getUid(), filed, value, ()->{
                progressDialog.dismiss();
                dialog.dismiss();
            });
//            database.getReference().child("users")
//                    .child(userModel.getUserId())
//                    .child(filed)
//                    .setValue(value)
//                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void unused) {
//                            progressDialog.dismiss();
//                            dialog.dismiss();
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            progressDialog.dismiss();
//                            dialog.dismiss();
//                        }
//                    });
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
            //uploadingImage(selectedImageUri,  "cover", "cover");
            uploadImage(selectedImageUri, "cover");
        }
        if (data != null && data.getData() != null && requestCode == 75){
            selectedImageUri = data.getData();
            binding.myProfile.setImageURI(selectedImageUri);
            progressDialog.show();
            uploadImage(selectedImageUri, "image");
            //uploadingImage(selectedImageUri, "profiles", "image");
        }
    }

    private void uploadImage(Uri imageUri, String fieldName) {
        try {
            File file = uriToFile(imageUri);
            if (file != null) {
                uploadImageFile(file, fieldName);
            } else {
                Toast.makeText(EditProfile.this, "Failed to get file from URI", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(EditProfile.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageFile(File file, String fieldName) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        Call<ImageUploadResponse> call = apiService.uploadImage("uploadImage", fieldName, body);
        call.enqueue(new Callback<ImageUploadResponse>() {
            @Override
            public void onResponse(Call<ImageUploadResponse> call, Response<ImageUploadResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ImageUploadResponse imageResponse = response.body();
                    if (imageResponse.getStatus().equalsIgnoreCase("success")) {
                        //Toast.makeText(PostActivity.this, "Uploaded: " + imageResponse.getData().getUrl(), Toast.LENGTH_SHORT).show();
                        String imageUrl = imageResponse.getData().getUrl();
                        progressDialog.setMessage("updating profile changes....");
                        userDataUpdate.enqueueUpdateTask(auth.getCurrentUser().getUid(), fieldName, imageUrl, ()->{
                            progressDialog.dismiss();
                        });
                    }else {
                        progressDialog.dismiss();
                        Toast.makeText(EditProfile.this, "Not Uploaded: " + imageResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    //progressDialog.incrementProgressBy(1);
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(EditProfile.this, "Upload failed.", Toast.LENGTH_SHORT).show();
                }

//                Log.d("UploadDebug", "File name: " + file.getName());
//                Log.d("UploadDebug", "File size: " + file.length());
//                Log.d("UploadDebug", "File path: " + file.getAbsolutePath());
//
//                Log.d("UploadResponse", "Response code: " + response.code());
//                Log.d("UploadResponse", "Response body: " + response.body());
//                Log.d("UploadResponse", "Error body: " + response.errorBody());
            }

            @Override
            public void onFailure(Call<ImageUploadResponse> call, Throwable t) {
                t.printStackTrace();
                progressDialog.dismiss();
                Toast.makeText(EditProfile.this, "Upload error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File uriToFile(Uri uri) throws IOException {
        File file = null;
        if ("content".equals(uri.getScheme())) {
            try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                String fileName = getFileName(uri);
                File cacheFile = new File(getCacheDir(), fileName);
                try (OutputStream outputStream = new FileOutputStream(cacheFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, length);
                    }
                }
                file = cacheFile;
            }
        } else if ("file".equals(uri.getScheme())) {
            file = new File(uri.getPath());
        }

        return file;
    }

    private String getFileName(Uri uri) {
        String fileName = null;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (nameIndex != -1 && cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex);
            }
            cursor.close();
        }
        if (fileName == null) {
            fileName = uri.getLastPathSegment();
        }
        return fileName;
    }

//    public void uploadingImage(Uri selectedUri, String childName, String type){
//        StorageReference reference = storage.getReference().child(childName)
//                .child(auth.getCurrentUser().getUid());
//        reference.putFile(selectedUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                if (task.isSuccessful()){
//                    reference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Uri> task) {
//                            if (task.isSuccessful()){
//                                imageUrl = task.getResult().toString();
//                                profileUpdate(type, imageUrl);
//                            }
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Toast.makeText(EditProfile.this, e.toString(), Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(EditProfile.this, e.toString(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

//    public void profileUpdate(String childNode, String url){
//        database.getReference().child("users")
//                .child(auth.getCurrentUser().getUid())
//                .child(childNode)
//                .setValue(url)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void unused) {
//                        progressDialog.dismiss();
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        progressDialog.dismiss();
//                        if (activity != null){
//                            Toast.makeText(EditProfile.this, e.toString(), Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//    }
}
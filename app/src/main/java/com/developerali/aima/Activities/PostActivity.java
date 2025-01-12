package com.developerali.aima.Activities;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developerali.aima.Helpers.Helper;
import com.developerali.aima.Helpers.UserDataUpdate;
import com.developerali.aima.MainActivity;
import com.developerali.aima.Model_Apis.ApiResponse;
import com.developerali.aima.Model_Apis.ApiService;
import com.developerali.aima.Model_Apis.ImageUploadResponse;
import com.developerali.aima.Model_Apis.RetrofitClient;
import com.developerali.aima.Models.PostModel;
import com.developerali.aima.Models.UserModel;
import com.developerali.aima.Models.VideoModel;
import com.developerali.aima.Models.shortsModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ActivityPostBinding;
import com.developerali.aima.databinding.DialogLinkInsertBinding;
import com.developerali.aima.databinding.DialogNotLoginBinding;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostActivity extends AppCompatActivity {

    ActivityPostBinding binding;
    Uri imageUri;
    FirebaseDatabase database;
    FirebaseStorage storage;
    String link;
    FirebaseAuth auth;
    ProgressDialog dialog;
    FirebaseFirestore firebaseFirestore;
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        apiService = RetrofitClient.getClient().create(ApiService.class);

        if (auth.getCurrentUser().getUid() != null){
            if (Helper.userDetails != null){
                if (Helper.userDetails.getImage() != null && !Helper.userDetails.getImage().isEmpty()){
                    Glide.with(getApplicationContext())
                            .load(Helper.userDetails.getImage())
                            .placeholder(getDrawable(R.drawable.profileplaceholder))
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .into(binding.uploaderImage);
                }
                binding.uploaderName.setText(Helper.userDetails.getName());
                binding.uploaderType.setText(Helper.userDetails.getType());
            }
        }else {
            showNotLoginDialog();
        }


        dialog = new ProgressDialog(PostActivity.this);
        dialog.setTitle("Post Uploading");
        dialog.setMessage("loading...");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        Intent intent = getIntent();
        //Handle text data
        if (intent.hasExtra(Intent.EXTRA_TEXT)) {
            String sharedFromAnotherApp = intent.getStringExtra(Intent.EXTRA_TEXT);
            binding.uploadContent.setText(sharedFromAnotherApp);
            if (sharedFromAnotherApp.length() >= 50){
                binding.postBtn.setEnabled(true);
                binding.postBtn.setBackground(getDrawable(R.drawable.bg_main_background_corner));
                binding.postBtn.setTextColor(getColor(R.color.white));
            }
        }

        // Handle image data
        if (intent.hasExtra(Intent.EXTRA_STREAM)) {
            imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            binding.imageSeen.setImageURI(imageUri);
            binding.imageSeen.setVisibility(View.VISIBLE);
            binding.postBtn.setEnabled(true);
            binding.postBtn.setBackground(getDrawable(R.drawable.bg_main_background_corner));
            binding.postBtn.setTextColor(getColor(R.color.white));
        }

        if (intent.getStringExtra("uri") != null){
            imageUri = Uri.parse(intent.getStringExtra("uri"));
            binding.imageSeen.setImageURI(imageUri);
            binding.imageSeen.setVisibility(View.VISIBLE);
            binding.postBtn.setEnabled(true);
            binding.postBtn.setBackground(getDrawable(R.drawable.bg_main_background_corner));
            binding.postBtn.setTextColor(getColor(R.color.white));
        }

        binding.uploadContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence count, int i, int i1, int i2) {
                if (count.toString().length() >= 5){
                    binding.postBtn.setEnabled(true);
                    binding.postBtn.setBackground(getDrawable(R.drawable.bg_main_background_corner));
                    binding.postBtn.setTextColor(getColor(R.color.white));
                }else {
                    binding.postBtn.setEnabled(false);
                    binding.postBtn.setBackground(getDrawable(R.drawable.react_background));
                    binding.postBtn.setTextColor(getColor(R.color.black));
                }

                binding.textCount.setText(count.toString().length() + "/2500");
                if (count.toString().length() > 2450){
                    binding.uploadContent.setError("limit exceeding");
                    binding.textCount.setTextColor(getColor(R.color.red_colour));
                }else {
                    binding.textCount.setTextColor(getColor(R.color.black));
                    binding.uploadContent.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        binding.uploadImage.setOnClickListener(v->{
            if (ContextCompat.checkSelfPermission(PostActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(PostActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                // Request the necessary permissions
                ActivityCompat.requestPermissions(PostActivity.this, new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 100);
            }else{
                ImagePicker.with(this)
                        .crop()	    			//Crop image(Optional), Check Customization for more option
                        .compress(100)			//Final image size will be less than 3 MB(Optional)
                        .maxResultSize(512, 512)	//Final image resolution will be less than 1080 x 1080(Optional)
                        .start(85);
            }
        });

        String key = Helper.dateKey(Helper.LongToLocalDate(new Date().getTime())) + auth.getCurrentUser().getUid();

        binding.uploadShorts.setOnClickListener(v->{
            showLinkInsertDialog("shorts");
        });

        binding.uploadVideo.setOnClickListener(v->{
            showLinkInsertDialog("video");
//            firebaseFirestore.collection("video")
//                    .document(key)
//                    .get()
//                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                        @Override
//                        public void onSuccess(DocumentSnapshot documentSnapshot) {
//                            if (documentSnapshot.exists()){
//                                Helper.showAlertNoAction(PostActivity.this, "Limit Exceed !",
//                                        "You can post only once a day. Please try tomorrow!", "Okay");
//                            }else {
//
//                            }
//                        }
//                    });
        });

//        binding.postBtn.setEnabled(true);
        
        binding.postBtn.setOnClickListener(v->{
            dialog.show();
            if (imageUri != null){
                dialog.setMessage("Image upload processing");
                uploadImage(imageUri);
            }else {
                uploadPost(null);
            }
//            firebaseFirestore.collection("post")
//                    .document(key)
//                    .get()
//                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                        @Override
//                        public void onSuccess(DocumentSnapshot documentSnapshot) {
//                            if (documentSnapshot.exists()){
//                                Helper.showAlertNoAction(PostActivity.this, "Limit Exceed !",
//                                        "You can post only once a day. Please try tomorrow!", "Okay");
//                            }else {
//
//                            }
//                        }
//                    });
        });

        binding.profileDashboard.setOnClickListener(v->{
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
                        .compress(100)            //Final image size will be less than 3 MB(Optional)
                        .maxResultSize(512, 512)    //Final image resolution will be less than 1080 x 1080(Optional)
                        .start(85);
            } else {
                // Permission denied, handle accordingly
                Toast.makeText(PostActivity.this, "Camera and Storage permission are required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showLinkInsertDialog(String name) {
        DialogLinkInsertBinding dialogLinkInsertBinding = DialogLinkInsertBinding.inflate(getLayoutInflater());
        Dialog dialog1 = new Dialog(PostActivity.this);
        dialog1.setContentView(dialogLinkInsertBinding.getRoot());
        dialog1.getWindow().setGravity(Gravity.CENTER_VERTICAL);
        dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog1.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);


        dialogLinkInsertBinding.submitBtn.setOnClickListener(v->{
            link = dialogLinkInsertBinding.linkInsert.getText().toString();

            if (!link.isEmpty()){

                if (link.contains("https://youtu.be/") || link.contains("https://www.youtube.com/shorts/") ||
                                link.contains("https://youtube.com/") || link.contains("https://youtube.com/")){

                    String youTubeId = extractVideoIdFromYouTubeLink(link);
                    String key = Helper.dateKey(Helper.LongToLocalDate(new Date().getTime())) + auth.getCurrentUser().getUid();
                    dialogLinkInsertBinding.submitBtn.setEnabled(false);

                    if (name.equalsIgnoreCase("shorts")){
                        shortsModel shortsModel = new shortsModel();
                        shortsModel.setVideoLink(youTubeId);
                        shortsModel.setTime(new Date().getTime());
                        shortsModel.setUploader(auth.getCurrentUser().getUid());

                        dialog1.dismiss();
                        dialog.show();

                        database.getReference().child("shorts").child(key)
                                .setValue(shortsModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(PostActivity.this, "posted short video.", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                        Intent i = new Intent(PostActivity.this, MainActivity.class);
                                        startActivity(i);
                                        finish();
                                    }
                                });
                    }

                    if (name.equalsIgnoreCase("video")){
                        //                        VideoModel videoModel = new VideoModel();
//                        videoModel.setVideoId(youTubeId);
//                        videoModel.setTime(new Date().getTime());
//                        videoModel.setApproved(false);
//                        videoModel.setUploader(auth.getCurrentUser().getUid());
//                        String description = binding.uploadContent.getText().toString();
//                        videoModel.setCaption(description);
//
//                        dialogLinkInsertBinding.submitBtn.setEnabled(false);
//                        dialogLinkInsertBinding.submitBtn.setBackground(getDrawable(R.drawable.button_already_followd));
//                        firebaseFirestore.collection("video")
//                                .document(key)
//                                .set(videoModel).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                    @Override
//                                    public void onSuccess(Void unused) {
//                                        Toast.makeText(PostActivity.this, "posted video.", Toast.LENGTH_SHORT).show();
//                                        dialog.dismiss();
//                                        Intent i = new Intent(PostActivity.this, MainActivity.class);
//                                        startActivity(i);
//                                        finish();
//                                    }
//                                });
                        String description = binding.uploadContent.getText().toString();
                        Call<ApiResponse> call = apiService.insertVideo(
                                "insertVideo", auth.getCurrentUser().getUid(), youTubeId, description
                        );

                        call.enqueue(new Callback<ApiResponse>() {
                            @Override
                            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                                if (response.isSuccessful()){
                                    ApiResponse apiResponse = response.body();
                                    if (apiResponse.getStatus().equalsIgnoreCase("success")){
                                        dialog.dismiss();
                                        Intent i = new Intent(PostActivity.this, MainActivity.class);
                                        startActivity(i);
                                        finish();
                                    }else {
                                        dialog.dismiss();
                                        Helper.showAlertNoAction(PostActivity.this,
                                                "Failed", apiResponse.getMessage(), "Okay");
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<ApiResponse> call, Throwable t) {
                                dialog.dismiss();
                                Toast.makeText(PostActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                }else {
                    Toast.makeText(this, "Please Insert YouTube Video", Toast.LENGTH_LONG).show();
                    dialogLinkInsertBinding.linkInsert.setError("Please Insert YouTube Video");
                }

            }else {
                dialogLinkInsertBinding.linkInsert.setError("Can't Empty");
            }
        });

        dialog1.show();
    }

    public static String extractVideoIdFromYouTubeLink(String youtubeLink) {
        String videoId = null;

        // Regular expression pattern for extracting video ID from YouTube link
        Pattern pattern = Pattern.compile("(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/shorts\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%2F|youtu.be%2F|%2Fv%2F|e%2F|watch%3Fv%3D|watch%3Ffeature=player_embedded&v%3D|%2Fshorts%2F)[^#\\&\\?\\n]*");
        Matcher matcher = pattern.matcher(youtubeLink);

        if (matcher.find()) {
            videoId = matcher.group();
        }

        return videoId;
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

        dialogNotLoginBinding.messageText.setText("Login is required for POST something. You can post after get logged in :)");
        dialogNotLoginBinding.loginBtn.setOnClickListener(v->{
            Intent i = new Intent(PostActivity.this, Login.class);
            startActivity(i);
            finish();
        });

        dialog1.show();
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
//        UsagesModel usagesModel = new UsagesModel("Posted Something", startTime, currentTime);
//        arrayList.add(usagesModel);
//        CommonFeatures.writeListInPref(PostActivity.this, arrayList);
//
//        super.onPause();
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getData() != null && requestCode == 85){
            binding.postBtn.setEnabled(true);
            imageUri = data.getData();
            binding.imageSeen.setVisibility(View.VISIBLE);
            binding.imageSeen.setImageURI(imageUri);
            binding.postBtn.setBackground(getDrawable(R.drawable.button_follow_background));
        }
    }

    private void uploadImage(Uri imageUri) {
        try {
            File file = uriToFile(imageUri);
            if (file != null) {
                uploadImageFile(file);
            } else {
                Toast.makeText(PostActivity.this, "Failed to get file from URI", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(PostActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadImageFile(File file) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        Call<ImageUploadResponse> call = apiService.uploadImage("uploadImage", "testing", body);
        call.enqueue(new Callback<ImageUploadResponse>() {
            @Override
            public void onResponse(Call<ImageUploadResponse> call, Response<ImageUploadResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ImageUploadResponse imageResponse = response.body();
                    if (imageResponse.getStatus().equalsIgnoreCase("success")) {
                        //Toast.makeText(PostActivity.this, "Uploaded: " + imageResponse.getData().getUrl(), Toast.LENGTH_SHORT).show();
                        String imageUrl = imageResponse.getData().getUrl();
                        dialog.setMessage("creating new post....");
                        uploadPost(imageUrl);

                    }else {
                        Toast.makeText(PostActivity.this, "Not Uploaded: " + imageResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    //progressDialog.incrementProgressBy(1);
                } else {
                    Toast.makeText(PostActivity.this, "Upload failed.", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(PostActivity.this, "Upload error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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


//    public void uploadImage(Uri imageUri){
//        dialog.show();
//        String key = database.getReference().push().getKey();
//
//
//        StorageReference reference = storage.getReference().child("uploads")
//                .child(auth.getCurrentUser().getUid()).child(key);
//
//        reference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                if (task.isSuccessful()){
//                    reference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
//                        @Override
//                        public void onComplete(@NonNull Task<Uri> task) {
//                            String imageUrl = task.getResult().toString();
//                            dialog.setMessage("creating new post....");
//                            uploadPost(imageUrl);
//
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            Toast.makeText(PostActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(PostActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    public void uploadPost(String image){
        String description = binding.uploadContent.getText().toString();
        Call<ApiResponse> call = apiService.insertPost(
                "insertPost", auth.getCurrentUser().getUid(), image, description
        );
        dialog.setMessage("almost done...");
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful()){
                    ApiResponse response1 = response.body();
                    if (response1.getStatus().equalsIgnoreCase("success")){
                        UserDataUpdate userDataUpdate = new UserDataUpdate();
                        userDataUpdate.enqueueUpdateTask(auth.getCurrentUser().getUid(), "posts", String.valueOf(1), ()->{});

                        dialog.dismiss();
                        Intent i = new Intent(PostActivity.this, MainActivity.class);
                        startActivity(i);
                        finish();
                    }else {
                        dialog.dismiss();
                        Helper.showAlertNoAction(PostActivity.this,
                                response1.getStatus(), response1.getMessage(), "Okay");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                dialog.dismiss();
                Toast.makeText(PostActivity.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
//        firebaseFirestore.collection("post")
//                .document(key)
//                .set(postModel)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void unused) {
//
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        dialog.dismiss();
//                    }
//                });



//        database.getReference().child("posts").push()
//                .setValue(postModel).addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void unused) {
//                        dialog.dismiss();
//                        Intent i = new Intent(PostActivity.this, MainActivity.class);
//                        startActivity(i);
//                        finish();
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        dialog.dismiss();
//                        Toast.makeText(PostActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });

    }
}
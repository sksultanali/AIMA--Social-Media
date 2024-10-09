package com.developerali.aima.Activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developerali.aima.Helper;
import com.developerali.aima.MainActivity;
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

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PostActivity extends AppCompatActivity {

    ActivityPostBinding binding;
    Uri imageUri;
    FirebaseDatabase database;
    FirebaseStorage storage;
    String link;
    FirebaseAuth auth;
    ProgressDialog dialog;
    FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser().getUid() != null){
            getUploaderInfo(auth.getCurrentUser().getUid());
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
                binding.postBtn.setBackground(getDrawable(R.drawable.button_follow_background));
            }
        }

        // Handle image data
        if (intent.hasExtra(Intent.EXTRA_STREAM)) {
            imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            binding.imageSeen.setImageURI(imageUri);
            binding.imageSeen.setVisibility(View.VISIBLE);
            binding.postBtn.setEnabled(true);
            binding.postBtn.setBackground(getDrawable(R.drawable.button_follow_background));
        }

        if (intent.getStringExtra("uri") != null){
            imageUri = Uri.parse(intent.getStringExtra("uri"));
            binding.imageSeen.setImageURI(imageUri);
            binding.imageSeen.setVisibility(View.VISIBLE);
            binding.postBtn.setEnabled(true);
            binding.postBtn.setBackground(getDrawable(R.drawable.button_follow_background));
        }

        binding.uploadContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence count, int i, int i1, int i2) {
                if (count.toString().length() >= 15){
                    binding.postBtn.setEnabled(true);
                    binding.postBtn.setBackground(getDrawable(R.drawable.button_follow_background));
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
            ImagePicker.with(this)
                    .crop()	    			//Crop image(Optional), Check Customization for more option
                    .compress(1024)			//Final image size will be less than 3 MB(Optional)
                    .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                    .start(85);
        });

        String key = Helper.dateKey(Helper.LongToLocalDate(new Date().getTime())) + auth.getCurrentUser().getUid();

        binding.uploadShorts.setOnClickListener(v->{
            showLinkInsertDialog("shorts");
        });

        binding.uploadVideo.setOnClickListener(v->{
            firebaseFirestore.collection("video")
                    .document(key)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()){
                                Helper.showAlertNoAction(PostActivity.this, "Limit Exceed !",
                                        "You can post only once a day. Please try tomorrow!", "Okay");
                            }else {
                                showLinkInsertDialog("video");
                            }
                        }
                    });
        });

//        binding.postBtn.setEnabled(true);
        
        binding.postBtn.setOnClickListener(v->{
            firebaseFirestore.collection("post")
                    .document(key)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()){
                                Helper.showAlertNoAction(PostActivity.this, "Limit Exceed !",
                                        "You can post only once a day. Please try tomorrow!", "Okay");
                            }else {
                                dialog.show();
                                if (imageUri != null){
                                    uploadImage(imageUri);
                                }else {
                                    uploadPost(null);
                                }
                            }
                        }
                    });
        });

        binding.profileDashboard.setOnClickListener(v->{
            finish();
        });


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
                        VideoModel videoModel = new VideoModel();
                        videoModel.setVideoId(youTubeId);
                        videoModel.setTime(new Date().getTime());
                        videoModel.setApproved(false);
                        videoModel.setUploader(auth.getCurrentUser().getUid());
                        String description = binding.uploadContent.getText().toString();
                        videoModel.setCaption(description);

                        dialogLinkInsertBinding.submitBtn.setEnabled(false);
                        dialogLinkInsertBinding.submitBtn.setBackground(getDrawable(R.drawable.button_already_followd));
                        firebaseFirestore.collection("video")
                                .document(key)
                                .set(videoModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(PostActivity.this, "posted video.", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                        Intent i = new Intent(PostActivity.this, MainActivity.class);
                                        startActivity(i);
                                        finish();
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


    public void uploadImage(Uri imageUri){

        dialog.show();

//        String key = database.getReference().push().getKey();
        String key = Helper.dateKey(Helper.LongToLocalDate(new Date().getTime())) + auth.getCurrentUser().getUid();

        StorageReference reference = storage.getReference().child("uploads")
                .child(auth.getCurrentUser().getUid()).child(key);
        reference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){
                    reference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            String imageUrl = task.getResult().toString();
                            uploadPost(imageUrl);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(PostActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PostActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getUploaderInfo(String profId){
        database.getReference("users")
                .child(profId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            UserModel userModel = snapshot.getValue(UserModel.class);

                            if (userModel != null){
                                if (userModel.getImage() != null){
                                    Glide.with(getApplicationContext())
                                            .load(userModel.getImage())
                                            .placeholder(getDrawable(R.drawable.profileplaceholder))
                                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                            .into(binding.uploaderImage);
                                }
                                binding.uploaderName.setText(userModel.getName());
                                if (userModel.getType() != null){
                                    binding.uploaderType.setText(userModel.getType());
                                }else {
                                    binding.uploaderType.setText("Public Profile");
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void uploadPost(String image){

        PostModel postModel = new PostModel();
        String description = binding.uploadContent.getText().toString();
        if (description != null){
            postModel.setCaption(description);
        }
        postModel.setUploader(auth.getCurrentUser().getUid());
        postModel.setTime(new Date().getTime());
        if (image != null){
            postModel.setImage(image);
        }
        postModel.setApproved(false);

        String key = Helper.dateKey(Helper.LongToLocalDate(new Date().getTime())) + auth.getCurrentUser().getUid();

        firebaseFirestore.collection("post")
                .document(key)
                .set(postModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        dialog.dismiss();
                        Intent i = new Intent(PostActivity.this, MainActivity.class);
                        startActivity(i);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                    }
                });



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
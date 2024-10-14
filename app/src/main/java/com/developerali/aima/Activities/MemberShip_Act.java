package com.developerali.aima.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developerali.aima.Forms.MembershipApply;
import com.developerali.aima.Helper;
import com.developerali.aima.Models.MembershipModel;
import com.developerali.aima.Models.UserModel;
import com.developerali.aima.Models.VideoModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ActivityMemberShipBinding;
import com.developerali.aima.databinding.DialogOpenChromeOrAppBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class MemberShip_Act extends AppCompatActivity {

    ActivityMemberShipBinding binding;
    Animation blinkAnimation;
    FirebaseAuth auth;
    Activity activity;
    FirebaseDatabase database;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMemberShipBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        activity = MemberShip_Act.this;

        //member message
        blinkAnimation = AnimationUtils.loadAnimation(MemberShip_Act.this, R.anim.blink);
        binding.memberMessage.setAnimation(blinkAnimation);


        database.getReference().child("member")
                .child(auth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            MembershipModel model = snapshot.getValue(MembershipModel.class);
                            if (model.getValid() < new Date().getTime()){
                                binding.memberMessage.setText("Card: "+ model.getUiNo() + "\nValidity Expired. Please Renew!");
                                binding.memberMessage.setTextColor(getColor(R.color.red_colour));
                                binding.downloadImage.setVisibility(View.GONE);

                            }else {
                                binding.memberMessage.setText("Membership Activated");
                                binding.memberMessage.setTextColor(getColor(R.color.green_colour));
                                binding.downloadImage.setVisibility(View.VISIBLE);

                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
                                String validDate = simpleDateFormat.format(model.getValid());
                                binding.valid.setText("Valid UpTo: " + validDate);

                                binding.cardHolderName.setText(model.getName());
                                binding.fatherName.setText("Father Name: " + model.getFatherName());
                                binding.cardHolderAddress.setText(model.getAddress());

                                Glide.with(MemberShip_Act.this)
                                        .load(model.getImage())
                                        .placeholder(getDrawable(R.drawable.profileplaceholder))
                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                        .into(binding.imageOnCard);

                                binding.uidNo.setText("DID: " + model.getUiNo());
                                binding.nameOnCard.setText("Name: " + model.getName().toUpperCase());
                                binding.dob.setText("DOB: " + model.getDob());



                                binding.easyFlipView.setBackground(getDrawable(R.drawable.frontcard));
                                binding.frontCard.setVisibility(View.VISIBLE);
                                binding.lottieNO.setVisibility(View.GONE);
                            }
                        }else {
                            binding.memberMessage.setText("Membership Not Activated");
                            binding.memberMessage.setTextColor(getColor(R.color.red_colour));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Toast.makeText(MemberShip_Act.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        database.getReference().child("users")
                .child(auth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            UserModel userModel = snapshot.getValue(UserModel.class);
                            if (userModel != null){
                                binding.profileName.setText(userModel.getName());
                                if (userModel.isVerified()){
                                    binding.verifiedProfile.setVisibility(View.VISIBLE);
                                }
                                if (userModel.getType() != null){
                                    binding.profileType.setText(userModel.getType());
                                }else {
                                    binding.profileType.setText("Public Profile");
                                }
                                if (userModel.getImage() != null && !activity.isDestroyed()){
                                    Glide.with(MemberShip_Act.this)
                                            .load(userModel.getImage())
                                            .placeholder(getDrawable(R.drawable.profileplaceholder))
                                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                            .into(binding.memberProfile);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.frontCard.setOnClickListener(v->{
            binding.easyFlipView.setBackground(getDrawable(R.drawable.background_white_linear));
            binding.backCard.setVisibility(View.VISIBLE);
            binding.frontCard.setVisibility(View.GONE);
        });
        binding.backCard.setOnClickListener(v->{
            binding.easyFlipView.setBackground(getDrawable(R.drawable.frontcard));
            binding.frontCard.setVisibility(View.VISIBLE);
            binding.backCard.setVisibility(View.GONE);
        });

        binding.downloadImage.setOnClickListener(v->{
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // Permission is already granted, you can now take a screenshot
                takeScreenshot(binding.backCard);
                takeScreenshot(binding.frontCard);
            } else {
                // Permission is not granted, request it
                ActivityCompat.requestPermissions(this, new String[]{
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        });

        //button clicks
        binding.memberBack.setOnClickListener(v-> finish());

        binding.newMemberForm.setOnClickListener(v->{
            //Toast.makeText(activity, "loading...", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(MemberShip_Act.this, MembershipApply.class);
            i.putExtra("value", "appliedForms");
            startActivity(i);
//            Helper.showAlertNoAction(MemberShip_Act.this,
//                    "Denied Access",
//                    "For now, we are not allowing new member through online mode. Please try after sometime.",
//                    "Okay");
        });

        binding.activeOldMember.setOnClickListener(c->{
            //showBottomBar("");
            if (Helper.isChromeCustomTabsSupported(MemberShip_Act.this)){
                Helper.openChromeTab("https://docs.google.com/forms/d/e/1FAIpQLScl5pwfKyVziZwQfHFeZQjrVTD4HT_yVZ4XRFkQoYMEfKygQQ/viewform?usp=sf_link",
                        MemberShip_Act.this);
            }
        });

        binding.raiseComplain.setOnClickListener(c->{
            //showBottomBar("");
            if (Helper.isChromeCustomTabsSupported(MemberShip_Act.this)){
                Helper.openChromeTab("https://docs.google.com/forms/d/e/1FAIpQLSfuC84vP65BacEUjSqupNePhVjbjMTEo7Vt_4E23PHrpZSIkg/viewform?usp=sf_link",
                        MemberShip_Act.this);
            }
        });

        binding.renewCard.setOnClickListener(v->{
//            Toast.makeText(activity, "loading...", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(MemberShip_Act.this, MembershipApply.class);
            i.putExtra("value", "renewForms");
            startActivity(i);
            //Toast.makeText(activity, "coming soon...", Toast.LENGTH_SHORT).show();
        });

        binding.memberProfClick.setOnClickListener(v->{
            Intent i = new Intent(MemberShip_Act.this, ProfileActivity.class);
            startActivity(i);
        });

        binding.formFillUp.setOnClickListener(v->{
            VideoModel videoModel = new VideoModel();
            Long time = 1691513124425L;
            videoModel.setVideoId("kC-uh72pAw0");    //ekhane video Id dite hobe
            videoModel.setUploader("Admin");
            videoModel.setCaption("Watch our tutorial video of form fill up.");
            videoModel.setTime(time);

            Intent intent = new Intent(MemberShip_Act.this, VideoShow.class);
            intent.putExtra("videoModel", videoModel);
            startActivity(intent);
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can now take a screenshot
                takeScreenshot(binding.backCard);
                takeScreenshot(binding.frontCard);
            } else {
                // Permission denied, show a message or take appropriate action
                Toast.makeText(MemberShip_Act.this, "Permission denied", Toast.LENGTH_SHORT).show();
                takeScreenshot(binding.backCard);
                takeScreenshot(binding.frontCard);
            }
        }
    }

    private void takeScreenshot(LinearLayout layout) {
        Bitmap screenshotBitmap = getScreenShot(layout);

        if (screenshotBitmap != null) {
            saveScreenshot(screenshotBitmap);
        } else {
            Toast.makeText(MemberShip_Act.this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap getScreenShot(View view) {
        Bitmap screenshot = null;
        try {
            if (view != null) {
                screenshot = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(screenshot);
                view.draw(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return screenshot;
    }

    private void saveScreenshot(Bitmap screenshot) {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault());
        String fileName = "AIMA_IMAGE_" + dateFormat.format(now) + ".png";

        try {
            // Save the screenshot to the device's gallery
            ContentResolver resolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");

            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

            OutputStream fos = resolver.openOutputStream(Objects.requireNonNull(imageUri));
            screenshot.compress(Bitmap.CompressFormat.PNG, 100, fos);
            if (fos != null) {
                fos.flush();
                fos.close();
            }

            Toast.makeText(MemberShip_Act.this, "Card Saved in Gallery", Toast.LENGTH_SHORT).show();

            // Optionally, share the screenshot
            //shareScreenshot(imageUri);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MemberShip_Act.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
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
//        UsagesModel usagesModel = new UsagesModel("Membership Page", startTime, currentTime);
//        arrayList.add(usagesModel);
//        CommonFeatures.writeListInPref(MemberShip_Act.this, arrayList);
//
//        super.onPause();
//    }

    public void showBottomBar(String link){

        DialogOpenChromeOrAppBinding dialogBinding = DialogOpenChromeOrAppBinding.inflate(getLayoutInflater());

        // Create a new dialog and set the custom layout
        Dialog dialog = new Dialog(this);
        dialog.setContentView(dialogBinding.getRoot());
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        dialogBinding.useChrome.setOnClickListener(v->{
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            startActivity(browserIntent);
            dialog.dismiss();
        });

        dialogBinding.useApp.setOnClickListener(v->{
            Intent i = new Intent(MemberShip_Act.this, WebViewActivity.class);
            i.putExtra("provide", link);
            startActivity(i);
            dialog.dismiss();
        });

        // Show the dialog
        dialog.show();
    }
}
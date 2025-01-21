package com.developerali.aima.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developerali.aima.Forms.MembershipApply;
import com.developerali.aima.Helpers.Helper;
import com.developerali.aima.MainActivity;
import com.developerali.aima.Model_Apis.ApiResponse;
import com.developerali.aima.Model_Apis.ApiService;
import com.developerali.aima.Model_Apis.MembershipResponse;
import com.developerali.aima.Model_Apis.RetrofitClient;
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
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MemberShip_Act extends AppCompatActivity implements PaymentResultListener {

    ActivityMemberShipBinding binding;
    Animation blinkAnimation;
    FirebaseAuth auth;
    Activity activity;
    FirebaseDatabase database;
    ApiService apiService;
    Checkout checkout;
    String validSt;
    int validTill;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    String[] validity = {"3 Months", "6 Months", "12 Months"};
    String[] price = {"60", "110", "200"};
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMemberShipBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        apiService = RetrofitClient.getClient().create(ApiService.class);

        progressDialog = new ProgressDialog(MemberShip_Act.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Image uploading...");

        checkout = new Checkout();
        checkout.setKeyID("rzp_live_0734xBSfpgbEg3");
        checkout.preload(getApplicationContext());

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        activity = MemberShip_Act.this;
        checkOldFormFill();

        //member message
        blinkAnimation = AnimationUtils.loadAnimation(MemberShip_Act.this, R.anim.blink);
        binding.memberMessage.setAnimation(blinkAnimation);

//        database.getReference().child("member")
//                .child(auth.getCurrentUser().getUid())
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        if (snapshot.exists()){
//                            try {
//                                MembershipModel model = snapshot.getValue(MembershipModel.class);
//                                if (model != null){
//
//                                }
//                            }catch (Exception e){
//                                Log.e("FirebaseError", "Error parsing data at " + snapshot.getKey(), e);
//                                Log.e("FirebaseError", "Conflicting data: " + snapshot.toString());
//                            }
//                        }else {
//                            binding.memberMessage.setText("Membership Not Activated");
//                            binding.memberMessage.setTextColor(getColor(R.color.red_colour));
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                        Toast.makeText(MemberShip_Act.this, error.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });

        if (Helper.userDetails != null){
            binding.profileName.setText(Helper.userDetails.getName());
            Helper.showBadge(Helper.userDetails.getVerified(), Helper.userDetails.getVerified_valid(), binding.verifiedProfile);
            binding.profileType.setText(Helper.userDetails.getType());

            if (Helper.userDetails.getImage() != null && !activity.isDestroyed() &&
                    !Helper.userDetails.getImage().isEmpty()){
                Glide.with(MemberShip_Act.this)
                        .load(Helper.userDetails.getImage())
                        .placeholder(getDrawable(R.drawable.profileplaceholder))
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .into(binding.memberProfile);
            }
        }

        binding.frontCard.setOnClickListener(v->{
            //binding.easyFlipView.setBackground(getDrawable(R.drawable.background_white_linear));
            binding.backCard.setVisibility(View.VISIBLE);
            binding.frontCard.setVisibility(View.GONE);
        });
        binding.backCard.setOnClickListener(v->{
            //binding.easyFlipView.setBackground(getDrawable(R.drawable.frontcard));
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
            Intent i = new Intent(MemberShip_Act.this, MembershipApplication.class);
            startActivity(i);
        });

        binding.activeOldMember.setOnClickListener(c->{
            if (Helper.isChromeCustomTabsSupported(MemberShip_Act.this)){
                Helper.openChromeTab("https://docs.google.com/forms/d/e/1FAIpQLScl5pwfKyVziZwQfHFeZQjrVTD4HT_yVZ4XRFkQoYMEfKygQQ/viewform?usp=sf_link",
                        MemberShip_Act.this);
            }
        });

        binding.raiseComplain.setOnClickListener(c->{
            if (Helper.isChromeCustomTabsSupported(MemberShip_Act.this)){
                Helper.openChromeTab("https://docs.google.com/forms/d/e/1FAIpQLSfuC84vP65BacEUjSqupNePhVjbjMTEo7Vt_4E23PHrpZSIkg/viewform?usp=sf_link",
                        MemberShip_Act.this);
            }
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

    private void checkOldFormFill() {
        Call<MembershipResponse> call = apiService.fetchMembershipByUserId("fetchMembershipByUserId", auth.getCurrentUser().getUid());
        call.enqueue(new Callback<MembershipResponse>() {
            @Override
            public void onResponse(Call<MembershipResponse> call, Response<MembershipResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MembershipResponse membershipResponse = response.body();
                    if (membershipResponse.getStatus().equalsIgnoreCase("success")) {
                        MembershipResponse.Data model = membershipResponse.getData();
                        if (model.getStatus().equalsIgnoreCase("Approved")){
                            if (model.getValid_till() < Helper.dateKey()){
                                binding.memberMessage.setText("Card: ADC-2003-1401-"+ model.getId() + "\nValidity Expired. Renew Now!");
                                binding.memberMessage.setTextColor(getColor(R.color.red_colour));
                                binding.downloadImage.setVisibility(View.GONE);

                                binding.renewCard.setOnClickListener(v->{
                                    showBottomBar();
                                });
                            }else {
                                binding.memberMessage.setText("Membership Activated");
                                binding.memberMessage.setTextColor(getColor(R.color.green_colour));
                                binding.downloadImage.setVisibility(View.VISIBLE);

                                String val = String.valueOf(model.getValid_till());
                                String validDate = Helper.formatDate("yyyyMMdd", "dd LLL yyyy", val);
                                binding.valid.setText("Valid UpTo: " + validDate);

                                binding.cardHolderName.setText(model.getName());
                                binding.fatherName.setText("Father Name: " + model.getFather_name());
                                binding.cardHolderAddress.setText(model.getAddress());

                                Glide.with(MemberShip_Act.this)
                                        .load(model.getImage())
                                        .placeholder(getDrawable(R.drawable.profileplaceholder))
                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                        .into(binding.imageOnCard);

                                binding.uidNo.setText("ADC-2003-1401-"+ model.getId());
                                binding.nameOnCard.setText("Name: " + model.getName());
                                String dob = Helper.formatDate("yyyy-MM-dd", "dd LLL yyyy", model.getDob());
                                binding.dob.setText("DOB: " + dob);

                                binding.easyFlipView.setBackground(getDrawable(R.drawable.frontcard));
                                binding.frontCard.setVisibility(View.VISIBLE);
                                binding.lottieNO.setVisibility(View.GONE);
                                
                                binding.renewCard.setOnClickListener(v->{
                                    Helper.showAlertNoAction(MemberShip_Act.this, "Validity Not Expired",
                                            "Your membership validity is not expired now. You can only renew after expiration of your membership.",
                                            "Okay");
                                });
                            }

                            
                        }else {
                            binding.memberMessage.setText(model.getStatus());
                            binding.renewCard.setOnClickListener(v->{
                                Helper.showAlertNoAction(MemberShip_Act.this, "Be Patience",
                                        "You already applied for membership, please be patience. It will take up to 48 hours.",
                                        "Okay");
                            });
                        }
//                        binding.payBtn.setEnabled(false);
//                        binding.payBtn.setText("You are already a member!");
//                        binding.payBtn.setBackground(getDrawable(R.drawable.react_background));
//                        binding.payBtn.setTextColor(getColor(R.color.black));
//
//                        imgUrl = memberModel.getImage();
//                        if (imgUrl != null && !imgUrl.isEmpty()){
//                            Glide.with(MembershipApplication.this)
//                                    .load(imgUrl)
//                                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
//                                    .placeholder(getDrawable(R.drawable.profileplaceholder))
//                                    .into(binding.uploaderImage);
//                        }
//                        binding.nameEd.setText(memberModel.getName());
//                        binding.fatherEd.setText(memberModel.getFather_name());
//                        binding.dobEd.setText(Helper.formatDate("yyyy-MM-dd", "dd LLL yyyy", memberModel.getDob()));
//                        dob = memberModel.getDob();
//                        binding.address.setText(memberModel.getAddress());
                    }else {
                        binding.memberMessage.setText("Membership Not Activated");
                        binding.memberMessage.setTextColor(getColor(R.color.red_colour));
                        binding.renewCard.setOnClickListener(v->{
                            Helper.showAlertNoAction(MemberShip_Act.this, "Not Eligible",
                                    "First be a member then only you can renew your membership...!",
                                    "Okay");
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<MembershipResponse> call, Throwable t) {
                binding.memberMessage.setText("Membership Not Activated");
                binding.memberMessage.setTextColor(getColor(R.color.red_colour));
                binding.renewCard.setOnClickListener(v->{
                    Helper.showAlertNoAction(MemberShip_Act.this, "Error 405",
                            "Something went wrong. Please try again later...!",
                            "Okay");
                });
            }
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

    private void takeScreenshot(ConstraintLayout layout) {
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

    public void showBottomBar(){
        DialogOpenChromeOrAppBinding dialogBinding = DialogOpenChromeOrAppBinding.inflate(getLayoutInflater());

        // Create a new dialog and set the custom layout
        Dialog dialog = new Dialog(this);
        dialog.setContentView(dialogBinding.getRoot());
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.BOTTOM);

        ArrayAdapter<String> obj = new ArrayAdapter<String>(MemberShip_Act.this, R.layout.layout_spinner_items, validity);
        dialogBinding.spinnerValidity.setAdapter(obj);
        dialogBinding.spinnerValidity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                dialogBinding.validityPayment.setText(price[i]);
                if (i == 0){
                    validSt = Helper.getFutureDate(3);
                }else if (i == 1){
                    validSt = Helper.getFutureDate(6);
                }else {
                    validSt = Helper.getFutureDate(12);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        dialogBinding.payBtn.setOnClickListener(v->{
            int amount = Integer.parseInt(dialogBinding.validityPayment.getText().toString());
            startPaymentMethod(amount);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void startPaymentMethod(int price) {
        int normalPrice = price * 100;

        try {
            JSONObject options = new JSONObject();
            options.put("name", "All India Minority Association");
            options.put("description", "Donating For All India Minority Association");
            options.put("send_sms_hash",true);
            options.put("allow_rotation", true);

            //You can omit the image option to fetch the image from dashboard
            options.put("image", "https://play-lh.googleusercontent.com/zckSw2H858GVtLbh2UwBWUocb6tT9CsEQcQGGVeF0pOnga1-bVxS_lgStiNGxeVoOC8");
            options.put("currency", "INR");
            options.put("amount", normalPrice);

            JSONObject preFill = new JSONObject();
            preFill.put("email", Helper.userDetails.getEmail());
            preFill.put("contact", Helper.userDetails.getPhone());
            options.put("prefill", preFill);

            checkout.open(MemberShip_Act.this, options);
        } catch (Exception e) {
            Toast.makeText(MemberShip_Act.this, "Error in payment: " + e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
            e.printStackTrace();
        }
    }

    @Override
    public void onPaymentSuccess(String s) {
        progressDialog.setMessage("We're submitting you data...");
        progressDialog.show();
        validTill = Integer.parseInt(validSt);
        Call<ApiResponse> call = apiService.updateMembership("updatePostField", auth.getCurrentUser().getUid(),
                "valid_till", String.valueOf(validTill));
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null){
                    ApiResponse apiResponse = response.body();
                    progressDialog.dismiss();
                    if (apiResponse.getStatus().endsWith("success")){
                        MainActivity.sendTopicNotification("admin", "Membership Renewed",
                                "Someone renewed membership.");
                        Toast.makeText(MemberShip_Act.this, "Success!", Toast.LENGTH_LONG).show();
                    }else {
                        Helper.showAlertNoAction(MemberShip_Act.this,
                                "Failure", apiResponse.getMessage(), "Okay");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(MemberShip_Act.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onPaymentError(int i, String s) {

    }
}
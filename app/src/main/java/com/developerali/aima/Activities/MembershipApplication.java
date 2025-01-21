package com.developerali.aima.Activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developerali.aima.Forms.MembershipApply;
import com.developerali.aima.Helpers.Helper;
import com.developerali.aima.MainActivity;
import com.developerali.aima.Model_Apis.ApiResponse;
import com.developerali.aima.Model_Apis.ApiService;
import com.developerali.aima.Model_Apis.ImageUploadResponse;
import com.developerali.aima.Model_Apis.MembershipResponse;
import com.developerali.aima.Model_Apis.RetrofitClient;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ActivityMembershipApplicationBinding;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MembershipApplication extends AppCompatActivity implements PaymentResultListener {

    ActivityMembershipApplicationBinding binding;
    FirebaseAuth auth;
    ApiService apiService;
    String imgUrl, dobTxt, validSt, name, fName, address, dob;
    int validTill;
    ProgressDialog progressDialog;
    Checkout checkout;
    String[] validity = {"3 Months", "6 Months", "12 Months"};
    String[] price = {"60", "110", "200"};
    boolean step2layVisible = true, step1layVisible = false, step3layVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMembershipApplicationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        checkout = new Checkout();
        checkout.setKeyID("rzp_live_0734xBSfpgbEg3");
        checkout.preload(getApplicationContext());

        auth = FirebaseAuth.getInstance();
        apiService = RetrofitClient.getClient().create(ApiService.class);
        imgUrl = Helper.userDetails.getImage();
        validSt = Helper.getFutureDate(3);

        if (imgUrl != null && !imgUrl.isEmpty()){
            Glide.with(MembershipApplication.this)
                    .load(imgUrl)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .placeholder(getDrawable(R.drawable.profileplaceholder))
                    .into(binding.uploaderImage);
        }

        progressDialog = new ProgressDialog(MembershipApplication.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("connecting to server...");

        checkVisible();
        checkOldFormFill();

        binding.profileLink.setText(auth.getCurrentUser().getUid());
        binding.profileLink.setOnClickListener(v->{
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Profile Link Copied", auth.getCurrentUser().getUid());
            Toast.makeText(MembershipApplication.this, "Profile Link Copied", Toast.LENGTH_LONG).show();
            clipboard.setPrimaryClip(clip);
        });


        ArrayAdapter<String> obj = new ArrayAdapter<String>(MembershipApplication.this, R.layout.layout_spinner_items, validity);
        binding.spinnerValidity.setAdapter(obj);
        binding.spinnerValidity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                binding.validityPayment.setText(price[i]);
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

        binding.dobEd.setOnClickListener(v->{
            Calendar calendar = Calendar.getInstance();
            int mm = calendar.get(Calendar.MONTH);
            int dd = calendar.get(Calendar.DAY_OF_MONTH);
            int yyyy = calendar.get(Calendar.YEAR);

            DatePickerDialog dp = new DatePickerDialog(MembershipApplication.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");
                    SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
                    calendar.set(year, month, day);
                    String choosenDate = simpleDateFormat.format(calendar.getTime());
                    dobTxt = simpleDateFormat2.format(calendar.getTime());
                    binding.dobEd.setText(choosenDate);
                }
            }, yyyy, mm, dd);

            dp.show();
        });

        binding.openLink.setOnClickListener(v->{
//            Intent i = new Intent(MembershipApplication.this, WebViewActivity.class);
//            i.putExtra("provide", "https://docs.google.com/forms/d/e/1FAIpQLSdXOmDnMTPwT6ocBD0UCjFodn80R3lVJkECzr0enuMz1uOlvw/viewform?usp=sf_link");
//            startActivity(i);
            Helper.openLink(MembershipApplication.this, "https://docs.google.com/forms/d/e/1FAIpQLSdXOmDnMTPwT6ocBD0UCjFodn80R3lVJkECzr0enuMz1uOlvw/viewform?usp=sf_link");
            binding.step1Lay.setVisibility(View.GONE);
            binding.step2Lay.setVisibility(View.VISIBLE);
            binding.step3Lay.setVisibility(View.VISIBLE);
        });

        binding.uploaderImage.setOnClickListener(v->{
            if (ContextCompat.checkSelfPermission(MembershipApplication.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(MembershipApplication.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                // Request the necessary permissions
                ActivityCompat.requestPermissions(MembershipApplication.this, new String[]{
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

        binding.step2Btn.setOnClickListener(v->{
            name = binding.nameEd.getText().toString();
            fName = binding.fatherEd.getText().toString();
            address = binding.address.getText().toString();
            dob = binding.dobEd.getText().toString();
            validTill = Integer.parseInt(validSt);

            if (name.isEmpty()){
                binding.nameEd.setError("*");
            } else if (fName.isEmpty()) {
                binding.fatherEd.setError("*");
            } else if (address.isEmpty()) {
                binding.address.setError("*");
            } else if (dob.isEmpty()){
                binding.dobEd.setError("*");
            }else {
                binding.step2Lay.setVisibility(View.GONE);
                binding.step3Lay.setVisibility(View.VISIBLE);
            }
        });

        binding.myProfile.setOnClickListener(v->{
            onBackPressed();
        });

        binding.payBtn.setOnClickListener(v->{
            StringBuilder missingFields = new StringBuilder();

            // Validate each field and add missing ones to the list
            if (name == null || name.isEmpty()) {
                missingFields.append(" • Name is required\n");
            }
            if (fName == null || fName.isEmpty()) {
                missingFields.append(" • Father's Name is empty\n");
            }
            if (address == null || address.isEmpty()) {
                missingFields.append(" • Address not found\n");
            }
            if (dob == null || dob.isEmpty()) {
                missingFields.append(" • Date of Birth is empty\n");
            }
            if (validTill <= 0) {
                missingFields.append(" • Choose a valid plan\n");
            }
            if (imgUrl == null || imgUrl.isEmpty()) {
                missingFields.append(" • Image not chosen\n");
            }

            // If there are missing fields, show them to the user
            if (missingFields.length() > 0) {
                Helper.showAlertNoAction(MembershipApplication.this, "Data Glitch",
                        "Please fill the following fields:\n" + missingFields.toString(), "Okay");
            }else {
                startPaymentMethod();
            }
        });








    }

    private void startPaymentMethod() {
        int Amt = Integer.parseInt(binding.processingFee.getText().toString());
        int Amt2 = Integer.parseInt(binding.validityPayment.getText().toString());
        int normalPrice = (Amt+Amt2) * 100;

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

            checkout.open(MembershipApplication.this, options);
        } catch (Exception e) {
            Toast.makeText(MembershipApplication.this, "Error in payment: " + e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
            e.printStackTrace();
        }
    }

    private void checkOldFormFill() {
        Call<MembershipResponse> call = apiService.fetchMembershipByUserId("fetchMembershipByUserId", auth.getCurrentUser().getUid());
        call.enqueue(new Callback<MembershipResponse>() {
            @Override
            public void onResponse(Call<MembershipResponse> call, Response<MembershipResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MembershipResponse membershipResponse = response.body();
                    if (membershipResponse.getStatus().equalsIgnoreCase("success")) {
                        MembershipResponse.Data memberModel = membershipResponse.getData();

                        binding.payBtn.setEnabled(false);
                        binding.payBtn.setText("You are already a member!");
                        binding.payBtn.setBackground(getDrawable(R.drawable.react_background));
                        binding.payBtn.setTextColor(getColor(R.color.black));

                        imgUrl = memberModel.getImage();
                        if (imgUrl != null && !imgUrl.isEmpty()){
                            Glide.with(MembershipApplication.this)
                                    .load(imgUrl)
                                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                    .placeholder(getDrawable(R.drawable.profileplaceholder))
                                    .into(binding.uploaderImage);
                        }
                        binding.nameEd.setText(memberModel.getName());
                        binding.fatherEd.setText(memberModel.getFather_name());
                        binding.dobEd.setText(Helper.formatDate("yyyy-MM-dd", "dd LLL yyyy", memberModel.getDob()));
                        dob = memberModel.getDob();
                        binding.address.setText(memberModel.getAddress());
                    }
                }
            }

            @Override
            public void onFailure(Call<MembershipResponse> call, Throwable t) {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ImagePicker.with(this)
                        .crop()
                        .compress(200)       //Final image resolution will be less than 1080 x 1080(Optional)
                        .start(85);
            } else {
                Toast.makeText(MembershipApplication.this, "Camera and Storage permission are required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getData() != null && requestCode == 25){
            Uri selectedImageUri = data.getData();
            binding.uploaderImage.setImageURI(selectedImageUri);
            progressDialog.show();
            uploadImage(selectedImageUri, "member");
        }
    }

    private void uploadImage(Uri imageUri, String fieldName) {
        try {
            File file = uriToFile(imageUri);
            if (file != null) {
                uploadImageFile(file, fieldName);
            } else {
                Toast.makeText(MembershipApplication.this, "Failed to get file from URI", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MembershipApplication.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        imgUrl = imageResponse.getData().getUrl();
                        progressDialog.dismiss();
                    }else {
                        progressDialog.dismiss();
                        Toast.makeText(MembershipApplication.this, "Not Uploaded: " + imageResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(MembershipApplication.this, "Upload failed.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ImageUploadResponse> call, Throwable t) {
                t.printStackTrace();
                progressDialog.dismiss();
                Toast.makeText(MembershipApplication.this, "Upload error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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

    void checkVisible(){
        binding.step1Title.setOnClickListener(v->{
            if (step1layVisible){
                binding.step1Lay.setVisibility(View.GONE);
                step1layVisible = false;
            }else {
                binding.step1Lay.setVisibility(View.VISIBLE);
                step1layVisible = true;
            }
        });
        binding.step2Title.setOnClickListener(v->{
            if (step2layVisible){
                binding.step2Lay.setVisibility(View.GONE);
                step2layVisible = false;
            }else {
                binding.step2Lay.setVisibility(View.VISIBLE);
                step2layVisible = true;
            }
        });
        binding.step3Title.setOnClickListener(v->{
            if (step3layVisible){
                binding.step3Lay.setVisibility(View.GONE);
                step3layVisible = false;
            }else {
                binding.step3Lay.setVisibility(View.VISIBLE);
                step3layVisible = true;
            }
        });

        String htmlContent = "<b>Important Notice</b><br>" +
                "<p>You can fill this form only one time. Corrections or Modifications will be allowed but chargeable. <br><br>" +
                "<strong>Note:</strong> Getting problem? Go to <b>Help Center</b></div>";
        binding.step1Text.setText(HtmlCompat.fromHtml(htmlContent, HtmlCompat.FROM_HTML_MODE_LEGACY));

        String html2Content = "<b>You Should Know</b><br>" +
                "<p>Below details will be show in you member card. So make sure below details are correct. <br>" +
                "If we found any issue during verification, your membership will be rejected!" +
                "<strong>Note:</strong> Getting problem? Go to <b>Help Center</b></div>";
        binding.step2Text.setText(HtmlCompat.fromHtml(html2Content, HtmlCompat.FROM_HTML_MODE_LEGACY));

        String html3Content = "<b>Check Plan & Pay Online</b><br>" +
                "<p>Pay as you go with plan! you have to renew membership after finish below plan<p><br>" +
                "<strong>Note:</strong> Getting problem? Go to <b>Help Center</b></div>";
        binding.step3Text.setText(HtmlCompat.fromHtml(html3Content, HtmlCompat.FROM_HTML_MODE_LEGACY));
    }

    @Override
    public void onPaymentSuccess(String s) {
        progressDialog.setMessage("We're submitting you data...");
        progressDialog.show();
        Call<ApiResponse> call = apiService.insertMembership(
                "insertMembership", auth.getCurrentUser().getUid(), name, fName, imgUrl, dobTxt, address, validTill
        );

        String name = binding.nameEd.getText().toString();
        String html2Content = "<b>Congratulations! "+ name + " </b><br>" +
                "<p>Your application is on review. It will take up to <b>7 days</b>. We will check all data you provided and if found any issue then our operators will get back to you!<br>" +
                "<strong>Note:</strong> Getting problem? Go to <b>Help Center</b>";

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null){
                    ApiResponse apiResponse = response.body();
                    progressDialog.dismiss();
                    if (apiResponse.getStatus().endsWith("success")){
                        MainActivity.sendTopicNotification("admin", "Membership Applied",
                                name + " applied for new membership.");
                        Helper.showAlertNoAction(MembershipApplication.this,
                                "Application Submitted", html2Content
                                , "Okay");
                    }else {
                        Helper.showAlertNoAction(MembershipApplication.this,
                                "Failure", apiResponse.getMessage(), "Okay");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(MembershipApplication.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPaymentError(int i, String s) {
        String name = binding.nameEd.getText().toString();
        MainActivity.sendTopicNotification("admin", "Membership Failed",
                name + " failed for new membership.");
//        Helper.showAlertNoAction(MembershipApplication.this,
//                "Failure", s , "Okay");
    }
}
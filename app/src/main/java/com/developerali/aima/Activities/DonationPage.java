package com.developerali.aima.Activities;

import androidx.appcompat.app.AppCompatActivity;


import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Toast;

import com.developerali.aima.CommonFeatures;
import com.developerali.aima.MainActivity;
import com.developerali.aima.Models.UsagesModel;
import com.developerali.aima.databinding.ActivityDonationPageBinding;
import com.developerali.aima.databinding.DialogAmountInsertBinding;
import com.developerali.aima.databinding.DialogNotLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.phonepe.intent.sdk.api.B2BPGRequest;
import com.phonepe.intent.sdk.api.B2BPGRequestBuilder;
import com.phonepe.intent.sdk.api.PhonePe;
import com.phonepe.intent.sdk.api.PhonePeInitException;
import com.phonepe.intent.sdk.api.models.PhonePeEnvironment;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Formatter;


public class DonationPage extends AppCompatActivity implements PaymentResultListener {

    ActivityDonationPageBinding binding;
    private DonationPage activity;
    SharedPreferences sharedPreferences;
    long startTime, totalSeconds;
    FirebaseAuth auth;
    Checkout checkout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDonationPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Donation Page");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.rippleBackground.startRippleAnimation();
        activity = DonationPage.this;

//        binding.rippleBackground.setOnClickListener(v->{
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//                intent.setData(Uri.parse("upi://pay?pa=sksultanali52584@okhdfcbank"));
//                Intent chooser = Intent.createChooser(intent, "Donate From...");
//                if (intent.resolveActivity(getPackageManager()) != null) {
//                    startActivity(chooser);
//                }
//                startActivity(chooser);
//            Toast.makeText(DonationPage.this, "Loading...", Toast.LENGTH_SHORT).show();
//        });

        binding.vpaId.setOnClickListener(c->{
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("ID Copied", binding.vpaId.getText().toString());
            Toast.makeText(DonationPage.this, "VPA ID Copied", Toast.LENGTH_SHORT).show();
            clipboard.setPrimaryClip(clip);
        });

        binding.phoneNo.setOnClickListener(c->{
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Phone Copied", binding.phoneNo.getText().toString());
            Toast.makeText(DonationPage.this, "Phone Number Copied", Toast.LENGTH_SHORT).show();
            clipboard.setPrimaryClip(clip);
        });

        binding.accountNo.setOnClickListener(c->{
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Account Copied", binding.accountNo.getText().toString());
            Toast.makeText(DonationPage.this, "Account No Copied", Toast.LENGTH_SHORT).show();
            clipboard.setPrimaryClip(clip);
        });

        binding.ifsc.setOnClickListener(c->{
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("IFSC Code Copied", binding.ifsc.getText().toString());
            Toast.makeText(DonationPage.this, "IFSC Code Copied", Toast.LENGTH_SHORT).show();
            clipboard.setPrimaryClip(clip);
        });

        // Declare constants
//        final String MERCHANT_ID = "ALLINDIAONLINE";
//        final String API_ENDPOINT = "/pg/v1/pay";
//        final String CALLBACK_URL = "https://webhook.site/5c35178e-017a-4a80-b9ea-2bf10e672e3e";
//        final String SALT = "5c35178e-017a-4a80-b9ea-2bf10e672e3e";
//        final int SALT_INDEX = 1;
//
//
//        String merchantTransactionId = "AIM" + System.currentTimeMillis();
//        String merchantUserId = auth.getCurrentUser().getUid();
//        int amount = 10;
//
//        String requestBody = "{\n" +
//                "  \"merchantId\": \"" + MERCHANT_ID + "\",\n" +
//                "  \"merchantTransactionId\": \"" + merchantTransactionId + "\",\n" +
//                "  \"merchantUserId\": \"" + merchantUserId + "\",\n" +
//                "  \"amount\": " + amount + ",\n" +
//                "  \"callbackUrl\": \"" + CALLBACK_URL + "\",\n" +
//                "  \"mobileNumber\": \"8967254087\",\n" +
//                "  \"paymentInstrument\": {\n" +
//                "    \"type\": \"PAY_PAGE\"\n" +
//                "  }\n" +
//                "}";
//
//        String base64EncodedRequestBody = encodeToBase64(requestBody);
//
//        String checksum = sha256(base64EncodedRequestBody + API_ENDPOINT + SALT) + "###" + SALT_INDEX;
//
//        B2BPGRequest b2BPGRequest = new B2BPGRequestBuilder()
//                .setData(base64EncodedRequestBody)
//                .setChecksum(checksum)
//                .setUrl(API_ENDPOINT)
//                .build();



        Checkout.preload(getApplicationContext());
        checkout = new Checkout();
        checkout.setKeyID("rzp_live_0734xBSfpgbEg3");


        binding.rippleBackground.setOnClickListener(v -> {
            showDonateBox();
        });










    }

    private void showDonateBox() {
        DialogAmountInsertBinding donateBox = DialogAmountInsertBinding.inflate(getLayoutInflater());
        Dialog dialog1 = new Dialog(this);
        dialog1.setContentView(donateBox.getRoot());

        dialog1.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog1.getWindow().setGravity(Gravity.CENTER_VERTICAL);

        donateBox.donateBtn.setOnClickListener(v->{
            String donateAmount = donateBox.donateAmount.getText().toString();
            String phoneNum = donateBox.phoneNum.getText().toString();

            if (donateAmount.isEmpty()){
                donateBox.donateAmount.setError("*");
            } else if (phoneNum.isEmpty()) {
                donateBox.phoneNum.setError("*");
            }else {

                try {

                    int normalPrice = Integer.parseInt(donateAmount) * 100;

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
//                preFill.put("email", user.getEmail());
                preFill.put("contact", "+91"+phoneNum);
                options.put("prefill", preFill);

                    checkout.open(activity, options);

                } catch (Exception e) {
                    Toast.makeText(activity, "Error in payment: " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                    e.printStackTrace();
                }

                dialog1.dismiss();

            }
        });

        dialog1.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 22) {

            Toast.makeText(activity, "flow complete...", Toast.LENGTH_SHORT).show();
              /*This callback indicates only about completion of UI flow.
                    Inform your server to make the transaction
                    status call to get the status. Update your app with the
                    success/failure status.*/

        }
    }

    private static String encodeToBase64(String input) {
        // Encode the input string to Base64
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    private static String sha256(String input) {
        try {
            byte[] bytes = input.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(bytes);

            StringBuilder result = new StringBuilder();
            for (byte b : digest) {
                result.append(String.format("%02x", b));
            }

            return result.toString();
        } catch (Exception e) {
            // Handle the exception according to your needs
            e.printStackTrace();
            return null;
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        sharedPreferences = getSharedPreferences("UsageTime", MODE_PRIVATE); //creating database
        totalSeconds = sharedPreferences.getLong("total_seconds", 0);  //getting previous value
        startTime = System.currentTimeMillis();  //get start time for counting

    }

    @Override
    protected void onPause() {
        long currentTime = System.currentTimeMillis();  //get stop time for counting
        long totalTime = currentTime - startTime;   //calculating watch time
        long newTime = totalSeconds + (totalTime/1000);    //add previous sec and now time converting in sec

        SharedPreferences.Editor editor = sharedPreferences.edit();  // updating in database
        editor.putLong("total_seconds", newTime);
        editor.apply();

        ArrayList<UsagesModel> arrayList = CommonFeatures.readListFromPref(this);
        UsagesModel usagesModel = new UsagesModel("Donation Page", startTime, currentTime);
        arrayList.add(usagesModel);
        CommonFeatures.writeListInPref(DonationPage.this, arrayList);
        super.onPause();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onPaymentSuccess(String s) {
        Toast.makeText(activity, s, Toast.LENGTH_SHORT).show();
        Toast.makeText(activity, "Thank you for donating on AIMA !", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPaymentError(int i, String s) {

    }
}
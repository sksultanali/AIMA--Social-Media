package com.developerali.aima.Forms;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.developerali.aima.Activities.WebViewActivity;
import com.developerali.aima.Models.MemberApplied;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ActivityMembershipApplyBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.shuhart.stepview.StepView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class MembershipApply extends AppCompatActivity implements PaymentResultListener {

    ActivityMembershipApplyBinding binding;
    ArrayList<String> formName = new ArrayList<>();
    Uri imageUri;
    FirebaseDatabase database;
    ProgressDialog progressDialog;
    String valueChild, formLink;
    FirebaseAuth auth;
    FirebaseStorage storage;
    Checkout checkout;
    String[] validity = {"3 Months", "6 Months", "12 Months"};
    String[] price = {"60", "110", "200"};
    String[] payWith = {"Select Payment Mode", "UPI", "QR Code", "Bank Transfer"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMembershipApplyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        Checkout.preload(getApplicationContext());
        checkout = new Checkout();
        checkout.setKeyID("rzp_live_0734xBSfpgbEg3");

        valueChild = getIntent().getStringExtra("value");
        if (valueChild.equalsIgnoreCase("renewForms")){
            binding.processingFee.setText("0");
            binding.emailCodeHint.setHint("Enter Card UID No");
            formLink = "https://docs.google.com/forms/d/e/1FAIpQLSdq0Pbfers1yBD-Rp8ndPdxDhKrQXZUVbnPl0Si6BPf2bzmJA/viewform?usp=sf_link";
        }else {
            formLink = "https://docs.google.com/forms/d/e/1FAIpQLSdXOmDnMTPwT6ocBD0UCjFodn80R3lVJkECzr0enuMz1uOlvw/viewform?usp=sf_link";
        }

        progressDialog = new ProgressDialog(MembershipApply.this);
        progressDialog.setMessage("loading request");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        //formName.add("Choose Plan & Make Payment");
        formName.add("Step: 1 - Fill form data");
        formName.add("Step: 2 - Re_enter important data");
        formName.add("Step: 2 - Pay and Submit");

        binding.stepView.getState()
                .animationType(StepView.ANIMATION_CIRCLE)
                .steps(new ArrayList<String>() {{
                    add("step 1");
                    add("step 2");
                    add("step 3");
                }})
                .stepsNumber(3)
                .animationDuration(getResources().getInteger(android.R.integer.config_shortAnimTime))
                .commit();

        binding.stepName.setText(formName.get(0));
        binding.step01.setVisibility(View.VISIBLE);
        binding.stepView.setOnStepClickListener(new StepView.OnStepClickListener() {
            @Override
            public void onStepClick(int step) {
                binding.stepView.go(step, true);
                binding.stepName.setText(formName.get(step));
                setStep(step);
            }
        });

        binding.btnNext.setOnClickListener(v->{
            int position = binding.stepView.getCurrentStep();
            if (position < 1){
                binding.stepView.go(position + 1, true);
                setStep(position + 1);
            }else {
                binding.stepView.done(true);
            }
//            if (position == 1){
//                finalSubmit();
//            }
        });

        binding.btnPre.setOnClickListener(v->{
            int position = binding.stepView.getCurrentStep();
            if (position == 1){
                binding.stepView.go(position - 1, true);
                setStep(position - 1);
            }else {
                binding.stepView.done(false);
            }
        });



        ArrayAdapter<String> obj = new ArrayAdapter<String>(MembershipApply.this, R.layout.layout_spinner_items, validity);
        binding.spinnerValidity.setAdapter(obj);

        binding.spinnerValidity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                binding.validityPayment.setText(price[i]);
                if (i == 0){
                    binding.renewText.setText("Membership Fee (3 months)");
                }else if (i == 1){
                    binding.renewText.setText("Membership Fee (6 months)");
                }else {
                    binding.renewText.setText("Membership Fee (12 months)");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

//        binding.spinnerPayWith.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                switch (i){
//                    case 0:
//                        binding.payBtn.setVisibility(View.GONE);
//                        binding.paymentMode.setText("");
//                        binding.qrCode.setVisibility(View.GONE);
//                        binding.bankDetails.setVisibility(View.GONE);
//                        break;
//                    case 1:
//                        binding.payBtn.setVisibility(View.VISIBLE);
//                        binding.paymentMode.setText("UPI");
//                        binding.qrCode.setVisibility(View.GONE);
//                        binding.bankDetails.setVisibility(View.GONE);
//                        break;
//                    case 2:
//                        binding.qrCode.setVisibility(View.VISIBLE);
//                        binding.paymentMode.setText("Scan");
//                        binding.payBtn.setVisibility(View.GONE);
//                        binding.bankDetails.setVisibility(View.GONE);
//                        break;
//                    case 3:
//                        binding.bankDetails.setVisibility(View.VISIBLE);
//                        binding.paymentMode.setText("Bank");
//                        binding.payBtn.setVisibility(View.GONE);
//                        binding.qrCode.setVisibility(View.GONE);
//                        break;
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });

//        binding.accountNo.setOnClickListener(c->{
//            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//            ClipData clip = ClipData.newPlainText("Account Copied", binding.accountNo.getText().toString());
//            Toast.makeText(MembershipApply.this, "Account No Copied", Toast.LENGTH_SHORT).show();
//            clipboard.setPrimaryClip(clip);
//        });
//
//        binding.ifsc.setOnClickListener(c->{
//            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//            ClipData clip = ClipData.newPlainText("IFSC Code Copied", binding.ifsc.getText().toString());
//            Toast.makeText(MembershipApply.this, "IFSC Code Copied", Toast.LENGTH_SHORT).show();
//            clipboard.setPrimaryClip(clip);
//        });


        binding.payBtn.setOnClickListener(v->{
            String emailCode = binding.codeEmail.getText().toString();
            String phone = binding.phoneNumber.getText().toString();

            if (emailCode.isEmpty()){
                binding.codeEmail.setError("can't empty");
            } else if (phone.isEmpty()) {
                binding.phoneNumber.setError("can't empty");
            }else {

                try {

                    int Amt = Integer.parseInt(binding.processingFee.getText().toString());
                    int Amt2 = Integer.parseInt(binding.validityPayment.getText().toString());
                    int normalPrice = (Amt+Amt2) * 100;

                    JSONObject options = new JSONObject();
                    options.put("name", "All India Minority Association");
                    options.put("description", "Donating For All India Minority Association");
                    options.put("send_sms_hash",true);
                    options.put("allow_rotation", true);

                    //You can omit the image option to fetch the image from dashboard
                    options.put("image", "https://play-lh.googleusercontent.com/zckSw2H858GVtLbh2UwBWUocb6tT9CsEQcQGGVeF0pOnga1-bVxS_lgStiNGxeVoOC8");
                    options.put("currency", "INR");
                    options.put("amount", normalPrice);

                    //JSONObject preFill = new JSONObject();
//                preFill.put("email", user.getEmail());
                   // preFill.put("contact", "+91"+phoneNum);
                    //options.put("prefill", preFill);

                    checkout.open(MembershipApply.this, options);

                } catch (Exception e) {
                    Toast.makeText(MembershipApply.this, "Error in payment: " + e.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                    e.printStackTrace();
                }
                //pay and if success then
                //finalSubmit();

            }
        });

        binding.openChrome.setOnClickListener(v->{
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(formLink));
            startActivity(browserIntent);

        });

        binding.openApp.setOnClickListener(v->{
            Intent i = new Intent(MembershipApply.this, WebViewActivity.class);
            i.putExtra("provide", formLink);
            startActivity(i);
        });

//        binding.uploadScreenShot.setOnClickListener(v->{
//            ImagePicker.with(this)
//                    .crop()	    			//Crop image(Optional), Check Customization for more option
//                    .compress(3072)			//Final image size will be less than 3 MB(Optional)
//                    .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
//                    .start(85);
//        });

    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (data != null && data.getData() != null && requestCode == 85){
//            imageUri = data.getData();
//            binding.uploadScreenShotText.setText("Image Uploaded");
//            binding.uploadScreenShotText.setTextColor(getColor(R.color.green_colour));
//        }
//    }

    private void finalSubmit() {
        progressDialog.show();

        MemberApplied memberApplied = new MemberApplied();
        memberApplied.setEmailCode(binding.codeEmail.getText().toString());
        memberApplied.setPhone(binding.phoneNumber.getText().toString());
        memberApplied.setScreenshot("NA");
        memberApplied.setProfId(auth.getCurrentUser().getUid());
        memberApplied.setDate(new Date().getTime());

        database.getReference().child(valueChild)
                .child(binding.codeEmail.getText().toString())
                .setValue(memberApplied)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(MembershipApply.this, "Application Submitted", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(MembershipApply.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

//        StorageReference reference = storage.getReference()
//                .child("newMember").child(database.getReference().push().getKey());
//        reference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                if (task.isSuccessful()){
//                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//                        @Override
//                        public void onSuccess(Uri uri) {
//                            String link = uri.toString();
//
//
//                        }
//                    });
//                }
//            }
//        });
    }


    @Override
    public void onPaymentSuccess(String s) {
        finalSubmit();
        Toast.makeText(MembershipApply.this, s, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    public void setStep(int position){
        switch (position){
            case 0:
                binding.stepName.setText(formName.get(0));
                binding.step01.setVisibility(View.VISIBLE);
                binding.step02.setVisibility(View.GONE);
                binding.btnNext.setVisibility(View.VISIBLE);
                binding.btnPre.setVisibility(View.GONE);
                break;
            case 1:
                binding.stepName.setText(formName.get(1));
                binding.step01.setVisibility(View.GONE);
                binding.step02.setVisibility(View.VISIBLE);
                binding.btnNext.setVisibility(View.GONE);
                binding.btnPre.setVisibility(View.VISIBLE);
                break;
        }
    }

}
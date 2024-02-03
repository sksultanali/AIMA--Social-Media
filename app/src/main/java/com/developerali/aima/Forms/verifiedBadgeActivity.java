package com.developerali.aima.Forms;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.developerali.aima.MainActivity;
import com.developerali.aima.Models.VerifiedBadgeModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ActivityVerifiedBadgeBinding;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Date;

public class verifiedBadgeActivity extends AppCompatActivity {

    ActivityVerifiedBadgeBinding binding;
    String identityProof, validWant;
    Uri identityUri, screenShotUri;
    String identityUrl, screenShotUrl;
    ProgressDialog dialog;
    String validity[] = {"3 Months", "6 Months", "12 Months"};
    String price[] = {"30", "50", "100"};
    String payWith[] = {"Select Payment Mode", "UPI", "QR Code", "Bank Transfer"};
    String identity[] = {"Select Identity", "Aadhaar Card", "Voter Card", "Pan Card", "GST"};

    FirebaseStorage storage;
    FirebaseDatabase database;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVerifiedBadgeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        dialog = new ProgressDialog(verifiedBadgeActivity.this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("loading request");

        ArrayAdapter<String> obj = new ArrayAdapter<String>(verifiedBadgeActivity.this, R.layout.layout_spinner_items, validity);
        binding.spinnerValidity.setAdapter(obj);
        ArrayAdapter<String> obj2 = new ArrayAdapter<String>(verifiedBadgeActivity.this, R.layout.layout_spinner_items, identity);
        binding.spinnerIdentity.setAdapter(obj2);
        ArrayAdapter<String> obj3 = new ArrayAdapter<String>(verifiedBadgeActivity.this, R.layout.layout_spinner_items, payWith);
        binding.spinnerPayWith.setAdapter(obj3);

        binding.spinnerValidity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                binding.validityPayment.setText(price[i]);
                validWant = validity[i];
                if (i == 0){
                    binding.renewText.setText("#Next Renewal = After 3months");
                }else if (i == 1){
                    binding.renewText.setText("#Next Renewal = After 6months");
                }else {
                    binding.renewText.setText("#Next Renewal = After 12months");
                }
                binding.renewText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        binding.spinnerIdentity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                identityProof = identity[i];
                if (i == 0){
                    binding.identityUpload.setVisibility(View.GONE);
                }else {
                    binding.identityUpload.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                binding.identityUpload.setVisibility(View.GONE);
            }
        });

        binding.spinnerPayWith.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        binding.payBtn.setVisibility(View.GONE);
                        binding.paymentMode.setText("");
                        binding.qrCode.setVisibility(View.GONE);
                        binding.bankDetails.setVisibility(View.GONE);
                        binding.paymentReceipt.setVisibility(View.GONE);
                        break;
                    case 1:
                        binding.payBtn.setVisibility(View.VISIBLE);
                        binding.paymentMode.setText("UPI");
                        binding.qrCode.setVisibility(View.GONE);
                        binding.bankDetails.setVisibility(View.GONE);
                        binding.paymentReceipt.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        binding.qrCode.setVisibility(View.VISIBLE);
                        binding.paymentMode.setText("Scan");
                        binding.payBtn.setVisibility(View.GONE);
                        binding.bankDetails.setVisibility(View.GONE);
                        binding.paymentReceipt.setVisibility(View.VISIBLE);
                        break;
                    case 3:
                        binding.bankDetails.setVisibility(View.VISIBLE);
                        binding.paymentMode.setText("Bank");
                        binding.payBtn.setVisibility(View.GONE);
                        binding.qrCode.setVisibility(View.GONE);
                        binding.paymentReceipt.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        binding.payBtn.setOnClickListener(v->{
            Toast.makeText(this, "loading...", Toast.LENGTH_SHORT).show();
            int amount = Integer.parseInt(binding.validityPayment.getText().toString()) +
                    Integer.parseInt(binding.processingFee.getText().toString());
            String uri = "upi://pay?pa=" + "sksultanali9732@ybl" + "&am=" + amount + "&pn=" + "&mc=&tid=&tr=" + System.currentTimeMillis() + "&tn=" + "Payment For AIMA Membership" + "&url=";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            Intent chooser = Intent.createChooser(intent, "Pay with...");
            startActivity(chooser);
        });

        binding.submitForm.setOnClickListener(v->{
            if (identityUrl == null || identityUrl.isEmpty()){
                Toast.makeText(this, "Identity Proof Not Uploaded!", Toast.LENGTH_LONG).show();
            } else if (screenShotUrl == null || screenShotUrl.isEmpty()) {
                Toast.makeText(this, "Payment Screenshot Not Uploaded!", Toast.LENGTH_LONG).show();
            }else {
                VerifiedBadgeModel verifiedBadgeModel = new VerifiedBadgeModel();
                verifiedBadgeModel.setTime(new Date().getTime());
                verifiedBadgeModel.setPaymentMode(binding.paymentMode.getText().toString());
                verifiedBadgeModel.setValidity(validWant);
                verifiedBadgeModel.setIdentityLink(identityUrl);
                verifiedBadgeModel.setPaymentScreenshot(screenShotUrl);

                database.getReference().child("verified")
                                .child(auth.getCurrentUser().getUid())
                                        .setValue(verifiedBadgeModel)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Toast.makeText(verifiedBadgeActivity.this, "Application Submitted", Toast.LENGTH_SHORT).show();
                                                        finish();
                                                    }
                                                });
            }
        });

        binding.screenShot.setOnClickListener(v->{
            ImagePicker.with(this)
                    .crop()	    			//Crop image(Optional), Check Customization for more option
                    .compress(2072)			//Final image size will be less than 3 MB(Optional)
                    .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                    .start(45);
        });

        binding.identityUpload.setOnClickListener(v->{
            ImagePicker.with(this)
                    .crop()	    			//Crop image(Optional), Check Customization for more option
                    .compress(2072)			//Final image size will be less than 3 MB(Optional)
                    .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                    .start(85);
        });


        binding.backBadge.setOnClickListener(v->{
            finish();
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getData() != null && requestCode == 85){
            identityUri = data.getData();
            binding.identityUpload.setImageURI(identityUri);
            dialog.show();

            StorageReference reference = storage.getReference().child("verified")
                    .child(auth.getCurrentUser().getUid()).child(database.getReference().push().getKey());
            reference.putFile(identityUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                identityUrl = uri.toString();
                                dialog.dismiss();
                            }
                        });
                    }
                }
            });
        }
        if (data != null && data.getData() != null && requestCode == 45){
            screenShotUri = data.getData();
            binding.screenShot.setImageURI(screenShotUri);

            dialog.show();

            StorageReference reference = storage.getReference().child("verified")
                    .child(auth.getCurrentUser().getUid()).child(database.getReference().push().getKey());
            reference.putFile(screenShotUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                screenShotUrl = uri.toString();
                                dialog.dismiss();
                            }
                        });
                    }
                }
            });
        }
    }

}
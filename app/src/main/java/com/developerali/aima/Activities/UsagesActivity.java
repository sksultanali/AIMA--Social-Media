package com.developerali.aima.Activities;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developerali.aima.Adapters.UsagesAdapter;
import com.developerali.aima.Helpers.CommonFeatures;
import com.developerali.aima.Models.UsagesModel;
import com.developerali.aima.Models.UserModel;
import com.developerali.aima.Models.WithdrawModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ActivityUsagesBinding;
import com.developerali.aima.databinding.BottomWithdrewMoneyBinding;
import com.developerali.aima.databinding.DialogRulesBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class UsagesActivity extends AppCompatActivity {

    ActivityUsagesBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    Activity activity;
    Double ratePerStar;
    ArrayList<UsagesModel> arrayList;
    ProgressDialog progressDialog;
    Animation blinkAnimation;
    String[] payWith = {"Select Withdraw Mode", "UPI", "Bank Transfer"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsagesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle("Earned Stars");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        activity = UsagesActivity.this;

        ratePerStar = 0.0;

        progressDialog = new ProgressDialog(UsagesActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("loading request...");

        blinkAnimation = AnimationUtils.loadAnimation(UsagesActivity.this, R.anim.blink);

        database.getReference().child("users")
                .child(auth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            UserModel userModel = snapshot.getValue(UserModel.class);
                            if (userModel.getImage() != null && !activity.isDestroyed()){
                                Glide.with(UsagesActivity.this)
                                        .load(userModel.getImage())
                                        .placeholder(getDrawable(R.drawable.profileplaceholder))
                                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                        .into(binding.myProfile);
                            }

                            if (userModel.getType() != null){
                                binding.type.setText(userModel.getType());
                            }
                            binding.stars.setText(userModel.getStars() + " Stars");

                            database.getReference().child("others")
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.exists()){
                                                        ratePerStar = snapshot.child("perStar").getValue(Double.class);

                                                        int moneyValue = (int) (userModel.getStars() * ratePerStar);

                                                        ValueAnimator animator = ValueAnimator.ofInt(0, moneyValue);
                                                        animator.setDuration(5000); // Animation duration in milliseconds
                                                        animator.addUpdateListener(valueAnimator -> {
                                                            int animatedValue = (int) valueAnimator.getAnimatedValue();
                                                            binding.money.setText(String.valueOf(animatedValue));
                                                        });
                                                        animator.start();


                                                        binding.withdrawBtn.setOnClickListener(v->{

                                                            if (animator.isRunning()){
                                                                Toast.makeText(UsagesActivity.this, "Calculating Amount", Toast.LENGTH_SHORT).show();
                                                            }else if (moneyValue < 100){

                                                                showDialog("Failed Process",
                                                                        " •  Minimum Withdrawal Threshold Is Not Satisfied \n" +
                                                                                        " •  Minimum Threshold is ₹100 " +
                                                                                ". But Your Balance is ₹" + moneyValue +
                                                                                ". Try After Making More Money. ");

                                                            }else if (userModel.getType() == null){
                                                                showDialog("Not A Member Profile",
                                                                        " •  Profile Type Is Not Satisfied \n" +
                                                                        " •  Must Be a Member Profile " +
                                                                        ". You Have " + binding.type.getText().toString() +
                                                                        ". Try After Being a Member. ");

                                                            }else {

                                                                showBottomForm();

                                                            }

                                                        });

                                                        


                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError error) {

                                                }
                                            });





                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //getFetchData();
        arrayList = new ArrayList<>();

        database.getReference().child("withdraw")
                .child(auth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            binding.withdrawBtn.setEnabled(false);
                            binding.withdrawBtn.setText("Withdraw Requested");
                            binding.withdrawBtn.setBackgroundColor(getColor(R.color.red_colour));
                            binding.withdrawBtn.setTextColor(getColor(R.color.white));
                            binding.withdrawBtn.setAnimation(blinkAnimation);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setHasFixedSize(true);
        UsagesAdapter adapter = new UsagesAdapter(UsagesActivity.this, arrayList);
        binding.recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        if (arrayList == null || arrayList.isEmpty()){
            binding.clearAllUsages.setVisibility(View.GONE);
            binding.noData.setVisibility(View.VISIBLE);
        }else {
            binding.clearAllUsages.setVisibility(View.VISIBLE);
            binding.noData.setVisibility(View.GONE);
        }


        binding.clearAllUsages.setOnClickListener(v->{
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(UsagesActivity.this);
            SharedPreferences.Editor editor = pref.edit();
            editor.remove("usages_list");
            editor.apply();
            arrayList.clear();
            adapter.notifyDataSetChanged();
        });

        try {
            SharedPreferences sharedPreferences = getSharedPreferences("UsageTime", MODE_PRIVATE); //creating database
            long totalSeconds = sharedPreferences.getLong("total_seconds", 1);  //getting previous value
            long minutes;
            if (totalSeconds < 60){
                minutes = 0;
            }else {
                minutes = totalSeconds / 60;
            }

            totalSeconds %= totalSeconds;

            binding.min.setText(minutes + " Min " + totalSeconds + " Sec");
        }catch (Exception e){

        }









    }

    public void showBottomForm(){
        BottomWithdrewMoneyBinding bottomBinding = BottomWithdrewMoneyBinding.inflate(getLayoutInflater());

        AlertDialog dialog = new AlertDialog.Builder(UsagesActivity.this)
                .setView(bottomBinding.getRoot())
                .create();

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //dialog.getWindow().setGravity(Gravity.BOTTOM);

        ArrayAdapter<String> obj = new ArrayAdapter<String>(UsagesActivity.this, android.R.layout.simple_spinner_item, payWith);
        bottomBinding.spinnerWithdraw.setAdapter(obj);

        bottomBinding.moneyAmount.setText(binding.money.getText().toString());

        bottomBinding.spinnerWithdraw.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 1){
                    bottomBinding.upiSegment.setVisibility(View.VISIBLE);
                    bottomBinding.bankSegment.setVisibility(View.GONE);
                    bottomBinding.withdrawBtn.setEnabled(false);
                    bottomBinding.withdrawBtn.setBackground(getDrawable(R.drawable.button_already_followd));
                } else if (position == 2) {
                    bottomBinding.upiSegment.setVisibility(View.GONE);
                    bottomBinding.bankSegment.setVisibility(View.VISIBLE);
                    bottomBinding.withdrawBtn.setEnabled(false);
                    bottomBinding.withdrawBtn.setBackground(getDrawable(R.drawable.button_already_followd));
                }else {
                    bottomBinding.upiSegment.setVisibility(View.GONE);
                    bottomBinding.bankSegment.setVisibility(View.GONE);
                    bottomBinding.withdrawBtn.setEnabled(false);
                    bottomBinding.withdrawBtn.setBackground(getDrawable(R.drawable.button_already_followd));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        bottomBinding.upiId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().length() < 1){
                    bottomBinding.withdrawBtn.setEnabled(false);
                    bottomBinding.withdrawBtn.setBackground(getDrawable(R.drawable.button_already_followd));
                }
                if (s.toString().length() > 1){
                    bottomBinding.withdrawBtn.setEnabled(true);
                    bottomBinding.withdrawBtn.setBackground(getDrawable(R.drawable.button_follow_background));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        bottomBinding.bankName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().length() < 1){
                    bottomBinding.withdrawBtn.setEnabled(false);
                    bottomBinding.withdrawBtn.setBackground(getDrawable(R.drawable.button_already_followd));
                }
                if (s.toString().length() > 1){
                    bottomBinding.withdrawBtn.setEnabled(true);
                    bottomBinding.withdrawBtn.setBackground(getDrawable(R.drawable.button_follow_background));
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        bottomBinding.withdrawBtn.setOnClickListener(v->{

            progressDialog.show();

            int amount = Integer.parseInt(bottomBinding.moneyAmount.getText().toString());
            String details = "UPI Id- " + bottomBinding.upiId.getText().toString() + ", \n" +
                    "Name- " + bottomBinding.name.getText().toString() + ", \n" +
                    "Phone No- " + bottomBinding.phoneNo.getText().toString() + ", \n" +
                    "Bank Name- " + bottomBinding.bankName.getText().toString() + ", \n" +
                    "Bank Account No- " + bottomBinding.accountNo.getText().toString() + ", \n" +
                    "IFsC Code- " + bottomBinding.ifscCode.getText().toString() ;

            WithdrawModel withdrawModel = new WithdrawModel(details, amount);

            database.getReference().child("withdraw")
                    .child(auth.getCurrentUser().getUid())
                    .setValue(withdrawModel)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            database.getReference().child("users")
                                    .child(auth.getCurrentUser().getUid())
                                    .child("stars")
                                    .removeValue();
                            progressDialog.dismiss();
                            dialog.dismiss();
                            binding.withdrawBtn.setEnabled(false);
                            binding.withdrawBtn.setText("Withdraw Requested");
                            binding.withdrawBtn.setBackgroundColor(getColor(R.color.red_colour));
                            binding.money.setText("0");
                            binding.withdrawBtn.setTextColor(getColor(R.color.white));
                            binding.stars.setText("0 Stars");
                            binding.withdrawBtn.setAnimation(blinkAnimation);
                            showDialog("Withdraw Request Sent !", "Your Request Is Sent To The Server. You Will Be Notified Soon");
                        }
                    });
        });




        dialog.show();
    }

    private void getFetchData() {
        if (arrayList == null) {
            arrayList = new ArrayList<>();
        }
        arrayList = CommonFeatures.readListFromPref(this);
        Collections.reverse(arrayList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.help_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.help){
            String message =    " •  One Min = 1 Star = ₹" + ratePerStar + "\n" +
                                " •  60 Minutes Minimum Required For Updating Star On Profile. \n" +
                                " •  Must Be A Member For Withdraw \n" +
                                " •  Spent More Time on AIMA Book, Earn More Money!";
            showDialog("Want to Earn Money? ", message);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @SuppressLint("ResourceAsColor")
    public void showDialog(String title, String textMessage){
        DialogRulesBinding dialogRulesBinding = DialogRulesBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new AlertDialog.Builder(UsagesActivity.this)
                .setView(dialogRulesBinding.getRoot())
                .create();

        dialogRulesBinding.titleText.setText(title);
        dialogRulesBinding.messageText.setText(textMessage);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
        dialogRulesBinding.saveBtn.setOnClickListener(v->{
            dialog.dismiss();
        });

        dialog.show();
    }
}
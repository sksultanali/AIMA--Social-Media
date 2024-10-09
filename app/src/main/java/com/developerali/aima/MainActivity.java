package com.developerali.aima;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.developerali.aima.Activities.Login;
import com.developerali.aima.BottomBar.HomeFragment;
import com.developerali.aima.BottomBar.MeetingFragment;
import com.developerali.aima.BottomBar.MenuFragment;
import com.developerali.aima.BottomBar.ShortsFragment;
import com.developerali.aima.BottomBar.WadiNews;
import com.developerali.aima.databinding.ActivityMainBinding;
import com.developerali.aima.databinding.DialogCongratulationStarBinding;
import com.developerali.aima.databinding.DialogNotLoginBinding;
import com.developerali.aima.databinding.DilaogUpdateMakingBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import me.ibrahimsn.lib.OnItemSelectedListener;


public class MainActivity extends AppCompatActivity{

    ActivityMainBinding binding;
    FirebaseAuth auth;
    private long startTime;
    private long totalSeconds;
    private long stars;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //CommonFeatures.lowerColour(getWindow(), getResources());
        auth = FirebaseAuth.getInstance();

        //default fragment setting
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Intent intent = getIntent();
        Uri data = intent.getData();

        FirebaseDatabase.getInstance().getReference().child("update")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            int version = snapshot.child("version").getValue(Integer.class);
                            String details = snapshot.child("details").getValue(String.class);

                            try {
                                PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                                if (version > packageInfo.versionCode){
                                    showUpdateDialog(version, details);
                                }
                            } catch (PackageManager.NameNotFoundException e) {
                                throw new RuntimeException(e);
                            }


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        if (data != null && auth.getCurrentUser() == null) {
            transaction.replace(R.id.content, new MeetingFragment());
            transaction.commit();
            extractLink(data);
            showNotLoginDialog();
        }else if (data != null && auth.getCurrentUser() != null){
            transaction.replace(R.id.content, new HomeFragment());
            transaction.commit();
            extractLink(data);
        }else {
            transaction.replace(R.id.content, new HomeFragment());
            transaction.commit();
        }




        //bottom navigation bar
        binding.bottomBar.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public boolean onItemSelect(int i) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                switch (i) {
                    case 0:
                        transaction.replace(R.id.content, new HomeFragment()).addToBackStack(null);
                        break;
                    case 1:
                        transaction.replace(R.id.content, new WadiNews()).addToBackStack(null);
                        break;
                    case 2:
                        transaction.replace(R.id.content, new MeetingFragment()).addToBackStack(null);
                        break;
                    case 3:
                        transaction.replace(R.id.content, new ShortsFragment()).addToBackStack(null);
                        break;
                    case 4:
                        transaction.replace(R.id.content, new MenuFragment()).addToBackStack(null);
                        break;
                }
                transaction.commit();
                return false;
            }
        });


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build();
        StrictMode.setThreadPolicy(policy);





    }

    @Override
    protected void onResume() {
        super.onResume();
        sharedPreferences = getSharedPreferences("UsageTime", MODE_PRIVATE); //creating database
        totalSeconds = sharedPreferences.getLong("total_seconds", 0);  //getting previous value
        startTime = System.currentTimeMillis();  //get start time for counting

        long minutes = totalSeconds/60;
        long hour = minutes/60;

        //Toast.makeText(this, ""+ seconds, Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, ""+ minutes, Toast.LENGTH_SHORT).show();

        if (hour >= 1){
            //Toast.makeText(this, "Congratulation min- " + minutes, Toast.LENGTH_SHORT).show();
            //update as minutes

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users")
                    .child(auth.getCurrentUser().getUid())
                    .child("stars");

            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        try {
                            stars = snapshot.getValue(Long.class);
                            reference.setValue(stars + minutes);

                            SharedPreferences sharedPreferences1 = getSharedPreferences("UsageTime", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences1.edit();  // updating in database
                            editor.putLong("total_seconds", 0);
                            editor.apply();
                            totalSeconds = sharedPreferences.getLong("total_seconds", 0);

                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    showCongratulatoins();
                                }
                            }, 5000);
                        }catch (Exception e){

                        }

                    }else {
                        reference.setValue(minutes);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void showCongratulatoins() {
        DialogCongratulationStarBinding dialogUpdate = DialogCongratulationStarBinding.inflate(getLayoutInflater());
        Dialog dialog1 = new Dialog(this);
        dialog1.setContentView(dialogUpdate.getRoot());

        dialog1.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog1.getWindow().setGravity(Gravity.CENTER_VERTICAL);

        dialog1.setCancelable(false);
        dialog1.setCanceledOnTouchOutside(false);

        dialogUpdate.okayBtn.setOnClickListener(v->{
            dialog1.dismiss();
        });

        dialog1.show();
    }

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
//        UsagesModel usagesModel = new UsagesModel("AIMA Book", startTime, currentTime);
//        if (arrayList != null) {
//
//            arrayList.add(usagesModel);
//
//        } else {
//
//        }
//
//        CommonFeatures.writeListInPref(MainActivity.this, arrayList);
//
//        super.onPause();
//    }

    private void showUpdateDialog(int version, String details) {
        DilaogUpdateMakingBinding dialogUpdate = DilaogUpdateMakingBinding.inflate(getLayoutInflater());
        Dialog dialog1 = new Dialog(this);
        dialog1.setContentView(dialogUpdate.getRoot());

        dialog1.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog1.getWindow().setGravity(Gravity.CENTER_VERTICAL);

        dialog1.setCancelable(false);
        dialog1.setCanceledOnTouchOutside(false);

        dialogUpdate.titleUpdate.setText("Update " + version + " Available :(");
        dialogUpdate.message.setText(details);

        dialogUpdate.updateBtn.setOnClickListener(v->{
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + getPackageName()));
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        });

        dialog1.show();
    }

    public void extractLink (Uri link){

        List<String> pathSegments = link.getPathSegments();

        if (link.getHost().equalsIgnoreCase("meet.jit.si") && pathSegments.size() >= 1){
            String value = pathSegments.get(0);
            if (value.equalsIgnoreCase("static")){
                String val = link.getQueryParameter("room");
                setSecretCode(val);
                Toast.makeText(this, val+ " 1", Toast.LENGTH_SHORT).show();
            }else {
                setSecretCode(value);
            }
        }else if (pathSegments.size() >= 1){
            String value = pathSegments.get(0);
            setSecretCode(value);
        }
    }

    public void setSecretCode(String secretCode){

        MeetingFragment fragment = new MeetingFragment();
        Bundle bundle = new Bundle();
        bundle.putString("secretCode", secretCode);
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.content, fragment)
                .commit();

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
            Intent i = new Intent(MainActivity.this, Login.class);
            startActivity(i);
            finish();
        });

        dialog1.show();
    }

}
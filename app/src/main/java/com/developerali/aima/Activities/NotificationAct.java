package com.developerali.aima.Activities;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.developerali.aima.Adapters.NotificationAdapter;
import com.developerali.aima.Models.NotificationModel;
import com.developerali.aima.databinding.ActivityNotificationBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class NotificationAct extends AppCompatActivity {

    ActivityNotificationBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    ArrayList<NotificationModel> models;
    NotificationAdapter adapter;
    ProgressDialog dialog;
    private long startTime;
    private long totalSeconds;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        dialog = new ProgressDialog(NotificationAct.this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage("loading request...");

        models = new ArrayList<>();
        models.clear();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(NotificationAct.this);
        binding.notificationRecyclerView.setLayoutManager(linearLayoutManager);

        database.getReference().child("notification")
                .child(auth.getCurrentUser().getUid())
                .limitToLast(18)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        models.clear();
                        if (snapshot.exists()){
                            for (DataSnapshot snapshot1 : snapshot.getChildren()){
                                NotificationModel notificationModel = snapshot1.getValue(NotificationModel.class);

                                if (notificationModel != null){
                                    notificationModel.setNotificationId(snapshot1.getKey());
                                    notificationModel.setId(notificationModel.getId());
                                    notificationModel.setType(notificationModel.getType());
                                    notificationModel.setSeen(notificationModel.getSeen());
                                    notificationModel.setNotifyAt(notificationModel.getNotifyAt());
                                    notificationModel.setNotifyBy(notificationModel.getNotifyBy());
                                    models.add(notificationModel);
                                }
                            }
                            Collections.reverse(models);
                            adapter = new NotificationAdapter(models, NotificationAct.this);
                            binding.notificationRecyclerView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                            binding.textNoComments.setVisibility(View.GONE);
                            binding.spinKit.setVisibility(View.GONE);

                            if (models.isEmpty()){
                                binding.clearAll.setVisibility(View.GONE);
                            }else {
                                binding.clearAll.setVisibility(View.VISIBLE);
                            }

                        }else {
                            binding.clearAll.setVisibility(View.GONE);
                            binding.spinKit.setVisibility(View.GONE);
                            binding.textNoComments.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.spinKit.setVisibility(View.GONE);
                        Toast.makeText(NotificationAct.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        binding.backComments.setOnClickListener(v->{
            finish();
        });

        binding.clearAll.setOnClickListener(v->{
            dialog.show();
            database.getReference().child("notification")
                    .child(auth.getCurrentUser().getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            models.clear();
                            if (snapshot.exists()){
                                for (DataSnapshot snapshot1 : snapshot.getChildren()){
                                    NotificationModel notificationModel = snapshot1.getValue(NotificationModel.class);
                                    database.getReference().child("notification")
                                            .child(auth.getCurrentUser().getUid())
                                            .child(snapshot1.getKey())
                                            .removeValue();
                                }
                                adapter.notifyDataSetChanged();
                                dialog.dismiss();
                            }else {
                                binding.textNoComments.setVisibility(View.VISIBLE);
                                dialog.dismiss();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            dialog.dismiss();
                        }
                    });
        });

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
//        UsagesModel usagesModel = new UsagesModel("Checked Notifications", startTime, currentTime);
//        arrayList.add(usagesModel);
//        CommonFeatures.writeListInPref(NotificationAct.this, arrayList);
//
//        super.onPause();
//    }
}
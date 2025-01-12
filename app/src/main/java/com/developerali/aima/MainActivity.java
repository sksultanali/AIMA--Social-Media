package com.developerali.aima;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentTransaction;

import com.developerali.aima.Activities.CheckMapActivity;
import com.developerali.aima.Activities.Login;
import com.developerali.aima.BottomBar.HomeUIFragment;
import com.developerali.aima.BottomBar.MeetingFragment;
import com.developerali.aima.BottomBar.MenuFragment;
import com.developerali.aima.BottomBar.ShortsFragment;
import com.developerali.aima.BottomBar.WadiNews;
import com.developerali.aima.Helpers.Helper;
import com.developerali.aima.Model_Apis.ApiService;
import com.developerali.aima.Model_Apis.BoundaryData;
import com.developerali.aima.Model_Apis.MapPointerResponse;
import com.developerali.aima.Model_Apis.RetrofitClient;
import com.developerali.aima.Model_Apis.UserDetails;
import com.developerali.aima.Models.NotificationRequest;
import com.developerali.aima.Notifications.AccessToken;
import com.developerali.aima.Notifications.NotificationRequestTopic;
import com.developerali.aima.databinding.ActivityMainBinding;
import com.developerali.aima.databinding.DialogNotLoginBinding;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;

import me.ibrahimsn.lib.OnItemSelectedListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity{

    ActivityMainBinding binding;
    FirebaseAuth auth;
    ApiService apiService;
    private static final String BASE_URL = "https://fcm.googleapis.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        apiService = RetrofitClient.getClient().create(ApiService.class);
        Helper.loadUserDetailsFromSharedPref(MainActivity.this);
        auth = FirebaseAuth.getInstance();

        //default fragment setting
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Intent intent = getIntent();
        Uri data = intent.getData();

        if (data != null && auth.getCurrentUser() == null) {
            transaction.replace(R.id.content, new MeetingFragment());
            transaction.commit();
            extractLink(data);
            showNotLoginDialog();
        }else if (data != null && auth.getCurrentUser() != null){
            //transaction.replace(R.id.content, new HomeFragment());
            transaction.replace(R.id.content, new HomeUIFragment());
            transaction.commit();
            extractLink(data);
        }else {
            //transaction.replace(R.id.content, new HomeFragment());
            transaction.replace(R.id.content, new HomeUIFragment());
            transaction.commit();
        }


        //bottom navigation bar
        binding.bottomBar.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public boolean onItemSelect(int i) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                switch (i) {
                    case 0:
                        //transaction.replace(R.id.content, new HomeFragment()).addToBackStack(null);
                        transaction.replace(R.id.content, new HomeUIFragment()).addToBackStack(null);
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














        FirebaseMessaging.getInstance().subscribeToTopic("/topics/users")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscribed";
                        if (!task.isSuccessful()) {
                            msg = "Subscribe failed";
                        }
                    }
                });
        checkNotification();
        //sendNotification();
    }

    public static void sendNotification(String token, String title, String body) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create API service
        ApiService apiService = retrofit.create(ApiService.class);

        // Prepare the notification request
        NotificationRequest notificationRequest = new NotificationRequest(
                new NotificationRequest.Message(
                        token,
                        new NotificationRequest.Notification(
                                title, body
                        )
                )
        );

        AccessToken accessTokenHelper = new AccessToken();
        accessTokenHelper.getAccessToken(new AccessToken.AccessTokenCallback() {
            @Override
            public void onTokenReceived(String token) {
                if (token != null) {
                    //Log.d("Token", "Token: " + token);
                    String authorization = "Bearer " + token;
                    apiService.sendNotificationToken(authorization, notificationRequest).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                //Log.d("FCM", "Notification sent successfully!");
                            } else {
                                //Log.d("FCM", "Error: " + response.message());
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            //Log.d("FCM", "Failure: " + t.getMessage());
                        }
                    });
                } else {
                    //Log.e("Token", "Failed to retrieve token.");
                }
            }
        });
    }
    public static void sendTopicNotification(String topic, String title, String body) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create API service
        ApiService apiService = retrofit.create(ApiService.class);

        // Prepare the notification request
        NotificationRequestTopic notificationRequest = new NotificationRequestTopic(
                new NotificationRequestTopic.Message(
                        topic,
                        new NotificationRequestTopic.Notification(
                                title, body
                        )
                )
        );

        AccessToken accessTokenHelper = new AccessToken();
        accessTokenHelper.getAccessToken(new AccessToken.AccessTokenCallback() {
            @Override
            public void onTokenReceived(String token) {
                if (token != null) {
                    //Log.d("Token", "Token: " + token);
                    String authorization = "Bearer " + token;
                    apiService.sendNotificationTopic(authorization, notificationRequest).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                //Log.d("FCM", "Notification sent successfully!");
                            } else {
                                //Log.d("FCM", "Error: " + response.message());
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            //Log.d("FCM", "Failure: " + t.getMessage());
                        }
                    });
                } else {
                    //Log.e("Token", "Failed to retrieve token.");
                }
            }
        });
    }

    private void checkNotification() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 201);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 201) {
            // Check if the permission was granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                // Permission denied, handle accordingly (e.g., show a message to the user)
                checkNotification();
            }
        }
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
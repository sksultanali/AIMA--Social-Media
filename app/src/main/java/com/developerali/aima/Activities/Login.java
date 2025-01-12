package com.developerali.aima.Activities;


import static com.reactnativegooglesignin.RNGoogleSigninModule.RC_SIGN_IN;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.developerali.aima.Helpers.Helper;
import com.developerali.aima.MainActivity;
import com.developerali.aima.Model_Apis.ApiResponse;
import com.developerali.aima.Model_Apis.ApiService;
import com.developerali.aima.Model_Apis.RetrofitClient;
import com.developerali.aima.Models.UserModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.BuildConfig;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Login extends AppCompatActivity {

    ActivityLoginBinding binding;
    String email, password;
    FirebaseAuth auth;
    FirebaseDatabase database;
    Uri uri;
    FirebaseStorage storage;
    GoogleSignInClient signInClient;
    ApiService apiService;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        apiService = RetrofitClient.getClient().create(ApiService.class);

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        }

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

//        binding.progressBar3 = new Progressbinding.progressBar3(Login.this);
//        binding.progressBar3.setTitle("Loading..");
//        binding.progressBar3.setMessage("Finding you profile");
//        binding.progressBar3.setCancelable(false);

        binding.signUpRedirect.setOnClickListener(v->{
            Intent i = new Intent(Login.this, SignUp.class);
            startActivity(i);
        });

        binding.loginBtn.setOnClickListener(v->{
            email = binding.loginEmail.getText().toString();
            password = binding.loginPassword.getText().toString();
            if (email.isEmpty()){
                binding.loginEmail.setError("Required");
            }else if (password.isEmpty()){
                binding.loginPassword.setError("Required");
            }else {

                binding.progressBar3.setVisibility(View.VISIBLE);

                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Intent i = new Intent(Login.this, MainActivity.class);
                            startActivity(i);
                            binding.progressBar3.setVisibility(View.GONE);
                            finish();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        binding.progressBar3.setVisibility(View.GONE);
                        Toast.makeText(Login.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });

            }

        });

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        signInClient = GoogleSignIn.getClient(Login.this, googleSignInOptions);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null){
            signInClient.signOut().addOnCompleteListener(this, task -> {

            });
        }

        binding.googleBtn.setOnClickListener(v->{
            signIn();
        });

        binding.fbBtn.setOnClickListener(v->{
            Helper.showAlertNoAction(Login.this,
                    "Denied Permission",
                    "For now, we are not allowing login with facebook. Please try after few days.",
                    "Okay");
        });

    }

    private void signIn() {
        Intent signInIntent = signInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN ){

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken(), account.getEmail(),
                        account.getDisplayName(), account.getPhotoUrl().toString());
                binding.progressBar3.setVisibility(View.VISIBLE);

            } catch (ApiException e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            }

        }

    }

    private void firebaseAuthWithGoogle(String idToken, String email, String name, String photoUrl) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        token = task.getResult();
                    }
                });

        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Call<ApiResponse> insertData;
                            if (photoUrl != null){
                                insertData = apiService.addNewUser(
                                        "insertOrUpdateUser",
                                        auth.getCurrentUser().getUid(),
                                        name,
                                        email,
                                        null,
                                        token,
                                        photoUrl
                                );
                            }else {
                                insertData = apiService.addNewUser(
                                        "insertOrUpdateUser",
                                        auth.getCurrentUser().getUid(),
                                        name,
                                        email,
                                        null,
                                        token,
                                        null
                                );
                            }
                            storeUserData(insertData);
//                            database.getReference().child("users").child(auth.getCurrentUser().getUid())
//                                    .setValue(userModel)
//                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                        @Override
//                                        public void onSuccess(Void unused) {
//                                            Intent i = new Intent(Login.this, MainActivity.class);
//                                            startActivity(i);
//                                            binding.progressBar3.setVisibility(View.GONE);
//                                            finish();
//                                        }
//                                    }).addOnFailureListener(new OnFailureListener() {
//                                        @Override
//                                        public void onFailure(@NonNull Exception e) {
//                                            binding.progressBar3.setVisibility(View.GONE);
//                                            Toast.makeText(Login.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Login.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void storeUserData(Call<ApiResponse> insertData) {
        insertData.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                binding.progressBar3.setVisibility(View.GONE);
                if (response.isSuccessful()){
                    ApiResponse response1 = response.body();
                    if (response1.getStatus().equalsIgnoreCase("success")){
                        Intent i = new Intent(Login.this, MainActivity.class);
                        startActivity(i);
                        finish();
                    }else {
                        Helper.showAlertNoAction(Login.this, "Failed",
                                response1.getMessage(), "Okay");
                    }
                }else {
                    Toast.makeText(Login.this, response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                binding.progressBar3.setVisibility(View.GONE);
                Toast.makeText(Login.this, t.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() != null){
            Intent i = new Intent(Login.this, MainActivity.class);
            startActivity(i);
            finish();
        }
    }
}
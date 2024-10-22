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

import com.developerali.aima.Helper;
import com.developerali.aima.MainActivity;
import com.developerali.aima.Models.UserModel;
import com.developerali.aima.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
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
import com.google.firebase.storage.FirebaseStorage;


public class Login extends AppCompatActivity {

    ActivityLoginBinding binding;
    String email, password;
    FirebaseAuth auth;
    //Progressbinding.progressBar3 binding.progressBar3;
    FirebaseDatabase database;
    Uri uri;
    FirebaseStorage storage;
    //GoogleSignInClient signInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

//        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
//        signInClient = GoogleSignIn.getClient(Login.this, googleSignInOptions);
//
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
//        if (account != null){
//            signInClient.signOut().addOnCompleteListener(this, task -> {
//
//            });
//        }

//        binding.googleBtn.setOnClickListener(v->{
//            signIn();
//        });

        binding.fbBtn.setOnClickListener(v->{
            Helper.showAlertNoAction(Login.this,
                    "Denied Access",
                    "For now, we are not allowing login with facebook. Please try after sometime.",
                    "Okay");
        });

    }

//    private void signIn() {
//        Intent signInIntent = signInClient.getSignInIntent();
//        startActivityForResult(signInIntent, RC_SIGN_IN);
//    }


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

        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            UserModel userModel = new UserModel();
                            userModel.setName(name);
                            userModel.setEmail(email);
                            if (photoUrl != null){
                                userModel.setImage(photoUrl);
                            }

                            database.getReference().child("users").child(auth.getCurrentUser().getUid())
                                    .setValue(userModel)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Intent i = new Intent(Login.this, MainActivity.class);
                                            startActivity(i);
                                            binding.progressBar3.setVisibility(View.GONE);
                                            finish();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            binding.progressBar3.setVisibility(View.GONE);
                                            Toast.makeText(Login.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Login.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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
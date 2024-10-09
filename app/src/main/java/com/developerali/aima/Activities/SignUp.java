package com.developerali.aima.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.developerali.aima.MainActivity;
import com.developerali.aima.Models.UserModel;
import com.developerali.aima.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class SignUp extends AppCompatActivity {

    ActivitySignUpBinding binding;
    String name, email, password, imageUrl;
    Uri selectedImage;
    FirebaseStorage storage;
    FirebaseDatabase database;
    FirebaseAuth auth;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        dialog = new ProgressDialog(SignUp.this);
        dialog.setTitle("Loading...");
        dialog.setMessage("Creating Profile");
        dialog.setCancelable(false);

        binding.loginRedirect.setOnClickListener(v->{
            Intent i = new Intent(SignUp.this, Login.class);
            startActivity(i);
        });

        binding.signUpImage.setOnClickListener(v->{
            Intent i = new Intent();
            i.setAction(Intent.ACTION_GET_CONTENT);
            i.setType("image/*"); //datatype bola holo
            startActivityForResult(i, 45);
        });

        binding.signUpBtn.setOnClickListener(v->{
            name = binding.signUpName.getText().toString();
            email = binding.SignUpEmail.getText().toString();
            password = binding.signUpPassword.getText().toString();
            if (name.isEmpty()){
                binding.signUpName.setError("Required");
            }else if (email.isEmpty()){
                binding.SignUpEmail.setError("Required");
            }else if (password.isEmpty()){
                binding.signUpPassword.setError("Empty");
            }else {

                dialog.show();

                if (selectedImage != null){

                    imageUpload(selectedImage, name, email, password);

                }else {

                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                String userId = task.getResult().getUser().getUid();
                                UserModel userModel = new UserModel(name, email);

                                database.getReference().child("users").child(userId)
                                        .setValue(userModel)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Intent i = new Intent(SignUp.this, MainActivity.class);
                                                startActivity(i);
                                                dialog.dismiss();
                                                finish();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                dialog.dismiss();
                                                Toast.makeText(SignUp.this, e.toString(), Toast.LENGTH_SHORT).show();

                                            }
                                        });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            if (e != null){
                                Toast.makeText(SignUp.this, e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getData() != null){
            selectedImage = data.getData();
            binding.signUpImage.setImageURI(data.getData());
            Toast.makeText(this, "image selected", Toast.LENGTH_SHORT).show();
        }
    }

    public void imageUpload(Uri imgData, String name, String email, String password){
        Toast.makeText(this, "uploading", Toast.LENGTH_SHORT).show();
        String key = database.getReference().push().getKey();
        StorageReference reference = storage.getReference().child("profiles").child(key);
        reference.putFile(imgData).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()){
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            imageUrl = uri.toString();
                            signUpNow(imageUrl, email, password, name);
                            Toast.makeText(SignUp.this, "uploaded success", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e != null){
                    Toast.makeText(SignUp.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void signUpNow(String image, String email, String password, String name) {

        Toast.makeText(this, "signing up", Toast.LENGTH_SHORT).show();
        
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    String userId = task.getResult().getUser().getUid();

                    UserModel userModel = new UserModel();
                    userModel.setEmail(email);
                    userModel.setName(name);
                    userModel.setPassword(password);
                    userModel.setImage(image);

                    database.getReference().child("users").child(userId)
                            .setValue(userModel)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(SignUp.this, "success", Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent(SignUp.this, MainActivity.class);
                                    startActivity(i);
                                    dialog.dismiss();
                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialog.dismiss();
                                    if (e != null){
                                        Toast.makeText(SignUp.this, e.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                if (e != null){
                    Toast.makeText(SignUp.this, e.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
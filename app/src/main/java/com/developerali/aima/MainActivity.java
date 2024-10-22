package com.developerali.aima;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.developerali.aima.Activities.Login;
import com.developerali.aima.BottomBar.HomeUIFragment;
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
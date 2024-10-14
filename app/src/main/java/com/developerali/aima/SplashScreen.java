package com.developerali.aima;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.developerali.aima.Activities.Login;

public class SplashScreen extends AppCompatActivity {

    private static final int SPLASH_DELAY = 4000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Hide navigation and status bars for an immersive experience
        hideSystemUI();

        // Create and start a new thread for the splash screen delay
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Sleep for the defined splash delay
                    Thread.sleep(SPLASH_DELAY);
                } catch (InterruptedException e) {
                    // Log the exception for debugging purposes
                    e.printStackTrace();
                } finally {
                    // Start the Login activity after the delay
                    Intent intent = new Intent(SplashScreen.this, Login.class);
                    startActivity(intent);
                    finish(); // Close the splash screen activity
                }
            }
        }).start();



    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public void onBackPressed() {
        // Prevent the user from going back during the splash screen
        super.onBackPressed();
    }
}
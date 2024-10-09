package com.developerali.aima.Activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.developerali.aima.R;
import com.developerali.aima.databinding.ActivityImageShowBinding;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ImageShow extends AppCompatActivity {

    ActivityImageShowBinding binding;
    String url;
    Activity activity;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImageShowBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        activity = ImageShow.this;

        url = getIntent().getStringExtra("image");
        if (url != null || !activity.isDestroyed()){
            Glide.with(ImageShow.this)
                    .load(url)
                    .placeholder(getDrawable(R.drawable.placeholder))
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(binding.imageShow);
        }


        binding.backImage.setOnClickListener(v->{
            finish();
        });

        binding.downloadImage.setOnClickListener(v->{
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // Permission is already granted, you can now take a screenshot
                takeScreenshot();
            } else {
                // Permission is not granted, request it
                ActivityCompat.requestPermissions(this, new String[]{
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        });





    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can now take a screenshot
                takeScreenshot();
            } else {
                // Permission denied, show a message or take appropriate action
                Toast.makeText(ImageShow.this, "Permission denied", Toast.LENGTH_SHORT).show();
                takeScreenshot();
            }
        }
    }

    private void takeScreenshot() {
        Bitmap screenshotBitmap = getScreenShot(binding.imageShow);

        if (screenshotBitmap != null) {
            saveScreenshot(screenshotBitmap);
        } else {
            Toast.makeText(ImageShow.this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap getScreenShot(View view) {
        Bitmap screenshot = null;
        try {
            if (view != null) {
                screenshot = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(screenshot);
                view.draw(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return screenshot;
    }

    private void saveScreenshot(Bitmap screenshot) {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault());
        String fileName = "AIMA_IMAGE_" + dateFormat.format(now) + ".png";

        try {
            // Save the screenshot to the device's gallery
            ContentResolver resolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");

            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

            OutputStream fos = resolver.openOutputStream(Objects.requireNonNull(imageUri));
            screenshot.compress(Bitmap.CompressFormat.PNG, 100, fos);
            if (fos != null) {
                fos.flush();
                fos.close();
            }

            Toast.makeText(ImageShow.this, "Image Saved in Gallery", Toast.LENGTH_SHORT).show();

            // Optionally, share the screenshot
            shareScreenshot(imageUri);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(ImageShow.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareScreenshot(Uri imageUri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/png");
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share Image via"));
    }


}
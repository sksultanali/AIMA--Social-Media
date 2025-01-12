package com.developerali.aima.Helpers;
import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class ImageDownloader {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private final Context context;

    public ImageDownloader(@NonNull Context context) {
        this.context = context;
    }

    // Public function to download an image
    public void downloadImage(@NonNull String imageUrl, @NonNull String fileName) {
        if (isPermissionGranted()) {
            initiateDownload(imageUrl, fileName);
        } else {
            requestPermission();
        }
    }

    // Check if storage permissions are granted
    private boolean isPermissionGranted() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    // Request storage permissions
    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(context, "Storage permission is required to download images.", Toast.LENGTH_SHORT).show();
        }
        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    // Handle permission result in your Activity
    public void handlePermissionResult(int requestCode, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Permission granted. Try downloading again.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Permission denied. Unable to download images.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Initiate the download process
    private void initiateDownload(@NonNull String imageUrl, @NonNull String fileName) {
        try {
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

            if (downloadManager == null) {
                throw new Exception("Download Manager not available.");
            }

            Uri uri = Uri.parse(imageUrl);
            DownloadManager.Request request = new DownloadManager.Request(uri);

            request.setTitle("Downloading " + fileName);
            request.setDescription("Please wait...");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(true);

            downloadManager.enqueue(request);
            Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
package com.developerali.aima.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.developerali.aima.databinding.ActivityWebViewBinding;

public class WebViewActivity extends AppCompatActivity {

    ActivityWebViewBinding binding;
    String link;

    private long startTime;
    private long totalSeconds;
    SharedPreferences sharedPreferences;
    private ValueCallback<Uri[]> mFilePathCallback;
    private static final int FILE_CHOOSER_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWebViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        link = getIntent().getStringExtra("provide");

        if (!link.isEmpty()){
            binding.webViewExternal.setWebViewClient(new WebViewClient());
            WebSettings settings = binding.webViewExternal.getSettings();
            settings.setLoadWithOverviewMode(true);
            settings.setUseWideViewPort(true);
            settings.setJavaScriptEnabled(true);
            settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
            settings.setDatabaseEnabled(false);
            settings.setDomStorageEnabled(false);
            settings.setGeolocationEnabled(false);
            settings.setSaveFormData(false);

            //setting the link
            binding.webViewExternal.loadUrl(link);

            binding.webViewExternal.setWebChromeClient(new WebChromeClient(){
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    super.onProgressChanged(view, newProgress);
                    binding.progressWebExternal.setProgress(newProgress);
                    binding.textProgressExternal.setText(newProgress +" % Loading...");
                    if (newProgress >= 90){
                        binding.spinKitExternal.setVisibility(View.GONE);
                        binding.textProgressExternal.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onReceivedTitle(WebView view, String title) {
                    super.onReceivedTitle(view, title);
                }

                @Override
                public void onReceivedIcon(WebView view, Bitmap icon) {
                    super.onReceivedIcon(view, icon);
                }

                // Override the onShowFileChooser method
                @Override
                public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                    // Create an intent to open the file picker
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");

                    // Start an activity for result with a request code
                    startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE);

                    // Store the ValueCallback for later use
                    mFilePathCallback = filePathCallback;

                    return true;
                }
            });




















        }else {
            Toast.makeText(this, "404 Broken !", Toast.LENGTH_SHORT).show();
            binding.spinKitExternal.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (mFilePathCallback != null) {
                Uri[] results = null;

                // Check if the file picker result is valid
                if (resultCode == Activity.RESULT_OK && data != null) {
                    String dataString = data.getDataString();
                    if (dataString != null) {
                        results = new Uri[]{Uri.parse(dataString)};
                    }
                }

                // Pass the selected file(s) to the WebView
                mFilePathCallback.onReceiveValue(results);
                mFilePathCallback = null;
            }
        }
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
//        UsagesModel usagesModel = new UsagesModel("Browsing on App", startTime, currentTime);
//        arrayList.add(usagesModel);
//        CommonFeatures.writeListInPref(WebViewActivity.this, arrayList);
//
//        super.onPause();
//    }

    @Override
    public void onBackPressed() {
        if (binding.webViewExternal.canGoBack()) {
            binding.webViewExternal.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
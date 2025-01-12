package com.developerali.aima.Helpers;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

import com.developerali.aima.Activities.WebViewActivity;
import com.developerali.aima.Model_Apis.BoundaryData;
import com.developerali.aima.Model_Apis.CountResponse;
import com.developerali.aima.Model_Apis.MapPointerResponse;
import com.developerali.aima.Models.UserModel;
import com.developerali.aima.R;
import com.developerali.aima.databinding.DialogNotLoginBinding;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Helper {

    public static UserModel userDetails;
    public static String tempId;
    public static double LATITUDE;
    public static double LONGITUDE;
    public static String NAME;
    public static String DESCRIPTION;
    public static MapPointerResponse.Data OPENED_DATA;

    public static String dateKey(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return date.format(formatter);
    }
    public static int dateKey() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return Integer.parseInt(today.format(formatter));
    }

    public static List<LatLng> convertToLatLngList(List<BoundaryData.Data> boundaryDataList) {
        List<LatLng> latLngList = new ArrayList<>();
        for (BoundaryData.Data data : boundaryDataList) {
            try {
                double latitude = Double.parseDouble(data.getLatitude());
                double longitude = Double.parseDouble(data.getLongitude());
                latLngList.add(new LatLng(latitude, longitude));
            } catch (NumberFormatException e) {
            }
        }
        return latLngList;
    }

//    public static String dateKey() {
//        LocalDate today = LocalDate.now();
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
//        return today.format(formatter);
//    }
    public static String dateKey(long date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate localDate = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate();
        return localDate.format(formatter);
    }
    public static String sQliDateKey(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(formatter);
    }
    public static LocalDate LongToLocalDate(long milliseconds) {
        Instant instant = Instant.ofEpochMilli(milliseconds);
        return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static long convertToLongTime(String dateTime) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(dateTime);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static String generateShareText(String postId) {
        return "🌟✨ Discover something amazing! ✨🌟\n\n" +
                "📱 Shared via the **AIMA App**! 🎉\n\n" +
                "🔥 Dive into this post directly from the app to explore more! 🚀\n\n" +
                "🔗 Post Link: https://i.aima.post/" + postId + "\n\n" +
                "📥 Download the AIMA App now and join the community! 💬❤️\n" +
                "👉 Download here: https://play.google.com/store/apps/details?id=com.developerali.aima";
    }

    public static void saveUserDetailsToSharedPref(Activity activity, UserModel userDetails) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String userDetailsJson = gson.toJson(userDetails);
        editor.putString("UserDetails", userDetailsJson);
        editor.apply();
    }

    public static void saveCountsToSharedPref(Activity activity, String fieldName, int countData) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(fieldName, countData);
        editor.apply();
    }

    public static void loadUserDetailsFromSharedPref(Activity activity) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
        String userDetailsJson = sharedPreferences.getString("UserDetails", null);
        if (userDetailsJson != null) {
            Gson gson = new Gson();
            Helper.userDetails = gson.fromJson(userDetailsJson, UserModel.class);
        } else {
            Log.e("UserDetails", "No user details found in SharedPreferences");
        }
    }

    public static String generateMapShareMessage(Activity activity){
        try {
            String encodedLabel, visibleName;
            if (Helper.NAME.equalsIgnoreCase("You are here")){
                encodedLabel = Uri.encode("My Location");
                visibleName = "Friend's Location";
            }else {
                encodedLabel = Uri.encode(Helper.NAME);
                visibleName = Helper.NAME;
            }
            Uri geoLocation = Uri.parse("https://maps.google.com/?q=" + Helper.LATITUDE + "," +
                    Helper.LONGITUDE + "&label=" + Uri.encode(encodedLabel));

            return "📍 Name: " + visibleName + "\n" +
                    Helper.DESCRIPTION +
                    "\n\n🔗 View on Map:\n" + geoLocation.toString() + "\n\n" +
                    "✨ This location is shared from AIMA - Social App. Discover amazing features! \n\n" +
                    "📲 Download our App Now:\n" +
                    "Google Play Store: \nhttps://play.google.com/store/apps/details?id=" + activity.getPackageName();
        }catch (Exception e){
            return "✨ This location is shared from AIMA - Social App. For the best experience. There are more features! \n\n" +
                    "📲 *Download our App Now!* " +
                    "[Google Play Store]\nhttps://play.google.com/store/apps/details?id=" + activity.getPackageName();
        }
    }


    public static void changeStatusBarColor(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(activity.getResources().getColor(color));
            View decor = window.getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    public static void changeStatusBarToDark(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(activity.getResources().getColor(color));
            View decor = window.getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    public static void showBadge(int verified, int valid, View imageView){
        int today = Helper.dateKey();
        if (verified != 0 && valid >= today){
            imageView.setVisibility(View.VISIBLE);
        }else {
            imageView.setVisibility(View.GONE);
        }
    }

    public static String formatDate(String inputTime, String inputFormat, String outputFormat) {
        try {
            // Parse and format date
            SimpleDateFormat inputFormatter = new SimpleDateFormat(inputFormat);
            SimpleDateFormat outputFormatter = new SimpleDateFormat(outputFormat);
            Date date = inputFormatter.parse(inputTime);
            return outputFormatter.format(date);
        } catch (Exception e) {
            // Return a default value in case of failure
            return inputTime;
        }
    }

    @SuppressLint("ResourceAsColor")
    public static void showAlertNoAction(Activity activity, String title, String content, String yesText) {
        DialogNotLoginBinding dialogBinding = DialogNotLoginBinding.inflate(LayoutInflater.from(activity));

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(dialogBinding.getRoot())
                .create();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));

        dialogBinding.titleText.setText(title);
        dialogBinding.messageText.setText(Html.fromHtml(content));

        dialogBinding.yesBtnText.setText(yesText);
        dialogBinding.noBtn.setVisibility(View.GONE);

        dialogBinding.loginBtn.setOnClickListener(v->{
            dialog.dismiss();
        });

        dialog.show();
    }

    public static String getFutureDate(int monthsToAdd) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusMonths(monthsToAdd);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return futureDate.format(formatter);
    }

    public static void openLink(Activity activity, String link){
        if (Helper.isChromeCustomTabsSupported(activity)){
            Helper.openChromeTab(link, activity);
        }else {
            Intent i = new Intent(activity.getApplicationContext(), WebViewActivity.class);
            i.putExtra("provide", link);
            activity.startActivity(i);
        }
    }

    public static boolean isChromeCustomTabsSupported(@NonNull final Context context) {
        Intent serviceIntent = new Intent("android.support.customtabs.action.CustomTabsService");
        serviceIntent.setPackage("com.android.chrome");
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentServices(serviceIntent, 0);
        return !resolveInfos.isEmpty();
    }
    public static void openChromeTab(String link, Activity activity){
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        builder.setToolbarColor(ContextCompat.getColor(activity, R.color.backgroundBottomColour));
        CustomTabsIntent customTabsIntent = builder.build();
        customTabsIntent.launchUrl(activity, Uri.parse(link));
    }
}

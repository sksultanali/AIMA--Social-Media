package com.developerali.aima;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.Window;

import com.developerali.aima.Models.UsagesModel;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class CommonFeatures {

    public static void lowerColour(Window window, Resources resources){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            window.setNavigationBarColor(resources.getColor(R.color.backgroundBottomColour));
        }
    }

    private static final String LIST_KEY = "usages_list";

    public static void writeListInPref(Context context, ArrayList<UsagesModel> list) {
        Gson gson = new Gson();
        String jsonString = gson.toJson(list);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(LIST_KEY, jsonString);
        editor.apply();
    }

    public static ArrayList<UsagesModel> readListFromPref(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String jsonString = pref.getString(LIST_KEY, "");

//        Gson gson = new Gson();
//        Type type = new TypeToken<ArrayList<UsagesModel>>() {}.getType();
//        ArrayList<UsagesModel> list = gson.fromJson(jsonString, type);
//        return list;

        if (jsonString != null || !jsonString.isEmpty()) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<UsagesModel>>() {}.getType();
            ArrayList<UsagesModel> list = gson.fromJson(jsonString, type);

            if (list != null) {
                return list;
            } else {
                // Handle the case where the JSON string could not be parsed into a list
                // You might want to return an empty list or log an error here
                return new ArrayList<>();
            }
        } else {
            // Handle the case where the JSON string is empty or null
            // You might want to return an empty list or log an error here
            return new ArrayList<>();
        }
    }

    ArrayList<UsagesModel> arrayList;
    public ArrayList<UsagesModel> initiateArray(){
        if (arrayList == null){
            arrayList = new ArrayList<>();
        }
        return arrayList;
    }



}

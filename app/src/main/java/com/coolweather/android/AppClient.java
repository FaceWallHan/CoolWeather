package com.coolweather.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import org.litepal.LitePalApplication;

public class AppClient extends LitePalApplication {
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    private SharedPreferences preferences;
    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();
        preferences=getSharedPreferences("data",MODE_PRIVATE);
    }

    public String getBingPic() {
        return preferences.getString("bingPic",null);
    }

    public void setBingPic(String bingPic) {
        @SuppressLint("CommitPrefEdits")
        SharedPreferences.Editor editor=preferences.edit();
        editor.putString("bingPic",bingPic);
        editor.apply();
    }

    public String getWeather() {
        return preferences.getString("weather",null);
    }

    public void setWeather(String weather) {
        @SuppressLint("CommitPrefEdits")
        SharedPreferences.Editor editor=preferences.edit();
        editor.putString("weather",weather);
        editor.apply();
    }

    public static Context getContext() {
        return context;
    }
}

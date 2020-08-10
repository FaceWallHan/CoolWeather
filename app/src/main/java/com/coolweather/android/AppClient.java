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

    public static Context getContext() {
        return context;
    }
}

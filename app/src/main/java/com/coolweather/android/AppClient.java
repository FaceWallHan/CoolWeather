package com.coolweather.android;

import android.annotation.SuppressLint;
import android.content.Context;

import org.litepal.LitePalApplication;

public class AppClient extends LitePalApplication {
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context=getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}

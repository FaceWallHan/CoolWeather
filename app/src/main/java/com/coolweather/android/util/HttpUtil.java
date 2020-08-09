package com.coolweather.android.util;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {
    public static void sendOkHttpRequest(String address, Callback callback){
        OkHttpClient client=new OkHttpClient();
        String addressUrl = "http://guolin.tech/api/";
        Request request=new Request.Builder()
                .url(addressUrl +address)
                .build();
        client.newCall(request).enqueue(callback);
    }
}

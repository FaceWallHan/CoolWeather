package com.coolweather.android.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.coolweather.android.AppClient;
import com.coolweather.android.WeatherActivity;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    private AppClient appClient;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    /**
     * 重构！
     * 重构！
     * 重构！
     * */
    @Override
    public void onCreate() {
        super.onCreate();
        appClient= (AppClient) getApplication();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateBingPic();
        updateWeather();
        AlarmManager manager= (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour=8*60*60*1000;    //这是8小时的毫秒数
        long triggerAtTime= SystemClock.elapsedRealtime()+anHour;
        Intent i=new Intent(this,AutoUpdateService.class);
        PendingIntent pi=PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }
    /**
     * 更新必应每日一图
     * **/
    private void updateBingPic(){
        HttpUtil.sendOkHttpRequest("bing_pic", new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseText= Objects.requireNonNull(response.body()).string();
                appClient.setBingPic(responseText);
            }
        });
    }
    /**
     * 更新天气信息
     * */
    private void updateWeather(){
        String weatherString=appClient.getWeather();
        if (weatherString!=null){
            //有缓存时直接解析天气数据
            Weather weather= Utility.handleWeatherResponse(weatherString);
            assert weather != null;
            /**
             * assert <boolean表达式>
             * 如果<boolean表达式>为true，则程序继续执行。
             * 如果为false，则程序抛出AssertionError，并终止执行。
             * **/
            String mWeatherId=weather.basic.weatherId;
            String weatherUrl="weather?cityid="+mWeatherId+"&key="+ HttpUtil.key;
            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    String responseText= Objects.requireNonNull(response.body()).string();
                    Weather weather=Utility.handleWeatherResponse(responseText);
                    if (weather!=null&&weather.status.equals("ok")){
                        appClient.setWeather(responseText);
                    }
                }
            });
        }
    }
}

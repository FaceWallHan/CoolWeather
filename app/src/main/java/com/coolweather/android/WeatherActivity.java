package com.coolweather.android;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private ScrollView weather_layout;
    private TextView title_city,title_update_time,degree_text,weather_info_text;
    private LinearLayout forecast_layout;
    private TextView aqi_text,pm25_text,comfort_text,car_wash_text,sport_text;
    private AppClient appClient;
    private ImageView bing_pic_img;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_activity);
        inView();
        DisplayData();
        DisplayPic();
    }
    private void inView(){
        weather_layout=findViewById(R.id.weather_layout);
        title_city=findViewById(R.id.title_city);
        title_update_time=findViewById(R.id.title_update_time);
        degree_text=findViewById(R.id.degree_text);
        weather_info_text=findViewById(R.id.weather_info_text);
        forecast_layout=findViewById(R.id.forecast_layout);
        aqi_text=findViewById(R.id.aqi_text);
        pm25_text=findViewById(R.id.pm25_text);
        comfort_text=findViewById(R.id.comfort_text);
        car_wash_text=findViewById(R.id.car_wash_text);
        sport_text=findViewById(R.id.sport_text);
        bing_pic_img=findViewById(R.id.bing_pic_img);
        appClient= (AppClient) getApplication();
    }
    private void DisplayPic(){
        View decorView=getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        /**
         * if (Build.VERSION.SDK_INT>=21)
         * 实现让背景图和状态栏融合到一起的效果
         * */
        String bingPic=appClient.getBingPic();
        if (bingPic!=null){
            Glide.with(this).load(bingPic).into(bing_pic_img);
        }else {
            loadBingPic();
        }
    }
    /**
     * 加载必应每日一图
     * **/
    private void loadBingPic(){
        String urlBing="bing_pic";
        HttpUtil.sendOkHttpRequest(urlBing, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText= Objects.requireNonNull(response.body()).string();
                appClient.setBingPic(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(responseText).into(bing_pic_img);
                    }
                });
            }
        });
    }
    private void DisplayData(){
        String weatherString=appClient.getWeather();
        if (weatherString!=null){
            //有缓存时直接解析天气数据
            Weather weather= Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        }else {
            //无缓存时去服务器查询天气
            String weatherId=getIntent().getStringExtra("weather_id");
            /**
             *如何优雅地getIntent？？？
             * **/
            weather_layout.setVisibility(View.GONE);
            requestWeather(weatherId);
        }
    }
    /**
     * 根据天气id请求城市天气信息
     * */
    private void requestWeather(String weatherId){
        String key="9af04bbcb8cf4190a670075979abe59a";
        String weatherUrl="weather?cityid="+weatherId+"&key="+key;
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();

                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseText= Objects.requireNonNull(response.body()).string();
                final Weather weather=Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather!=null&&weather.status.equals("ok")){
                            appClient.setWeather(responseText);
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
    /**
     * 处理并显示Weather实体类中的数据
     * */
    private void showWeatherInfo(Weather weather){
        String cityName=weather.basic.cityName;
        String updateTime=weather.basic.update.updateTime.split(" ")[1];
        String degree=weather.now.temperature+"℃";
        String weatherInfo=weather.now.more.info;
        title_city.setText(cityName);
        title_update_time.setText(updateTime);
        degree_text.setText(degree);
        weather_info_text.setText(weatherInfo);
        forecast_layout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecast_layout,false);
            TextView date_text=view.findViewById(R.id.date_text);
            TextView info_text=view.findViewById(R.id.info_text);
            TextView max_text=view.findViewById(R.id.max_text);
            TextView min_text=view.findViewById(R.id.min_text);
            min_text.setText(forecast.temperature.min);
            date_text.setText(forecast.date);
            info_text.setText(forecast.more.info);
            max_text.setText(forecast.temperature.max);
            forecast_layout.addView(view);
        }
        if (weather.aqi!=null){
            aqi_text.setText(weather.aqi.city.aqi);
            pm25_text.setText(weather.aqi.city.pm25);
            String comfort="舒适度"+weather.suggestion.comfort.info;
            String car_wash="洗车指数"+weather.suggestion.carWash.info;
            String sport="运动建议"+weather.suggestion.sport.info;
            comfort_text.setText(comfort);
            car_wash_text.setText(car_wash);
            sport_text.setText(sport);
            weather_layout.setVisibility(View.VISIBLE);
        }
    }
}

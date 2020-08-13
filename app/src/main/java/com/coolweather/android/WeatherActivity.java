package com.coolweather.android;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.service.AutoUpdateService;
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
    public SwipeRefreshLayout swipe_refresh;
    private String mWeatherId;
    public DrawerLayout drawer_layout;
    private Button nav_button;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_activity);
        inView();
        setListener();
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
        swipe_refresh=findViewById(R.id.swipe_refresh);
        swipe_refresh.setColorSchemeResources(R.color.colorPrimary);
        nav_button=findViewById(R.id.nav_button);
        drawer_layout=findViewById(R.id.drawer_layout);
    }
    private void setListener(){
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
        nav_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer_layout.openDrawer(GravityCompat.START);
            }
        });
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
            mWeatherId=weather.basic.weatherId;
            showWeatherInfo(weather);
        }else {
            //无缓存时去服务器查询天气
            mWeatherId=getIntent().getStringExtra("weather_id");
            /**
             *如何优雅地getIntent？？？
             * **/
            weather_layout.setVisibility(View.GONE);
            requestWeather(mWeatherId);
        }
    }
    /**
     * 根据天气id请求城市天气信息
     * */
    public void requestWeather(String weatherId){
        String weatherUrl="weather?cityid="+weatherId+"&key="+HttpUtil.key;
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipe_refresh.setRefreshing(false);

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
                            mWeatherId=weather.basic.weatherId;
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipe_refresh.setRefreshing(false);
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
            Intent intent=new Intent(this, AutoUpdateService.class);
            startService(intent);
        }
    }
}

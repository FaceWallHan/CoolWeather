package com.coolweather.android;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class WeatherActivity extends AppCompatActivity {
    private ScrollView weather_layout;
    private TextView title_city,title_update_time,degree_text,weather_info_text;
    private LinearLayout forecast_layout;
    private TextView aqi_text,pm25_text,comfort_text,car_wash_text,sport_text;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_activity);
        inView();
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
    }
}

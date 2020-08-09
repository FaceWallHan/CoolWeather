package com.coolweather.android;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.coolweather.android.db.Province;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
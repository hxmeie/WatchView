package com.hxm.watchview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WatchView watchView=findViewById(R.id.watch);
        watchView.setTime(System.currentTimeMillis());
    }
}

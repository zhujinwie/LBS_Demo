package com.navgnss.none.app;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by ZhuJinWei on 2017/2/7.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //使用SDK前初始化
        SDKInitializer.initialize(getApplicationContext());
    }
}

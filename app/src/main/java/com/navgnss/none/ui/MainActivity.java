package com.navgnss.none.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.baidu.mapapi.map.MapView;
import com.navgnss.none.R;
import com.navgnss.none.service.MySocketService;

public class MainActivity extends AppCompatActivity {
    MapView mMapView;//地图控件
    MySocketService service;

    ServiceConnection mySocketServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent serviceIntent=new Intent(this, MySocketService.class);
        mySocketServiceConnection=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                service=((MySocketService.InterBinder)iBinder).getService();

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                service=null;
            }
        };

       bindService(serviceIntent, mySocketServiceConnection, Context.BIND_AUTO_CREATE);
        Log.d(MySocketService.TAG,"bindService!");
        mMapView= (MapView) findViewById(R.id.bmapView);

        //TODO 测试用，跳转测试界面


    }

    @Override
    protected void onDestroy() {
        unbindService(mySocketServiceConnection);
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }




}


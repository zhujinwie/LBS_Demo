package com.navgnss.none.ui;

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.DotOptions;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.navgnss.none.R;
import com.navgnss.none.constant.Constants;
import com.navgnss.none.service.MySocketService;
import com.navgnss.none.tool.PreferenceService;
import com.navgnss.none.tool.SateData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    MapView mMapView;//地图控件
    MySocketService service;
    UpdateLBSBroadcastReceiver mReceiver;
    ServiceConnection mySocketServiceConnection;
    BaiduMap mBaiduMap;
    List<LatLng> ls;
    OverlayOptions option,optionLine; //Marker点
    boolean onShow,isConnected;
    View settingMenu;
    EditText host_et,port_et;
    Button connnectSocket_btn;
    PreferenceService preferenceService;// sharedPerence设置类
    Map<String,String> params;
    String host_str;
    String port_str;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        bindSocketServoce();
        Log.d(MySocketService.TAG,"bindService!");

        mReceiver=new UpdateLBSBroadcastReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(Constants.UPDATE_LBS);
        filter.addAction(Constants.RESETTING_NETWORK);
        registerReceiver(mReceiver,filter);

        initView();

    }

    /**
     * 初始化 sharedPreference ，views
     * */
    private void initView() {
        preferenceService=new PreferenceService(this);
        params=new HashMap<>();
        ls=new ArrayList<>();
        params=preferenceService.getPreferences();
        host_str=params.get("host");
        port_str=params.get("port");
        host_et= (EditText) findViewById(R.id.host_et_settingmenu);
        port_et= (EditText) findViewById(R.id.port_et_settingmenu);
        host_et.setText(host_str);
        port_et.setText(port_str);

        mMapView= (MapView) findViewById(R.id.bmapView);
        settingMenu=findViewById(R.id.setting_menu_layout);
        LayoutTransition transition=new LayoutTransition();


        mBaiduMap=mMapView.getMap();


        connnectSocket_btn= (Button) findViewById(R.id.socket_btn_settingmenu);
        connnectSocket_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!isConnected) {


                    Intent intent = new Intent();
                    intent.setAction(Constants.START_SOCKET);
                    intent.putExtra("host", host_et.getText().toString());
                    intent.putExtra("port", Integer.valueOf(port_et.getText().toString()));

                    sendBroadcast(intent);
                    connnectSocket_btn.setText("正在连接主机...");
                    connnectSocket_btn.setClickable(false);

                }else{

                    Log.d(MySocketService.TAG,"onClick:stop socket！");
                    Intent intent2 =new Intent();
                    intent2.setAction(Constants.STOP_SOCKET);
                    sendBroadcast(intent2);

                    connnectSocket_btn.setText("正在断开连接...");
                    connnectSocket_btn.setClickable(false);

                }
            }
        });

    }

    /**
     * bindService
     * 绑定监听
     * */
    private void bindSocketServoce() {
        Intent serviceIntent=new Intent(MainActivity.this, MySocketService.class);
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
    }

    /**
     *
     * 切换设置界面
     * "跳转" button 点击事件
     * */
    public void startTestActivity(View v){
       /* Intent intent=new Intent(this,TestSocketActivity.class);

        startActivity(intent);*/


        if(settingMenu.getVisibility()==View.GONE)
            settingMenu.setVisibility(View.VISIBLE);
        else
            settingMenu.setVisibility(View.GONE);


    }



    /**
     * 显示轨迹折线图
     * "轨迹" button 点击事件
     * */
    public void showPath(View v){



    }

    @Override
    protected void onDestroy() {
        unbindService(mySocketServiceConnection);
        unregisterReceiver(mReceiver);
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

    public class UpdateLBSBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(final Context context, Intent intent) {

            String action=intent.getAction();
            if(action==Constants.UPDATE_LBS){
                Log.d(MySocketService.TAG,"UI接收到定位信息！");

                SateData sateData= (SateData) intent.getSerializableExtra("LOLA");

                float la = sateData.getLatitude();
                float lo = sateData.getLongitude();

                Log.d(MySocketService.TAG,"la="+la+",lo="+lo);
                LatLng lalo=gpsToBD(new LatLng(la,lo));
                ls.add(lalo);

                mBaiduMap.clear();
                //构建Marker图标
                BitmapDescriptor bitmap = BitmapDescriptorFactory
                        .fromResource(R.drawable.car_icon);
                //构建MarkerOption，用于在地图上添加Marker
                option = new MarkerOptions()
                        .position(lalo)
                        .icon(bitmap);
                //在地图上添加Marker，并显示
                mBaiduMap.addOverlay(option);
                //mMapView.getMap().clear();

            }
            else{
                
                int code=intent.getIntExtra("code",-1);
                Log.d(MySocketService.TAG,"UI接收到SocketService通知! code="+code);
                switch(code){
                    case 0:
                        Toast.makeText(MainActivity.this,"socket连接成功！设置信息已保存！",Toast.LENGTH_SHORT).show();

                        preferenceService.save(host_et.getText().toString(),Integer.valueOf(port_et.getText().toString()));

                        isConnected=true;
                        connnectSocket_btn.setText("断开");
                        connnectSocket_btn.setTextColor(Color.RED);
                        connnectSocket_btn.setClickable(true);

                        break;
                    default:

                        Toast.makeText(MainActivity.this,"与主机连接已断开!",Toast.LENGTH_SHORT).show();
                        isConnected=false;
                        connnectSocket_btn.setText("重新连接Socket");
                        connnectSocket_btn.setClickable(true);

                        break;


                }
            }

        }
    }

    public LatLng gpsToBD(LatLng sourceLatLng){

        CoordinateConverter converter=new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        converter.coord(sourceLatLng);
        return converter.convert();


    }


}


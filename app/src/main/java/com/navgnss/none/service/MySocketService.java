package com.navgnss.none.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;


import com.navgnss.none.constant.Constants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by ZhuJinWei on 2017/2/9.
 *
 * 主要实现项目的socket功能
 */

public class MySocketService extends Service implements Runnable{

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Binder binder;
    private Thread readThread;

    private String host;//IPAddress
    private int port;//端口

    public static final String TAG="OVERWATCH";

    private ServiceBroadcastReceiver sbr;

    /**
     * 绑定service
     * 开始读取线程
     * */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        Log.d(TAG,"service is onBind!");
        binder =new InterBinder();

        host="256.256.256"; //初始错误的ip地址
        port=-1; //初始错误的端口

        sbr=new ServiceBroadcastReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(Constants.START_SOCKET);
        filter.addAction(Constants.STOP_SOCKET);
        registerReceiver(sbr,filter);

       /* //测试错误ip/port能否鉴定
        connectService();*/


        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public void onDestroy() {
        Log.d(TAG,"MySocketService is onDestroy!");

        super.onDestroy();


    }
    /**
     * 解除绑定
     * */
    @Override
    public boolean onUnbind(Intent intent) {

        Log.d(TAG,"MySocketService is onUnbind!");
        unregisterReceiver(sbr);
        return super.onUnbind(intent);

    }

    /**
     * 循环 ，接收从服务端发来的数据
     *
     *
     * 1.检测网络
     * 2.检测socket
     * 3.使用reader接受数据
     * */
    @Override
    public void run() {

        connectService();


        try {
            while (true) {
                Thread.sleep(500);
                if(socket!=null&&!socket.isClosed()){//socket不为null且未关闭
                    if(socket.isConnected()){//socket是否成功连接
                        if(!socket.isInputShutdown()){
                            String content;
                            if((content=reader.readLine())!=null){
                                getMessage(content);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e){
            Log.d(TAG,"循环出现异常！");
            e.printStackTrace();

            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            //提示已经关闭socket
            sendBroadcastForException(4);
        }

    }
    /**
     * TODO 处理接收到的数据
     * */
    private void getMessage(String content) {
        Log.d(TAG,"service 解析出数据一条！ content="+content);

    }

    public class InterBinder extends Binder{
        public MySocketService getService(){
            return MySocketService.this;
        }
    }

    /**
     * 连接服务器
     * */
    private void connectService(){

      try{

          socket=new Socket();
          Log.d(TAG,"socket新建成功！");
          SocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName(host), port);
          Log.d(TAG,"socketAddress新建成功！/n+port="+port+",host="+host);

          socket.connect(socketAddress, 3000);
          Log.d(TAG,"socket成功链接！");
          reader=new BufferedReader(new InputStreamReader(socket.getInputStream(),"GBK"));

          writer=new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"GBK")));

      }
      catch (UnknownHostException e){
            Log.d(TAG,"ip地址或端口设置错误1！"+e.getMessage());
            sendBroadcastForException(1);
          return;
      }
      catch (SocketException e){
            Log.d(TAG,"socketException2!"+e.getMessage());
            sendBroadcastForException(2);
          return;
      }
      catch(SocketTimeoutException e){
            e.printStackTrace();
          Log.d(TAG,"TimeoutException3!"+"e.message="+e.getMessage()+"e.cause="+e.getCause());
            sendBroadcastForException(3);
          return;
      }
      catch(IOException e){
          Log.d(TAG,"socket连接出错!"+"e.message="+e.getMessage()+",e.cause="+e.getCause());
          return;
      }
      catch(RuntimeException e){
              e.printStackTrace();
              Log.d(TAG,"ip地址或端口设置错误4！"+e.getMessage()+e.getCause());
              sendBroadcastForException(1);
              return;
      }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 发送连接异常广播
     *
     * @param code
     *       code==1:ip或port设置有误
     *       code==2：socket连接异常
     *       code==3：网络连接异常
     *       code==4: 当前socket已关闭
     *
     * */
    public void sendBroadcastForException(int code) {
        Intent intent=new Intent();
        intent.putExtra("code",code);
        sendBroadcast(intent);
    }

    public class ServiceBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            String action=intent.getAction();

            if(action.equals(Constants.START_SOCKET)){
                Log.d(TAG,"service 接到广播，开始connectService！");
                host=  intent.getStringExtra("host");
                port= intent.getIntExtra("port",-1);
              //  connectService();
                readThread=new Thread(MySocketService.this);
                readThread.start();
            }
            else if(action.equals(Constants.STOP_SOCKET)){
                //closeConnect();
            }

        }
    }
    /**
     * 关闭socket
     * * */
    private void closeConnect() {
       try {
           if (!socket.isInputShutdown() || !socket.isOutputShutdown()) {
               socket.shutdownInput();
               socket.shutdownOutput();
           } else if (socket.isConnected() || !socket.isClosed()) {
               socket.close();
           } else if (readThread.isAlive()) {
               readThread.interrupt();
           }
           socket = null;
       }
       catch(Exception e){
           e.printStackTrace();
           sendBroadcastForException(4);
       }
    }
}

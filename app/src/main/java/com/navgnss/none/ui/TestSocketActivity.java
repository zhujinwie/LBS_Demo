package com.navgnss.none.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.navgnss.none.R;
import com.navgnss.none.constant.Constants;

public class TestSocketActivity extends AppCompatActivity {

    EditText host_et,port_et;
    Button start_btn,stop_btn;
    TextView message_tv;

    String host;
    int port;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_socket);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        initview();

    }

    private void initview() {
        host_et= (EditText) findViewById(R.id.ip_edittext_test_activity);
        port_et= (EditText) findViewById(R.id.port_edittext_test_activity);
        start_btn= (Button) findViewById(R.id.start_btn_test_activity);
        stop_btn= (Button) findViewById(R.id.stop_btn_test_activity);
        message_tv= (TextView) findViewById(R.id.message_tv_test_activity);

        host=host_et.getText().toString();
        port=Integer.getInteger(port_et.getText().toString());

    }

    public void onClick(View v){
        int id=v.getId();



        switch (id){
            case R.id.start_btn_test_activity:

                Intent intent=new Intent();
                intent.setAction(Constants.START_SOCKET);
                intent.putExtra("host",host);
                intent.putExtra("port",port);

                sendBroadcast(intent);
                break;
            case R.id.stop_btn_test_activity:
                Intent intent2 =new Intent();
                intent2.setAction(Constants.STOP_SOCKET);
                sendBroadcast(intent2);
                break;
        }
    }



}

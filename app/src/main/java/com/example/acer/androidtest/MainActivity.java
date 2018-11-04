package com.example.acer.androidtest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private IntentFilter mIntentFilter;
    private NetWorkChangeReceiver mNetWorkChangeReceiver;
    private LocalReceiver mLocalReceiver;

    private LocalBroadcastManager mLocalBroadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocalBroadcastManager=LocalBroadcastManager.getInstance(this);

        Button button=(Button)findViewById(R.id.bt1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent("com.example.acer.broadcast.LOCAL_BROADCAST");
                mLocalBroadcastManager.sendBroadcast(intent);
            }
        });

        mIntentFilter=new IntentFilter();
        mIntentFilter.addAction("com.example.acer.broadcast.LOCAL_BROADCAST");
        mLocalReceiver=new LocalReceiver();
        mLocalBroadcastManager.registerReceiver(mLocalReceiver,null);

        mNetWorkChangeReceiver=new NetWorkChangeReceiver();
        registerReceiver(mNetWorkChangeReceiver,mIntentFilter);

    }

    class NetWorkChangeReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            ConnectivityManager connectivityManager=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();

            if (networkInfo!=null&&networkInfo.isAvailable()){
                Toast.makeText(context,"network can use ",Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(context,"network can not use ",Toast.LENGTH_SHORT).show();
            }

        }
    }

    class LocalReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context,"收到本地广播 ",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocalBroadcastManager.unregisterReceiver(mLocalReceiver);
    }
}

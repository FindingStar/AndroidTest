package com.example.acer.androidtest;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BlueToothActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "BlueToothActivity";
    private String BlueToothService_Name="hello";
    private UUID BlueToothService_UUID=UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

//    蓝牙适配器
    private BluetoothAdapter mBluetoothAdapter;
//    广播接受器
    private BlueToothStateReceiver mBlueToothStateReceiver;
//    客户端线程
    private ConnectThread mConnectThread;
//    服务端线程
    private AcceptThread mAcceptThread;

    private RvAdapter mRvAdapter;
    private RecyclerView mRecyclerView;
    private EditText inputText;

    private RecyclerView mMessageView;
    private static MsgAdapter mMessageAdapter;

    private static Handler mHandler=new Handler(){
        @Override
        public void dispatchMessage(Message msg) {
            mMessageAdapter.addMessage((String)msg.obj);
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //permission granted!
                }
                return;
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        if (Build.VERSION.SDK_INT >= 6.0) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
        initUI();
//        updateUI();
        registRec();
    }

    private void initUI(){
        findViewById(R.id.open).setOnClickListener(this);
        findViewById(R.id.close).setOnClickListener(this);
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.stop).setOnClickListener(this);
        findViewById(R.id.send).setOnClickListener(this);

        inputText=findViewById(R.id.input);
        mRecyclerView=findViewById(R.id.devices);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRvAdapter=new RvAdapter(this);
        mRecyclerView.setAdapter(mRvAdapter);
        mRvAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onClick(BluetoothDevice device) {
                mConnectThread=new ConnectThread(device);
                mConnectThread.start();
            }
        });
        mMessageView=findViewById(R.id.msglist);
        mMessageView.setLayoutManager(new LinearLayoutManager(this));
        mMessageAdapter=new MsgAdapter(this);
        mMessageView.setAdapter(mMessageAdapter);
    }
////注意 update 使用方式，，，notifyDataSetChanged 一会在看看
//    public void updateUI(){
//
//        if (mRvAdapter==null){
//            mRvAdapter=new RvAdapter();
//            mRecyclerView.setAdapter(mRvAdapter);
//        }else {
//            mRvAdapter.notifyDataSetChanged();
//        }
//
//    }

    private void openBT(){
        if (mBluetoothAdapter==null){
            mBluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
        }

//        即设备不支持 蓝牙 ，获取都获取不到
        if (mBluetoothAdapter==null)

            return;
//        isEnabled  和enable  区别？？？？
        if (!mBluetoothAdapter.enable()){
//            该动作是 直接打开蓝牙 设置
            Intent bt=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(bt,0);
        }
        Log.d(TAG, "openBT: 打开蓝牙");
    }

    private void registRec(){
        mBlueToothStateReceiver=new BlueToothStateReceiver();
        IntentFilter intentFilter=new IntentFilter();
//        每扫描到一个设备，系统都会发送此广播（BluetoothDevice.ACTION_FOUNDE）   同理 ，后面那个是 搜索完毕 系统发生广播
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mBlueToothStateReceiver,intentFilter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==0){
            if (resultCode==RESULT_OK){
                mMessageAdapter.addMessage("用户同意打开蓝牙");
            }else if (resultCode==RESULT_CANCELED){
                mMessageAdapter.addMessage("用户拒绝打开蓝牙");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.open:
                openBT();
                mMessageAdapter.addMessage("打开蓝牙");
                if (mAcceptThread==null&&mBluetoothAdapter!=null){
                    mAcceptThread=new AcceptThread();
                    mAcceptThread.start();
                    mMessageAdapter.addMessage("启动服务线程");
                }
                break;
            case R.id.close:
                mBluetoothAdapter.disable();
                break;
            case R.id.start:
                if (mBluetoothAdapter!=null){
                    mRvAdapter.clearDevices();
                    mBluetoothAdapter.startDiscovery();
                    mMessageAdapter.addMessage("开始搜索蓝牙");
                }else {
                    openBT();
                    if (mBluetoothAdapter!=null){
                        mRvAdapter.clearDevices();
                        mBluetoothAdapter.startDiscovery();
                        mMessageAdapter.addMessage("开始搜索蓝牙");
                    }
                }
                break;
            case R.id.stop:
                if (mBluetoothAdapter!=null&&mBluetoothAdapter.isDiscovering()){
                    mBluetoothAdapter.cancelDiscovery();
                }
                break;
            case R.id.send:
                String msg=inputText.getText().toString();
                if (TextUtils.isEmpty(msg)){
                    Toast.makeText(this,"消息为空",Toast.LENGTH_SHORT).show();
//                    return 退出该函数
                    return;
                }
                if (mConnectThread!=null){
                    mConnectThread.write(msg);
                }else if (mAcceptThread!=null){
                    mAcceptThread.write(msg);
                }
                mMessageAdapter.addMessage("发送消息："+msg);
                break;
        }
    }

    class BlueToothStateReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

            Toast.makeText(getApplicationContext(),"触发广播",Toast.LENGTH_SHORT).show();
//            注意下广播的 用法，，intent
            String action=intent.getAction();
            Log.d(TAG, "onReceive: 收到的action是"+action);
            switch (action){
                case BluetoothDevice.ACTION_FOUND:
                {
//                    注意这里有个device类
//                    这里    牛逼，直接 通过传来的intent  获取到设备信息
                    BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                    mDevices.add(device);
                    Toast.makeText(getApplicationContext(),"找到设备"+device.getName()+"////",Toast.LENGTH_SHORT).show();
                    if (mRvAdapter!=null){
                        mRvAdapter.addDevice(device);
                    }
                    break;
                }
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                {
                    Toast.makeText(getApplicationContext(),"搜索结束",Toast.LENGTH_SHORT).show();
                    mMessageAdapter.addMessage("搜索结束");
                    break;
                }
            }
        }
    }

    class AcceptThread extends Thread{
        private BluetoothServerSocket mBluetoothServerSocket;
        private BluetoothSocket mBluetoothSocket;

        private InputStream btInStream;
        private OutputStream btOutStream;

        private PrintWriter mPrintWriter;
        private boolean canAccept;
        private boolean canRecv;

        public AcceptThread(){
            canRecv=true;
            canAccept=true;
        }
        @Override
        public void run() {
            try{
//                第一一个参数 标示，没啥用，  第二个是  标示 使用蓝牙 的哪个服务 ，目前是串口通信spp
                BluetoothServerSocket temp=mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(BlueToothService_Name,BlueToothService_UUID);
                mBluetoothServerSocket=temp;
                if (mBluetoothServerSocket!=null){
                    mBluetoothSocket=mBluetoothServerSocket.accept();
                    sendHandleMessage("有客户端链接");
                }
                btInStream=mBluetoothSocket.getInputStream();
                btOutStream=mBluetoothSocket.getOutputStream();

                BufferedReader reader=new BufferedReader(new InputStreamReader(btInStream,"UTF-8"));
                String content=null;
                while (canRecv){
                    content=reader.readLine();
                    sendHandleMessage("收到消息："+content);
                }
            }catch (Exception e){
                e.printStackTrace();
                Log.d(TAG, "run: ");
            }finally {
                try {
                    if (mBluetoothSocket!=null){
                        mBluetoothSocket.close();
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }

            }
        }

        public void sendHandleMessage(String content){
            Message message=mHandler.obtainMessage();
            message.what=1001;
            message.obj=content;
            mHandler.sendMessage(message);
        }

        public void write(String msg){
            if (btOutStream!=null){
                try{
                    if (mPrintWriter==null){
                        mPrintWriter=new PrintWriter(new OutputStreamWriter(btOutStream,"UTF-8"),true);
                    }
                    mPrintWriter.println(msg);
                }catch (UnsupportedEncodingException e){
                    e.printStackTrace();
                    mPrintWriter.close();
                }
            }
        }
    }

    class ConnectThread extends Thread{

        private BluetoothDevice mBluetoothDevice;
        private BluetoothSocket mBluetoothSocket;
        private InputStream mClientInStream;
        private OutputStream mClientOutStream;
        private boolean canRecv;
        private PrintWriter mPrintWriter;

        public ConnectThread (BluetoothDevice device){
            mBluetoothDevice=device;
            canRecv=true;
        }

        @Override
        public void run() {
            try {
                BluetoothSocket  temp=mBluetoothDevice.createInsecureRfcommSocketToServiceRecord(BlueToothService_UUID);
                mBluetoothSocket=temp;
                if (mBluetoothSocket!=null){
                    mBluetoothSocket.connect();
                }
                sendHandleMessage("链接成功"+mBluetoothDevice.getName());

                mClientInStream=mBluetoothSocket.getInputStream();
                mClientOutStream=mBluetoothSocket.getOutputStream();

                BufferedReader reader=new BufferedReader(new InputStreamReader(mClientInStream,"UTF-8"));
                String content=null;
                while (canRecv){
                    content=reader.readLine();
                    sendHandleMessage("收到消息："+content);
                }

            }catch (IOException e){
                e.printStackTrace();
            }finally {
                try {
                    if (mBluetoothSocket!=null){
                        mBluetoothSocket.close();
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        public void sendHandleMessage(String content){
            Message message=mHandler.obtainMessage();
            message.what=1001;
            message.obj=content;
            mHandler.sendMessage(message);
        }
        public void write(String msg){
            if (mClientOutStream!=null){
                try{
                    if (mPrintWriter==null){
                        mPrintWriter=new PrintWriter(new OutputStreamWriter(mClientOutStream,"UTF-8"),true);
                    }
                    mPrintWriter.println(msg);
                }catch (UnsupportedEncodingException e){
                    e.printStackTrace();
                    mPrintWriter.close();
                    sendHandleMessage("错误:"+e.getMessage());
                }
            }
        }
    }

    class RvDeviceHolder extends RecyclerView.ViewHolder{

        private TextView nameTv;

        public RvDeviceHolder(View itemView) {
            super(itemView);
            nameTv=itemView.findViewById(R.id.name);
        }

//        public void bind(BluetoothDevice device){
//            nameTv.setText(device.getName());
//            addressTv.setText(device.getAddress());
//        }
    }
    class RvAdapter extends RecyclerView.Adapter<RvDeviceHolder>{
        private Context mContext;
        private List<BluetoothDevice> mDevices;
        private OnItemClickListener onItemClickListener;

        public void setOnItemClickListener(OnItemClickListener onItemClickListener){
            this.onItemClickListener=onItemClickListener;
        }

        public RvAdapter(Context context) {
            mContext=context;
            mDevices=new ArrayList<>();
        }

        @NonNull
        @Override
        public RvDeviceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater=LayoutInflater.from(parent.getContext());
            View view=inflater.inflate(R.layout.include_item_bt_rv,parent,false);
            return new RvDeviceHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RvDeviceHolder holder, final int position) {
            holder.nameTv.setText(mDevices.get(position).getName()+";"+mDevices.get(position).getAddress());
//            holder.bind(device);
            if (onItemClickListener!=null){
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(mContext,"你点击了item",Toast.LENGTH_SHORT).show();
                        onItemClickListener.onClick(mDevices.get(position));
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mDevices.size();
        }
        public void addDevice(BluetoothDevice device){
            mDevices.add(device);
            notifyItemInserted(mDevices.size()-1);
        }
        public void clearDevices(){
            mDevices.clear();
            notifyDataSetChanged();
        }
    }

    class MsgAdapter extends RecyclerView.Adapter<MsgHolder>{
        private Context mContext;
        private List<String>msgList;

        public MsgAdapter(Context context) {
            mContext=context;
            msgList=new ArrayList<>();
        }

        @NonNull
        @Override
        public MsgHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new MsgHolder(LayoutInflater.from(mContext).inflate(R.layout.include_item_bt_rv,parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull MsgHolder holder, int position) {
            holder.nameTv.setText(msgList.get(position));
        }

        @Override
        public int getItemCount() {
            return msgList.size();
        }
//        注意这个 添加后的鞥更新
        public void addMessage(String msg){
            msgList.add(msg);
            notifyItemInserted(msgList.size()-1);
        }
        public void clearList(){
            msgList.clear();
            notifyDataSetChanged();
        }

    }

    public interface OnItemClickListener{
        void onClick(BluetoothDevice device);
    }
    class MsgHolder extends RecyclerView.ViewHolder{

        private TextView nameTv;
        public MsgHolder(View itemView) {
            super(itemView);
            nameTv=itemView.findViewById(R.id.name);
        }
    }

    @Override
    protected void onDestroy() {
        if (mBlueToothStateReceiver!=null){
            unregisterReceiver(mBlueToothStateReceiver);
        }
        super.onDestroy();
    }
}

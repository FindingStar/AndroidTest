package com.example.acer.androidtest;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientActivity extends AppCompatActivity {

    /**
     * 测试 异步 线程，  以及简单的socker
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        Button button=findViewById(R.id.button_thread_extend_net);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MyNethread().start();
            }
        });

        Button button1=findViewById(R.id.button_thread_achieve_runnable);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new MyAchieveRunnable()).start();
            }
        });

        Button button2=findViewById(R.id.button_runnable_with_handler);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MyHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("异步线程","hanler.post+runnable  启动线程");
                    }
                });
            }
        });

        Button button3=findViewById(R.id.button_async_task);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MyAsyncTask().execute();
            }
        });

        Button button4=findViewById(R.id.button_runnable_with_handler);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyHandlerThread myHandlerThread=new MyHandlerThread("myHandlerThread");
                myHandlerThread.start();
                myHandlerThread.getLooper();

            }
        });
    }

    /**
     * 方式一： 通过继承Thread 实现
     */
    class MyNethread extends Thread{
        @Override
        public void run() {
            Log.d("异步线程","继承父类thread：run被调用");
            try{
                Log.d("MyThread","Client：Connecting");

                Socket socket=new Socket("192.168.43.140",12341);

                String message="我是phone";

                try{
                    Log.d("MyThread","Client Sending: '" + message + "'");

                    PrintWriter out=new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);

                    out.println(message);
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    socket.close();
                    Log.d("MyThread","Client:Socket closed");

                }

            }catch (UnknownHostException e1){
                e1.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 方式二： 通过实现Runnable接口  +  new Thread(Runnable)
     */
    class MyAchieveRunnable implements Runnable{

        @Override
        public void run() {
            Log.d("异步线程测试","MyAchieveRunnable： run 已经执行");
        }
    }
    /**
     * 方式三： 通过handler 实现   handler。post +thread
     */
    class MyHandler extends Handler{

    }

    /**
     * 方式四： 通过AsyncTask 实现
     *        注意！！！: 只有DoInBackground 在异步线程执行任务
     */
    class MyAsyncTask extends AsyncTask<Void,Void,Void>{


        @Override
        protected Void doInBackground(Void... voids) {
            Log.d("异步线程","AsyncTask doInBackGround  执行");
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }
    /**
     * 方式五： 通过HandlerThread 实现
     */
    class MyHandlerThread extends HandlerThread{

        public MyHandlerThread(String name) {
            super(name);
        }

        @Override
        protected void onLooperPrepared() {
            Handler handler=new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                }
            };
        }
    }
}

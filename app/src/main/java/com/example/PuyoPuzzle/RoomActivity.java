package com.example.PuyoPuzzle;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Locale;

public class RoomActivity extends AppCompatActivity implements Serializable {
    private  static int PORT = 58082;

    private Thread mServerThread = null;
    private TextView mTVMember;
    private TextView mTVAddress;
    private TextView mTVPort;
    private int member;
    private boolean  isFull = false;
    private ArrayList<Socket> sockets = new ArrayList<>();


    private Handler handler;
    private AppCompatActivity currentActivity;


    private ServerSocket mServerSocket;
    private int currentMember = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_main);
        mTVMember = (TextView) findViewById(R.id.member);
        findViewById(R.id.btnStopServer).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                stopServer();
            }
        });

        Log.v("KKT", "RoomActivity onCreate()");
        Intent intent1 = getIntent();
        member = intent1.getExtras().getInt("member");

        mTVAddress = (TextView) findViewById(R.id.tvAddress);
        mTVPort = (TextView)findViewById(R.id.tvPort);

        startServer();

    }

    private void startServer(){
        if(mServerThread == null){
            Log.v("KKT", "startServer() before SimpleServerThread()");
            mServerThread = new SimpleServerThread(){
                @Override
                public void run(){
                    Log.v("KKT", "SimpleServerThread run()"+ PORT);
                    try{
                        mServerSocket = new ServerSocket(PORT);
                        while(true){

                            mTVMember.setText((member-currentMember) + "명 대기중");

                            if(currentMember >= member)
                                break;

                            Log.v("KKT", "SimpleServerThread run() before accept");

                            Socket connection = mServerSocket.accept();
                            sockets.add(connection);

                            currentMember = currentMember + 1;

                            Log.v("KKT", "SimpleServerThread run() after accept");
                        }

                        for(int i = 0; i<member-1; i++){
                            Socket socket = sockets.get(i);
                            final PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                            out.write("member" + "\n");
                            out.flush();
                            out.write(Integer.toString(member)+"\n");
                            out.flush();
                        }

                        // 자기 자신을 제외한 연결된 소켓들에게 방 시작한다는 메시지 전달해줘야한다.
                        for(int i = 0; i<member-1; i++){
                            Socket socket = sockets.get(i);
                            final PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                            out.write("start" + "\n");
                            out.flush();
                        }

//                        Socket socket = null;
//                        socket = new Socket("192.168.0.1", 80);
//                        final Socket finalSocket = socket;
//
//                        String ip0 = finalSocket.getInetAddress().toString();
//                        ip0 = ip0.substring(1, ip0.length());
//
//
//                        for(int i = 0; i<member-1; i++){
//                            Socket socket = sockets.get(i);
//                            final PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//                            out.write("ip" + "\n");
//
//
//
//                            out.write("")
//                            out.flush();
//                        }


                        MyApplication myApp = (MyApplication) getApplication();
                        myApp.setSockets(sockets);
                        myApp.setCurrentMember(member);

                    }catch(IOException e){
                        e.printStackTrace();
                    }

                    finish();
                    Intent intent = new Intent(RoomActivity.this, MainActivityForServer.class);
                    startActivity(intent);
                }

                @Override
                public void closeServer() throws IOException {
                    if (mServerSocket != null && !mServerSocket.isClosed()) {
                        mServerSocket.close();
                    }
                    mServerSocket = null;
                }
            };
            Log.v("KKT", "startServer() after SimpleServerThread()");
            mServerThread.start();
        }
        Log.v("KKT", "setLocalIPAddress()");
        setLocalIPAddress();
    }

    private void stopServer(){
        try{
            if(mServerThread != null){
                ((SimpleServerThread) mServerThread).closeServer();
            }
            mServerThread = null;
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            mTVAddress.setText(String.format(Locale.US, "%s", "STOPPED"));
        }
    }

    private void setLocalIPAddress(){
        Log.v("KKT", "RoomActivity setLocalIPAddress()");
        new Thread(){
            public void run(){
                Socket socket = null;
                try{
                    socket = new Socket("192.168.0.1", 80);
                    Log.v("KKT", "RoomActivity setLocalIPAddress() socket 생성 성공");
                    final Socket finalSocket = socket;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTVAddress.setText(String.format(Locale.US, "IP: %s", finalSocket.getLocalAddress()));
                            Log.v("KKT", finalSocket.getLocalAddress().toString());
                        }
                    });
                }catch (UnknownHostException e){
                    e.printStackTrace();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }.start();
        mTVPort.setText(String.format(Locale.US, "PORT: %d", PORT));
    }

    public void setFull(boolean full) {
        isFull = full;
    }
}

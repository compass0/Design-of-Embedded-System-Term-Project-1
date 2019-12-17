package com.example.PuyoPuzzle;

import android.content.Intent;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static java.lang.Integer.parseInt;

public class SimpleClientThread extends Thread {

    private static String GIVEN_SERVER;
    private static int GIVEN_PORT;
    private static final int TIMEOUT=5000;

    private Socket mSocket;
    private boolean mShouldStop;



    private AppCompatActivity gameMainAcitivity;

    public SimpleClientThread(String addr, int port, AppCompatActivity gameMainActivity){
        Log.v("KKT", "SimpleClientThread Constructor()");
        GIVEN_SERVER = addr;
        GIVEN_PORT = port;

        mSocket = null;
        mShouldStop = true;



        this.gameMainAcitivity = gameMainActivity;
    }

    @Override
    public void run(){
        try {
            Log.v("KKT", "SimpleClientThread run()");
            Log.d("CLIENT THREAD", "connecting to" + GIVEN_SERVER + ":" + GIVEN_PORT);
            mSocket = new Socket(GIVEN_SERVER, GIVEN_PORT);
            mSocket.setSoTimeout(TIMEOUT);
            Log.v("KKT", "Socket 생성 성공");

            final BufferedReader in = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            final PrintWriter out = new PrintWriter(mSocket.getOutputStream(), true);

            MyApplication myApp = (MyApplication) gameMainAcitivity.getApplication();
            String s;
            s = in.readLine();
            if(s.equals("member")){
                s = in.readLine();
                myApp.setCurrentMember(Integer.parseInt(s));
            }

            myApp.setServerSocket(mSocket);

            s = in.readLine();
            if(s.equals("start")){
                gameMainAcitivity.finish();
                Intent intent = new Intent(gameMainAcitivity, MainActivityForClient.class);
                gameMainAcitivity.startActivity(intent);
            }




//            Socket socket;
//            String ip = null;
//            String port = null;
//            ArrayList<Socket> sockets = new ArrayList<>();
//            s = in.readLine();
//            while(s.equals("end")){
//                if(s.equals("ip")){
//                    ip = in.readLine();
//                }
//                else if(s.equals("port")){
//                    port = in.readLine();
//                }
//                socket = new Socket(ip, Integer.parseInt(port));
//                sockets.add(socket);
//            }
//
//            myApp.setSockets(sockets);


        }catch (IOException e){
            e.printStackTrace();
        }finally{
            if(mSocket != null){
                try{
                    mSocket.close();
                }catch (IOException e){

                }
            }
            mSocket = null;
            mShouldStop = true;
        }
    }

    public void closeConnection() throws IOException{
        mShouldStop = true;
        if(mSocket != null){
            mSocket.close();
        }
        mSocket = null;
    }

}

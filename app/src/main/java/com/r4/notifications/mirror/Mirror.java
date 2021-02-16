package com.r4.notifications.mirror;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

//TODO Background SocketServer service
//for the time after notification with reply was transmitted
//ping every few minutes afterwards
//TODO wait for secret
//TODO receive replies in Background service
//TODO reply then
class Mirror extends AsyncTask<MirrorNotification, Void, Void> { //deprecated
    private final static String TAG = "Mirror";

    private Socket mSocket;
    private DataOutputStream mDataOutputStream;
    private ObjectOutputStream outputStream = null;
    private PrintWriter mWriter;


    private String HOST_IP = "192.168.178.84"; //DEFAULT VALUES
    private int HOST_PORT = 9001; //DEFAULT VALUES

    protected Mirror() {
    }

    protected Mirror(String IP, int PORT) {
        this.HOST_IP = IP;
        this.HOST_PORT = PORT;
    }

    @Override
    protected Void doInBackground(MirrorNotification... mnts) {

        MirrorNotification notification = mnts[0];
        Log.d(TAG, "doInBackground: Starting Async Socket Connection to mirror");
        try {
            Log.d(TAG, "Trying to Connect to Socket " + HOST_IP + ":" + HOST_PORT);
            mSocket = new Socket();//HOST_IP, HOST_PORT);
//            mSocket.checkConnect();
//            InetAddress inetAddress = InetAddress.getByName("192.168.178.10");
//            SocketAddress socketAddress = new InetSocketAddress(inetAddress, 9999);
//            mSocket.bind(socketAddress);
            mSocket.connect(new InetSocketAddress(HOST_IP, HOST_PORT), 1000);
            Log.d(TAG, "Socket Connected");

            String jason = new Gson().toJson(notification);
//            outputStream = new ObjectOutputStream(mSocket.getOutputStream());
//            outputStream.writeObject(notification);
//            outputStream.flush();
//            outputStream.close();
            mWriter = new PrintWriter(mSocket.getOutputStream());//,true);
//            mPrintWriter.println(message);
            mWriter.write(jason);
            mWriter.flush();
            mWriter.close();
            mSocket.close();
            Log.d(TAG + "doInBackground", "Notification successfully Mirrored");
        } catch (IOException e) {
            Log.e(TAG + "doInBackground", "SOCKET CONNECTION FAILED " + HOST_IP + ":" + HOST_PORT, e);
//            Helper.toasted("Couldnt connect to Socket to mirror Notification");
        }
        Log.d(TAG + "doInBackground", "Ending Asynctask, Closed Socket");
        return null;
    }
}
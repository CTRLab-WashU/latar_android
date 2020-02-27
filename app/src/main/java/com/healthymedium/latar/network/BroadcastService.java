package com.healthymedium.latar.network;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class BroadcastService extends Service {

    private static final String TAG = BroadcastService.class.getSimpleName();

    // ------------------------------------------------------------------------

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public BroadcastService getService() {
            return BroadcastService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // ------------------------------------------------------------------------

    private ReceiveListener receiveListener = null;
    private int receivePort = 4172;

    private BroadcastTask broadcastTask;
    DatagramSocket socket;

    // ------------------------------------------------------------------------

    public BroadcastService() {

    }

    public void startReceiving(Context context, int port, ReceiveListener listener) {
        receivePort = port;
        receiveListener = listener;

        if(broadcastTask != null) {
            broadcastTask.isReceiving = false;
        }

        broadcastTask = new BroadcastTask();
        broadcastTask.execute();
    }

    public boolean stopReceiving() {
        if(broadcastTask != null) {
            broadcastTask.isReceiving = false;
        }
        return true;
    }

    public class BroadcastTask extends AsyncTask<Void, Void, Void> {

        public boolean isReceiving = false;
        @Override
        protected Void doInBackground(Void... nothing) {
            try {
                //Keep a socket open to listen to all the UDP trafic that is destined for this port
                socket = new DatagramSocket(receivePort, InetAddress.getByName("0.0.0.0"));
                socket.setBroadcast(true);

                isReceiving = true;
                while (isReceiving) {
                    Log.i(TAG,"ready to receive broadcast packets");

                    //Receive a packet
                    byte[] recvBuf = new byte[256];
                    DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                    socket.receive(packet);
                    byte[] data = packet.getData();

                    int i = data.length;
                    while (i-- > 0 && data[i] == 32) {}
                    Log.i(TAG,"packet received");

                    byte[] trimmed = new byte[i+1];
                    System.arraycopy(data, 0, trimmed, 0, i+1);

                    if(receiveListener != null){
                        String hostAddress = packet.getAddress().getHostAddress();
                        Message message = new Message(trimmed);
                        receiveListener.onMessageReceived(message,hostAddress);
                    }
                }
            } catch (IOException ex) {
                Log.i(TAG, ex.getMessage());
            }
            isReceiving = false;
            return null;
        }
    }


    public interface ReceiveListener {
        void onMessageReceived(Message message, String address);
    }

}

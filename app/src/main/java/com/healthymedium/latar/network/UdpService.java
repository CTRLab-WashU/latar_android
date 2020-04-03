package com.healthymedium.latar.network;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

public class UdpService extends Service {

    private static final String TAG = UdpService.class.getSimpleName();

    // service stuff ----------------------------------------------------------

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public UdpService getService() {
            return UdpService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // local variables --------------------------------------------------------

    private Listener listener = null;
    private int receivePort = 0;

    private ListeningTask task;
    DatagramSocket socket;

    // ------------------------------------------------------------------------

    public UdpService() {

    }

    // callback setters -------------------------------------------------------

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void removeListener() {
        this.listener = null;
    }

    // process control --------------------------------------------------------

    public void startListening(int port) {
        receivePort = port;

        if(task != null) {
            task.isReceiving = false;
        }

        task = new ListeningTask();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public boolean stopListening() {
        if(task != null) {
            task.isReceiving = false;
        }
        return true;
    }

    public boolean send(DatagramPacket packet){
        if(socket==null){
            return false;
        }
        
        try {
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    // task definition --------------------------------------------------------

    public class ListeningTask extends AsyncTask<Void, Void, Void> {

        public boolean isReceiving = false;
        @Override
        protected Void doInBackground(Void... nothing) {
            try {
                // keep a socket open to listen to all the UDP trafic that is destined for this port
                socket = new DatagramSocket(new InetSocketAddress(InetAddress.getLocalHost(),receivePort));
                socket.setReuseAddress(true);
                socket.setBroadcast(true);

                isReceiving = true;
                while (isReceiving) {

                    // receive a packet
                    byte[] recvBuf = new byte[256];
                    DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                    socket.receive(packet);

                    if(listener!=null) {
                        listener.onReceived(packet);
                    }

                }
            } catch (IOException ex) {
                Log.i(TAG, ex.getMessage());
            }
            isReceiving = false;
            return null;
        }
    }

    // public interfaces ------------------------------------------------------

    public interface Listener {
        void onReceived(DatagramPacket packet);
    }

}

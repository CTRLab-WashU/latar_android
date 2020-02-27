package com.healthymedium.latar.network;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Connection extends Service {

    private static final String tag = "Connection";
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public Connection getService() {
            return Connection.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    Handler handler = new Handler(Looper.getMainLooper());

    private static final int STATE_TCP_DISCONNECTED = 5;
    private static final int STATE_TCP_CONNECTED = 6;
    private static final int STATE_TCP_CONNECTING = 7;
    public int STATE_TCP = STATE_TCP_DISCONNECTED;

    private List<MessageListener> messageListeners = new ArrayList<>();
    private List<ConnectionListener> connectionListeners = new ArrayList<>();
    private TcpTask tcpTask;

    private ByteArrayOutputStream serverMessage;
    public String serverAddress = "";
    public int serverPort;

    private DataInputStream bufferIn;
    private DataOutputStream bufferOut;
    private boolean runningTcp = false;

    public Connection() {

    }

    public void connect(String address, int port){
        disconnect();
        serverAddress = address;
        serverPort = port;

        tcpTask = new TcpTask();
        tcpTask.execute();
    }

    public void disconnect(){
        runningTcp = false;
        if (bufferOut != null) {
            try {
                bufferOut.flush();
                bufferOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        bufferIn = null;
        bufferOut = null;
        serverMessage = new ByteArrayOutputStream();;
    }

    public boolean isConnected(){
        return STATE_TCP==STATE_TCP_CONNECTED;
    }

    public void addConnectionListener(ConnectionListener connectionListener){
        connectionListeners.add(connectionListener);
    }

    public void removeConnectionListener(ConnectionListener connectionListener){
        connectionListeners.remove(connectionListener);
    }

    public void removeAllConnectionListeners(){
        connectionListeners.clear();
    }

    public void addMessageListener(MessageListener messageListener){
        messageListeners.add(messageListener);
    }

    public void removeMessageListener(MessageListener messageListener){
        messageListeners.remove(messageListener);
    }

    public void removeAllMessageListeners(){
        messageListeners.clear();
    }

    public class TcpTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... nothing) {
            runningTcp = true;
            STATE_TCP = STATE_TCP_CONNECTING;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.i(tag,"connecting");
                }
            });

            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(serverAddress, serverPort), 1000);

                try {
                    bufferIn = new DataInputStream(socket.getInputStream());
                    bufferOut = new DataOutputStream(socket.getOutputStream());
                    STATE_TCP = STATE_TCP_CONNECTED;

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(tag,"connected");
                            for(ConnectionListener listener : connectionListeners){
                                if (listener != null) {
                                    listener.onConnected();
                                }
                            }
                        }
                    });
                    while (runningTcp) {
                        int read = bufferIn.read();
                        if(read != -1){
                            serverMessage.write(read);
                            if (serverMessage != null && read==Message.END) {
                                Message message = new Message(serverMessage.toByteArray());
                                for(MessageListener listener : messageListeners){
                                    if (listener != null) {
                                        listener.onMessageReceived(message);
                                    }
                                }
                                serverMessage.reset();
                            }
                        }
                    }

                } catch (final Exception e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(tag,"error: " + e);
                            for(ConnectionListener listener : connectionListeners){
                                if (listener != null) {
                                    listener.onError(e);
                                }
                            }
                        }
                    });
                } finally {
                    socket.close();
                }
                STATE_TCP = STATE_TCP_DISCONNECTED;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(tag,"disconnected");
                        for(ConnectionListener listener : connectionListeners){
                            if (listener != null) {
                                listener.onDisconnected();
                            }
                        }
                    }
                });
            } catch (final Exception e) {
                runningTcp = false;
                STATE_TCP = STATE_TCP_DISCONNECTED;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(tag,"error: " + e);
                        for(ConnectionListener listener : connectionListeners){
                            if (listener != null) {
                                listener.onError(e);
                            }
                        }
                    }
                });
            }
            return null;
        }
    }

    public void sendMessage(char command){
        sendMessage(command,null);
    }

    public void sendMessage(char command, byte[] body){
        Message message = new Message();
        message.setEnquiry();
        message.setCommand(command);
        if(body != null) {
            message.setBody(body);
        }
        sendMessage(message);
    }

    public void sendMessage(Message message) {
        if (bufferOut != null) {
            try {
                bufferOut.write(message.toBuffer());
                bufferOut.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public interface ConnectionListener {
        void onConnected();
        void onDisconnected();
        void onError(Exception e);
    }

    public interface MessageListener {
        void onMessageReceived(Message message);
    }

}

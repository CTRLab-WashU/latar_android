package com.healthymedium.latar;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.healthymedium.latar.navigation.NavigationManager;
import com.healthymedium.latar.network.Commands;
import com.healthymedium.latar.network.TcpConnection;
import com.healthymedium.latar.network.Message;
import com.healthymedium.latar.network.UdpService;
import com.healthymedium.latar.network.models.ClockUpdate;
import com.healthymedium.latar.network.models.DeviceInfo;
import com.healthymedium.latar.network.models.DisplayParams;
import com.healthymedium.latar.screens.CalibrationScreen;
import com.healthymedium.latar.screens.DisplayLatencyScreen;
import com.healthymedium.latar.screens.HomeScreen;
import com.healthymedium.latar.screens.TapLatencyScreen;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.WIFI_SERVICE;

public class Proctor {

    private static Proctor instance;

    // ------------------------------------------------------------------------

    private List<ClockUpdate> clockUpdates = new ArrayList();

    private static InetAddress serverAddress;
    private boolean addressFound = false;

    private final int tcpPort = 4032;
    private final int udpPort = 4172;

    // ------------------------------------------------------------------------

    WifiManager wifiManager;
    WifiManager.WifiLock wifiLock;

    TcpConnection connection;
    UdpService udpService;
    Listener listener;
    Gson gson;

    // ------------------------------------------------------------------------


    private Proctor(Context context){
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .setLenient()
                .create();

        wifiManager = (WifiManager)context.getSystemService(WIFI_SERVICE);
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF,"WIFI_LOCK");
        wifiLock.acquire();
    }

    public static Proctor get(){
        if(instance==null){
            instance = new Proctor(Application.getInstance());
        }
        return instance;
    }

    public void connect(){
        if(connection==null){
            return;
        }
        if(serverAddress==null) {
            return;
        }
        connection.connect(serverAddress.getHostAddress(), tcpPort);
    }

    public void disconnect(){
        if(connection==null){
            return;
        }
        connection.disconnect();
    }

    public boolean isConnected(){
        if(connection==null){
            return false;
        }
        return get().connection.isConnected();
    }

    public static TcpConnection getTcpConnection(){
        return get().connection;
    }

    public void bind(AppCompatActivity activity) {
        Intent tcpIntent = new Intent(activity, TcpConnection.class);
        activity.bindService(tcpIntent, tcpServiceConnection, Context.BIND_AUTO_CREATE);

        Intent udpIntent = new Intent(activity, UdpService.class);
        activity.bindService(udpIntent, udpServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbind(AppCompatActivity activity) {
        activity.unbindService(udpServiceConnection);
        activity.unbindService(tcpServiceConnection);
    }

    TcpConnection.MessageListener messageListener = new TcpConnection.MessageListener() {
        @Override
        public void onMessageReceived(Message message) {
            char cmd = message.getCommand();
            switch (cmd){
                case Commands.CLOCK_UPDATE:
                    message.setAcknowledgement(false);
                    message.setBody(gson.toJson(clockUpdates));
                    clockUpdates.clear();
                    connection.sendMessage(message);
                    break;
                case Commands.DEVICE_INFO:
                    sendDeviceInfo();
                    break;
                case Commands.DEVICE_IDENTIFY:
                    break;
                case Commands.CALIBRATION_SETUP:
                    NavigationManager.getInstance().popBackStack();
                    NavigationManager.getInstance().open(new CalibrationScreen());
                    break;
                case Commands.DISPLAY_START:
                    DisplayParams params = gson.fromJson(message.getBodyAsString(),DisplayParams.class);
                    NavigationManager.getInstance().popBackStack();
                    NavigationManager.getInstance().open(new DisplayLatencyScreen(params));
                    break;
                case Commands.TAP_START:
                    NavigationManager.getInstance().popBackStack();
                    NavigationManager.getInstance().open(new TapLatencyScreen());
                    message.setAcknowledgement(false);
                    connection.sendMessage(message);
                    break;
                case Commands.TAP_STOP:
                    Log.i("Proctor","TAP_STOP");
                    NavigationManager.getInstance().popBackStack();
                    NavigationManager.getInstance().open(new HomeScreen());
                    break;
            }

        }
    };

    TcpConnection.Listener tcpListener = new TcpConnection.Listener() {
        @Override
        public void onConnected() {

        }

        @Override
        public void onDisconnected() {

        }

        @Override
        public void onError(Exception e) {

        }
    };

    UdpService.Listener udpListener = new UdpService.Listener() {
        @Override
        public void onReceived(DatagramPacket packet) {
            byte[] data = packet.getData();

            for(int i=0; i<data.length; i++) {
                byte value = data[i];

                switch (value) {
                    case Commands.CLOCK_SYNC:
                        DeviceClock.zero();
                        sendDatagram(value);
                        Log.i("Proctor", "Device Clock Zero'd");
                        break;
                    case Commands.BROADCAST:
                        if(!addressFound) {
                            addressFound = true;
                            serverAddress = packet.getAddress();
                            Log.i("Proctor", "Found server at : "+serverAddress.getHostAddress());
                            listener.onServerFound(serverAddress.getHostAddress());
                        }
                        break;
                    case 0:

                        break;
                    default:
                        long timestamp = DeviceClock.now();
                        sendDatagram(value);
                        ClockUpdate update = new ClockUpdate();
                        update.timestamp = timestamp;
                        update.index = Byte.valueOf(value).intValue();
                        clockUpdates.add(update);
                        break;
                }

            }

        }
    };
    // ---------------------------------------------------------------------------------------------

    private void sendDeviceInfo(){
        DeviceInfo info = DeviceInfo.getInstance();
        String string = gson.toJson(info);

        Message message = new Message();
        message.setCommand(Commands.DEVICE_INFO);
        message.setAcknowledgement(false);
        message.setBody(string);
        connection.sendMessage(message);
    }

    private void sendDatagram(byte value) {
        byte data[] = new byte[1];
        data[0] = value;

        if(serverAddress!=null) {
            DatagramPacket packet = new DatagramPacket(data, 1, serverAddress, udpPort);
            udpService.send(packet);
        }
    }

    // ---------------------------------------------------------------------------------------------

    private ServiceConnection tcpServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            TcpConnection.LocalBinder binder = (TcpConnection.LocalBinder) service;
            TcpConnection tcpConnection = binder.getService();
            if(tcpConnection==null){
                return;
            }
            if(connection != null) {
                connection.disconnect();
                connection.removeMessageListener(messageListener);
                connection.removeListener(tcpListener);
                connection = null;
            }
            tcpConnection.addMessageListener(messageListener);
            tcpConnection.addListener(tcpListener);
            connection = tcpConnection;
            if(listener!=null) {
                listener.onServiceBound();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if(connection==null){
                return;
            }
            connection.disconnect();
            connection.removeMessageListener(messageListener);
            connection.removeListener(tcpListener);
            connection = null;
            if(listener!=null) {
                listener.onServiceUnbound();
            }
        }
    };

    // ---------------------------------------------------------------------------------------------

    private ServiceConnection udpServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            UdpService.LocalBinder binder = (UdpService.LocalBinder) service;
            udpService = binder.getService();
            udpService.setListener(udpListener);
            udpService.startListening(4172);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public boolean setServerAddress(String string){
        try {
            InetAddress address = InetAddress.getByName(string);
            addressFound = true;
            serverAddress = address;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean hasServerAddress(){
        return (serverAddress!=null);
    }

    public String getServerAddress(){
        if(serverAddress==null){
            return "Unknown";
        }
        return serverAddress.getHostAddress();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void removeListener(){
        this.listener = null;
    }

    public interface Listener {
        void onServiceBound();
        void onServiceUnbound();
        void onServerFound(String address);
    }

}

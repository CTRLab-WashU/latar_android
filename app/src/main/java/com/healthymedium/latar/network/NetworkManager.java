package com.healthymedium.latar.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.healthymedium.latar.Application;

import java.util.List;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.WIFI_SERVICE;

public class NetworkManager {

    private static NetworkManager instance;

    ConnectivityManager connectivityManager;
    WifiManager wifiManager;
    WifiManager.WifiLock wifiLock;
    BroadcastReceiver receiver;
    int networkId;

    String ssid;
    String passcode;

    private NetworkManager(){
        Context context = Application.getInstance().getApplicationContext();
        connectivityManager = (ConnectivityManager)context.getSystemService(CONNECTIVITY_SERVICE);
        wifiManager = (WifiManager)context.getSystemService(WIFI_SERVICE);
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF,"WIFI_LOCK");
        wifiLock.acquire();
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("NetworkManager",intent.toString());
            }
        };
    }

    public static NetworkManager getInstance(){
        if(instance==null){
            instance = new NetworkManager();
        }
        return instance;
    }

    public boolean isWirelessEnabled(){
        return wifiManager.isWifiEnabled();
    }

    public boolean setWirelessEnabled(boolean enabled){
        return wifiManager.setWifiEnabled(enabled);
    }

    public boolean isConnected(){
        NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }

    public boolean isConnected(String ssid){
        NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(mWifi.isConnected()) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String currentSSID = wifiInfo.getSSID().replace("\"","");
            return ssid.equals(currentSSID);
        } else {
            return false;
        }
    }

    public boolean connect(){

        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                // connect to our network
                Log.i("NetworkManager","onAvailable "+network);
//                connect(ssid,passcode);

            }

            @Override
            public void onLost(Network network) {
                // connect to our network
                Log.i("NetworkManager","onLost "+network.toString());
            }
        };

        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
//        builder.setNetworkSpecifier("");
        connectivityManager.registerNetworkCallback(builder.build(), networkCallback);
        return true;
    }

    public boolean connect(String ssid, String key){
        if(!isWirelessEnabled()){
            return false;
        }
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", ssid);
        wifiConfig.preSharedKey = String.format("\"%s\"", key);

        boolean exists = false;
        List<WifiConfiguration> configList = wifiManager.getConfiguredNetworks();
        for(WifiConfiguration config : configList){
            if(config.SSID.equals(wifiConfig.SSID)){
                networkId = config.networkId;
                exists = true;
                break;
            }
        }
        if(!exists){
            networkId = wifiManager.addNetwork(wifiConfig);
        }

        if(!isConnected(ssid)){
            wifiManager.disconnect();
            wifiManager.enableNetwork(networkId,true);
            return wifiManager.reconnect();
        }
        return true;
    }

    public BroadcastReceiver getReceiver() {
        return receiver;
    }

    public IntentFilter getReceiverIntentFilter(){
        return new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
    }
}

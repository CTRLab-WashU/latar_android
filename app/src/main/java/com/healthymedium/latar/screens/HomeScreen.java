package com.healthymedium.latar.screens;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.healthymedium.latar.BaseFragment;
import com.healthymedium.latar.R;
import com.healthymedium.latar.network.BroadcastService;
import com.healthymedium.latar.network.Connection;
import com.healthymedium.latar.network.Message;
import com.healthymedium.latar.network.models.DeviceInfo;

@SuppressLint("ValidFragment")
public class HomeScreen extends BaseFragment {

    private static String TCP_ADDRESS = "Unknown";
//private static final String TCP_ADDRESS = "192.168.1.112";
    private static final int TCP_PORT = 4032;
    private static boolean addressFound = false;
    private static boolean serviceBound = false;

    BroadcastService broadcastService;

    TextView textViewDeviceInfo;
    TextView textViewStatus;
    TextView textViewAddress;
    Button button;

    public HomeScreen() {

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .setLenient()
                .create();
        DeviceInfo deviceInfo = DeviceInfo.getInstance();
        textViewDeviceInfo = view.findViewById(R.id.textViewDeviceInfo);
        textViewDeviceInfo.setText(gson.toJson(deviceInfo));

        textViewAddress = view.findViewById(R.id.textViewAddress);
        textViewAddress.setText("Server Address: "+TCP_ADDRESS);
        textViewStatus = view.findViewById(R.id.textViewStatus);
        button = view.findViewById(R.id.button);
        if(getConnection().isConnected()){
            button.setText("Disconnect");
            button.setOnClickListener(OnClickListenerDisconnect);
            textViewStatus.setText("Connected");
        } else {
            button.setText("Connect");
            button.setOnClickListener(OnClickListenerConnect);
            textViewStatus.setText("Not Connected");
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getConnection().addConnectionListener(connectionListener);
        if(!addressFound) {
            Intent intentConnection = new Intent(getMainActivity(), BroadcastService.class);
            getMainActivity().bindService(intentConnection, serviceConnection, Context.BIND_AUTO_CREATE);
            serviceBound = true;
        }

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                NavigationManager.getInstance().popBackStack();
//                //DisplayParams params = new DisplayParams();
//                //params.count = 20;
//                //params.interval = 500;
//                //NavigationManager.getInstance().open(new DisplayLatencyScreen(params));
//                NavigationManager.getInstance().open(new TapLatencyScreen());
//            }
//        },2000);
    }

    @Override
    public void onPause() {
        super.onPause();
        getConnection().removeConnectionListener(connectionListener);
        if(serviceBound) {
            getMainActivity().unbindService(serviceConnection);
            serviceBound = false;
        }
    }


    Connection.ConnectionListener connectionListener = new Connection.ConnectionListener() {
        @Override
        public void onConnected() {
            button.setEnabled(false);
            button.setText("Disconnect");
            textViewStatus.setText("Connected");
            button.setOnClickListener(OnClickListenerDisconnect);
            button.setEnabled(true);
        }

        @Override
        public void onDisconnected() {
            button.setEnabled(false);
            button.setText("Connect");
            textViewStatus.setText("Not Connected");
            button.setOnClickListener(OnClickListenerConnect);
            button.setEnabled(true);
        }

        @Override
        public void onError(Exception e) {
            button.setEnabled(false);
            button.setText("Connect");
            textViewStatus.setText("Not Connected");
            button.setOnClickListener(OnClickListenerConnect);
            button.setEnabled(true);
        }
    };

    View.OnClickListener OnClickListenerConnect = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            button.setEnabled(false);
            button.setText("Connecting");
            textViewStatus.setText("");
            getConnection().connect(TCP_ADDRESS,TCP_PORT);
        }
    };

    View.OnClickListener OnClickListenerDisconnect = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            button.setEnabled(false);
            button.setText("");
            textViewStatus.setText("");
            getConnection().disconnect();
            button.setEnabled(true);
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            BroadcastService.LocalBinder binder = (BroadcastService.LocalBinder) service;
            broadcastService = binder.getService();
            broadcastService.startReceiving(getMainActivity(), 4172, new BroadcastService.ReceiveListener() {
                @Override
                public void onMessageReceived(Message message, String address) {
                    if(address != TCP_ADDRESS) {
                        TCP_ADDRESS = address;
                        textViewAddress.setText("Server Address: "+address);
                        broadcastService.stopReceiving();
                    }
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };




}

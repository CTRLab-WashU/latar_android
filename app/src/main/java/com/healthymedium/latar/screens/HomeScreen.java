package com.healthymedium.latar.screens;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
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
import com.healthymedium.latar.network.Connection;
import com.healthymedium.latar.network.models.DeviceInfo;

@SuppressLint("ValidFragment")
public class HomeScreen extends BaseFragment {

    private static final String TCP_ADDRESS = "192.168.191.153";
//private static final String TCP_ADDRESS = "192.168.1.112";
    private static final int TCP_PORT = 4032;

    Button button;
    TextView textViewDeviceInfo;
    TextView textViewStatus;

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

}

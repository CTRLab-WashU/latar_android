package com.healthymedium.latar;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.healthymedium.latar.navigation.NavigationManager;
import com.healthymedium.latar.network.Connection;
import com.healthymedium.latar.network.NetworkManager;
import com.healthymedium.latar.screens.HomeScreen;
import com.healthymedium.latar.utilities.ViewUtil;

public class MainActivity extends AppCompatActivity {

    Connection connection;
    Proctor proctor;
    boolean launched = false;

    public MainActivity() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intentConnection = new Intent(this, Connection.class);
        bindService(intentConnection, serviceConnection, Context.BIND_AUTO_CREATE);

        NetworkManager manager = NetworkManager.getInstance();
        registerReceiver(manager.getReceiver(),manager.getReceiverIntentFilter());
    }

    @Override
    protected void onStop() {
        super.onStop();
        proctor.unregisterConnection();
        unbindService(serviceConnection);
        unregisterReceiver(NetworkManager.getInstance().getReceiver());
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Connection.LocalBinder binder = (Connection.LocalBinder) service;
            connection = binder.getService();
            proctor.registerConnection(connection);
            NavigationManager.initialize(getSupportFragmentManager());
            NavigationManager.getInstance().open(new HomeScreen());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            NavigationManager.getInstance().clearBackStack();
            NavigationManager.getInstance().popBackStack();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        proctor = new Proctor();

        float px = 100;
        float in = 0.2f;
        Log.i("Conversion",px+" px = "+ ViewUtil.pxToIn(px)+" in");
        Log.i("Conversion",in+" in = "+ ViewUtil.inToPx(in)+" px");

    }


    public Connection getConnection(){
        return connection;
    }

}

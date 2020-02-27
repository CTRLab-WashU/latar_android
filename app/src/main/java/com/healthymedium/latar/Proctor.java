package com.healthymedium.latar;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.healthymedium.latar.navigation.NavigationManager;
import com.healthymedium.latar.network.Commands;
import com.healthymedium.latar.network.Connection;
import com.healthymedium.latar.network.Message;
import com.healthymedium.latar.network.models.DeviceInfo;
import com.healthymedium.latar.network.models.DisplayParams;
import com.healthymedium.latar.screens.CalibrationScreen;
import com.healthymedium.latar.screens.DisplayLatencyScreen;
import com.healthymedium.latar.screens.HomeScreen;
import com.healthymedium.latar.screens.TapLatencyScreen;

public class Proctor {

    Connection connection;
    Gson gson;

    public Proctor(){
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .setLenient()
                .create();
    }

    public boolean isReady(){
        return false;
    }

    public void registerConnection(Connection connection){
        if(connection==null){
            return;
        }
        unregisterConnection();
        connection.addMessageListener(messageListener);
        connection.addConnectionListener(connectionListener);
        this.connection = connection;
    }

    public void unregisterConnection(){
        if(connection==null){
            return;
        }
        connection.disconnect();
        connection.removeMessageListener(messageListener);
        connection.removeConnectionListener(connectionListener);
        this.connection = null;
    }

    Connection.MessageListener messageListener = new Connection.MessageListener() {
        @Override
        public void onMessageReceived(Message message) {
            char cmd = message.getCommand();
            switch (cmd){
                case Commands.CLOCK_SYNC:
                    DeviceClock.zero();
                    message.setAcknowledgement(false);
                    connection.sendMessage(message);
                    Log.i("Proctor","CLOCK_SYNC 0 ");
                    break;
                case Commands.CLOCK_UPDATE:
                    long timestamp = DeviceClock.now();
                    message.setAcknowledgement(false);
                    JsonObject obj = new JsonObject();
                    obj.addProperty("timestamp", timestamp);
                    message.setBody(gson.toJson(obj));
                    connection.sendMessage(message);
                    Log.i("Proctor","CLOCK_UPDATE = "+timestamp);
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

    Connection.ConnectionListener connectionListener = new Connection.ConnectionListener() {
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

}

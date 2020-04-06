package com.healthymedium.latar.screens;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.healthymedium.latar.BaseFragment;
import com.healthymedium.latar.DeviceClock;
import com.healthymedium.latar.Proctor;
import com.healthymedium.latar.R;
import com.healthymedium.latar.navigation.NavigationManager;
import com.healthymedium.latar.network.Commands;
import com.healthymedium.latar.network.Message;
import com.healthymedium.latar.network.TcpConnection;
import com.healthymedium.latar.network.models.TapLatencyData;
import com.healthymedium.latar.network.models.TouchCalibrationData;
import com.healthymedium.latar.utilities.ViewUtil;

import org.joda.time.DateTime;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("ValidFragment")
public class TouchCalibrationScreen extends BaseFragment {

    Gson gson;
    View testView;

    long startTime = 0;
    long stopTime = 0;
    List<Long> occurences = new ArrayList();


    public TouchCalibrationScreen() {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .setLenient()
                .create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        testView = inflater.inflate(R.layout.fragment_tap_latency, container, false);
        testView.setOnTouchListener(touchListener);

        startTime = DeviceClock.now();

        return testView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Proctor.getTcpConnection().addMessageListener(messageListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        Proctor.getTcpConnection().removeMessageListener(messageListener);
    }

    private void sendData(int index, int action, long actionTime, long callbackTime, float touchMajor, float touchMinor){
        TapLatencyData data = new TapLatencyData();
        data.index = index;
        data.action = action;
        data.actionTime = actionTime;
        data.callbackTime = callbackTime;
        data.actionName = (action==1 ? "UP":"DOWN");
        data.touchMajor = touchMajor;
        data.touchMinor = touchMinor;

        String string = gson.toJson(data);
        Log.i(getSimpleTag(),string);

        Message message = new Message();
        message.setCommand(Commands.TAP_DATA);
        message.setBody(string);

        Proctor.getTcpConnection().sendMessage(message);
    }

    View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            long now = DeviceClock.now();
            int action = event.getAction();

            if(action >= 2){
                return false;
            }

            if(action==MotionEvent.ACTION_DOWN) {
                occurences.add(now);
            }

            return true;
        }
    };


    TcpConnection.MessageListener messageListener = new TcpConnection.MessageListener() {
        @Override
        public void onMessageReceived(Message message) {
            char cmd = message.getCommand();
            switch (cmd){
                case Commands.CALIBRATION_TOUCH_STOP:
                    stopTime = DeviceClock.now();

                    TouchCalibrationData data = new TouchCalibrationData();
                    data.startTime = startTime;
                    data.stopTime = stopTime;
                    data.occurences = occurences;

                    String string = gson.toJson(data);
                    Log.i(getSimpleTag(),string);

                    message.setBody(string);
                    Proctor.getTcpConnection().sendMessage(message);

                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            NavigationManager.getInstance().popBackStack();
                        }
                    });
                    break;
            }
        }
    };



}

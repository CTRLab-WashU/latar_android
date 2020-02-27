package com.healthymedium.latar.screens;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.healthymedium.latar.BaseFragment;
import com.healthymedium.latar.DeviceClock;
import com.healthymedium.latar.R;
import com.healthymedium.latar.navigation.NavigationManager;
import com.healthymedium.latar.network.Commands;
import com.healthymedium.latar.network.Connection;
import com.healthymedium.latar.network.Message;
import com.healthymedium.latar.network.models.TapLatencyData;

import java.lang.reflect.Method;

@SuppressLint("ValidFragment")
public class CalibrationScreen extends BaseFragment {

    Gson gson;
    FrameLayout testView;

    public CalibrationScreen() {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .setLenient()
                .create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calibration, container, false);
        testView = view.findViewById(R.id.testView);
        testView.setOnTouchListener(touchListener);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Message message = new Message(Commands.CALIBRATION_SETUP);
        getConnection().sendMessage(message);
        getConnection().addMessageListener(messageListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        Message message = new Message(Commands.CALIBRATION_TEARDOWN);
        getConnection().sendMessage(message);
        getConnection().removeMessageListener(messageListener);
    }

    private long getEventTimeMicro(MotionEvent event) {
        long t_nanos = -1;
        try {
            Class cls = Class.forName("android.view.MotionEvent");
            Method getEventTimeNano = cls.getMethod("getEventTimeNano");
            t_nanos = (long) getEventTimeNano.invoke(event);
        } catch (Exception e) {
            Log.i(getSimpleTag(), e.getMessage());
        }
        long micro =  t_nanos / 1000;
        return DeviceClock.getTime(micro);
    }

    private void sendTapData(int action, long actionTime, long callbackTime){
        TapLatencyData data = new TapLatencyData();
        data.index = -1;
        data.action = action;
        data.actionTime = actionTime;
        data.callbackTime = callbackTime;
        data.actionName = (action==1 ? "UP":"DOWN");

        String string = gson.toJson(data);
        Log.i(getSimpleTag(),string);

        Message message = new Message();
        message.setCommand(Commands.CALIBRATION_TOUCH);
        message.setBody(string);

        getConnection().sendMessage(message);
    }

    Connection.MessageListener messageListener = new Connection.MessageListener() {
        @Override
        public void onMessageReceived(Message message) {
            char cmd = message.getCommand();
            switch (cmd){
                case Commands.CALIBRATION_DISPLAY:
                    int value = Integer.valueOf(message.getBodyAsString());
                    int color = (value==1)? Color.WHITE:Color.BLACK;
                    testView.setBackgroundColor(color);

                    message.setAcknowledgement(false);
                    getConnection().sendMessage(message);
                    break;
                case Commands.CALIBRATION_TEARDOWN:
                    testView.setOnTouchListener(null);
                    NavigationManager.getInstance().popBackStack();
                    break;
            }
        }
    };

    View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            long callbackTime = DeviceClock.now();
            long actionTime = getEventTimeMicro(event);
            int action = event.getAction();

            if(action >= 2){
                return false;
            }

            sendTapData(action,actionTime,callbackTime);
            return true;
        }
    };

}

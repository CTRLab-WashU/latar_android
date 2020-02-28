package com.healthymedium.latar.screens;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

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
import com.healthymedium.latar.utilities.ViewUtil;

import java.lang.reflect.Method;

@SuppressLint("ValidFragment")
public class TapLatencyScreen extends BaseFragment {

    Gson gson;
    View testView;
    int testIndex = 0;

    public TapLatencyScreen() {
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

        return testView;
    }

    @Override
    public void onResume() {
        Log.i(getSimpleTag(),"onResume");
        super.onResume();
//        getConnection().addMessageListener(messageListener);
    }

    @Override
    public void onPause() {
        Log.i(getSimpleTag(),"onPause");
//        getConnection().removeMessageListener(messageListener);
        super.onPause();
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

        getConnection().sendMessage(message);
    }

//    Connection.MessageListener messageListener = new Connection.MessageListener() {
//        @Override
//        public void onMessageReceived(Message message) {
//            char cmd = message.getCommand();
//            switch (cmd){
//                case Commands.TAP_START:
//                    testIndex = 0;
//                    testView.setOnTouchListener(touchListener);
//                    break;
//                case Commands.TAP_STOP:
//                    testView.setOnTouchListener(null);
//                    NavigationManager.getInstance().popBackStack();
//                    NavigationManager.getInstance().open(new HomeScreen());
//                    break;
//            }
//        }
//    };

    View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            long callbackTime = DeviceClock.now();
            long actionTime = getEventTimeMicro(event);
            int action = event.getAction();
            float touchMinor = ViewUtil.pxToIn(ViewUtil.dpToPx(event.getTouchMinor()));
            float touchMajor = ViewUtil.pxToIn(ViewUtil.dpToPx(event.getTouchMajor()));

            if(action >= 2){
                return false;
            }

            if(action==MotionEvent.ACTION_DOWN) {
                sendData(testIndex,action,actionTime,callbackTime,touchMajor,touchMinor);
                testIndex++;
            }

            return true;
        }
    };

}

package com.healthymedium.latar.screens;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.healthymedium.latar.BaseFragment;
import com.healthymedium.latar.DeviceClock;
import com.healthymedium.latar.Proctor;
import com.healthymedium.latar.R;
import com.healthymedium.latar.navigation.NavigationManager;
import com.healthymedium.latar.network.Commands;
import com.healthymedium.latar.network.TcpConnection;
import com.healthymedium.latar.network.Message;
import com.healthymedium.latar.network.models.DisplayLatencyData;
import com.healthymedium.latar.network.models.DisplayLatencySetup;
import com.healthymedium.latar.network.models.DisplayParams;

@SuppressLint("ValidFragment")
public class DisplayLatencyScreen extends BaseFragment {

    final int white = Color.WHITE;
    final int black = Color.BLACK;
    int color = white;
    int testIndex = 0;

    FrameLayout testView;

    DisplayParams params;
    CountDownTimer timer;
    Gson gson;

    public DisplayLatencyScreen(DisplayParams params) {
        this.params = params;
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .setLenient()
                .create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_screen_latency, container, false);
        testView = view.findViewById(R.id.testView);

        int millisInFuture = (params.count+1)*params.interval;

        timer = new CountDownTimer(millisInFuture, params.interval) {
            public void onTick(long millisUntilFinished) {
                long callbackTime = DeviceClock.now();
                testView.setBackgroundColor(color);
                long displayTime = DeviceClock.now();
                sendData(testIndex,color,callbackTime,displayTime);
                color = (color==white)?black:white;
                testIndex++;
            }

            public void onFinish() {
                Message message = new Message(Commands.DISPLAY_STOP);
                Proctor.getTcpConnection().sendMessage(message);
                Log.i(getSimpleTag(), "onFinish");
                NavigationManager.getInstance().popBackStack();
                NavigationManager.getInstance().open(new HomeScreen());
            }

        }.start();
        return view;
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

    private void sendData(int index, int color, long callbackTime, long displayTime){
        DisplayLatencyData data = new DisplayLatencyData();
        data.callbackTime = callbackTime;
        data.displayTime = displayTime;
        data.colorName = (color==white)?"WHITE":"BLACK";
        data.color = (color==white)?1:0;
        data.index = index;

        String string = gson.toJson(data);
        Log.i(getSimpleTag(),string);
        Log.i(getSimpleTag(),"delay = "+(displayTime-callbackTime)+"us");


        Message message = new Message();
        message.setCommand(Commands.DISPLAY_DATA);
        message.setBody(string);

        Proctor.getTcpConnection().sendMessage(message);
    }

    TcpConnection.MessageListener messageListener = new TcpConnection.MessageListener() {
        @Override
        public void onMessageReceived(Message message) {
            char cmd = message.getCommand();
            switch (cmd){
                case Commands.DISPLAY_START:
                    DisplayLatencySetup setup = gson.fromJson(message.getBodyAsString(), DisplayLatencySetup.class);
                    timer = new CountDownTimer((setup.count+1)*setup.interval, setup.interval) {

                        public void onTick(long millisUntilFinished) {
                            long callbackTime = DeviceClock.now();
                            testView.setBackgroundColor(color);
                            long displayTime = DeviceClock.now();
                            sendData(testIndex,color,callbackTime,displayTime);
                            color = (color==white)?black:white;
                            testIndex++;
                        }

                        public void onFinish() {
                            Message message = new Message(Commands.DISPLAY_STOP);
                            Proctor.getTcpConnection().sendMessage(message);
                            Log.i(getSimpleTag(), "onFinish");
                            NavigationManager.getInstance().popBackStack();
                        }

                    }.start();
                    break;
                case Commands.DISPLAY_STOP:
                    NavigationManager.getInstance().popBackStack();
                    break;
            }
        }
    };


}

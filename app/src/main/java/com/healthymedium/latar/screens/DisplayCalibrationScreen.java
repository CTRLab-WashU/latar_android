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
import com.healthymedium.latar.network.Message;
import com.healthymedium.latar.network.TcpConnection;
import com.healthymedium.latar.network.models.DisplayLatencyData;
import com.healthymedium.latar.network.models.DisplayLatencySetup;
import com.healthymedium.latar.network.models.DisplayParams;

@SuppressLint("ValidFragment")
public class DisplayCalibrationScreen extends BaseFragment {

    final int white = Color.WHITE;
    final int black = Color.BLACK;
    int color = white;
    int testIndex = 0;

    FrameLayout testView;

    CountDownTimer timer;
    Gson gson;

    public DisplayCalibrationScreen() {
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

        int interval = 500;
        int count = 3;

        int millisInFuture = count*interval;

        timer = new CountDownTimer(millisInFuture, interval) {
            public void onTick(long millisUntilFinished) {
                testView.setBackgroundColor(color);
                color = (color==white)?black:white;
                testIndex++;
            }

            public void onFinish() {
                Message message = new Message(Commands.CALIBRATION_DISPLAY_STOP);
                Proctor.getTcpConnection().sendMessage(message);
                Log.i(getSimpleTag(), "onFinish");
                NavigationManager.getInstance().popBackStack();
            }

        }.start();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}

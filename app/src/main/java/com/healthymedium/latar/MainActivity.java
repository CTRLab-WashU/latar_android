package com.healthymedium.latar;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.healthymedium.latar.navigation.NavigationManager;
import com.healthymedium.latar.screens.HomeScreen;
import com.healthymedium.latar.utilities.ViewUtil;

public class MainActivity extends AppCompatActivity {

    public MainActivity() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        NavigationManager.initialize(getSupportFragmentManager());
    }

    @Override
    protected void onStart() {
        super.onStart();
        Proctor.get().bind(this);
        NavigationManager.getInstance().clearBackStack();
        NavigationManager.getInstance().popBackStack();
        NavigationManager.getInstance().open(new HomeScreen());
    }

    @Override
    protected void onStop() {
        Proctor.get().unbind(this);
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        float px = 100;
        float in = 0.2f;
        Log.i("Conversion",px+" px = "+ ViewUtil.pxToIn(px)+" in");
        Log.i("Conversion",in+" in = "+ ViewUtil.inToPx(in)+" px");

    }

}

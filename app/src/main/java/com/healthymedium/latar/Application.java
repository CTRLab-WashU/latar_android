package com.healthymedium.latar;

public class Application extends android.app.Application {

    private static final String tag = "Application";
    static Application instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
//        DeviceClock.initialize();
    }

    public static Application getInstance(){
        return instance;
    }

}

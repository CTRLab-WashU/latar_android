package com.healthymedium.latar;

public class Application extends android.app.Application {

    private static final String tag = "Application";
    static Application instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static Application getInstance(){
        return instance;
    }

}

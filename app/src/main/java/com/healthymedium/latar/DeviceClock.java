package com.healthymedium.latar;

public class DeviceClock {

    private static long baseTime;

    private DeviceClock(){
        baseTime = microTime();
    }

    public static void zero() {
        baseTime = microTime();
    }

    public static long now() {
        return microTime() - baseTime;
    }

    public static long getTime(long time) {
        return time - baseTime;
    }

    private static long microTime() {
        return System.nanoTime() / 1000;
    }
}

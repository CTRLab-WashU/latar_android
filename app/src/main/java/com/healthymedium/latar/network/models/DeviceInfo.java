package com.healthymedium.latar.network.models;

import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import com.healthymedium.latar.Application;
import com.healthymedium.latar.BuildConfig;

public class DeviceInfo {

    private OperatingSystemInfo os = new OperatingSystemInfo();
    private HardwareInfo hardware = new HardwareInfo();
    private AppInfo app = new AppInfo();
    private String uuid;

    public static class OperatingSystemInfo {
        private String name = "Android";
        private Integer sdk = Build.VERSION.SDK_INT;
        private String release = Build.VERSION.RELEASE;
        private String securityPatch = Build.VERSION.SECURITY_PATCH;
    }

    public static class AppInfo {
        private String id = BuildConfig.APPLICATION_ID;
        private String versionName = BuildConfig.VERSION_NAME;
        private Integer versionCode = BuildConfig.VERSION_CODE;
    }

    public static class HardwareInfo {
        private String manufacturer = Build.MANUFACTURER;
        private String model = Build.MODEL;
        private String brand = Build.BRAND;
        private String id = Build.ID;
        private String name;
    }

    private DeviceInfo(){

    }

    public static DeviceInfo getInstance() {
        DeviceInfo info = new DeviceInfo();
        info.hardware.name = getDeviceName();
        info.uuid = getDeviceId();
        return info;
    }

    private static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String getDeviceId(){
        return Settings.Secure.getString(Application.getInstance().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase.append(c);
        }

        return phrase.toString();
    }
}

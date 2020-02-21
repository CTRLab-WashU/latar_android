package com.healthymedium.latar.network;

public class Commands {

    public static final char CLOCK_SYNC             = 0x42; // init clock synchronzation
    public static final char CLOCK_UPDATE           = 0x43; // data for clock synchronzation

    public static final char DEVICE_INFO            = 0x44; // device information for reference
    public static final char DEVICE_IDENTIFY        = 0x45; // make device self identify for user
    public static final char RESET                  = 0x46; // stop everything and reset

    public static final char CALIBRATION_SETUP      = 0x47;
    public static final char CALIBRATION_DISPLAY    = 0x48;
    public static final char CALIBRATION_TOUCH      = 0x49;
    public static final char CALIBRATION_TEARDOWN   = 0x4B;

    public static final char DISPLAY_START          = 0x4D;
    public static final char DISPLAY_DATA           = 0x4E;
    public static final char DISPLAY_STOP           = 0x4F;

    public static final char TAP_START              = 0x51;
    public static final char TAP_DATA               = 0x52;
    public static final char TAP_STOP               = 0x55;

}
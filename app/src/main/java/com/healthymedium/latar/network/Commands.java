package com.healthymedium.latar.network;

public class Commands {

    public static final char BROADCAST              = 0x20;

    public static final char CLOCK_SYNC             = 0x21; // init clock synchronzation
    public static final char CLOCK_UPDATE           = 0x22; // data for clock synchronzation

    public static final char DEVICE_INFO            = 0x24; // device information for reference
    public static final char DEVICE_IDENTIFY        = 0x25; // make device self identify for user
    public static final char APP_ERROR              = 0x26; // raise the white flag
    public static final char APP_RESET              = 0x27; // stop everything and reset

    public static final char CALIBRATION_SETUP      = 0x47;
    public static final char CALIBRATION_DISPLAY    = 0x48;
    public static final char CALIBRATION_TOUCH      = 0x49;
    public static final char CALIBRATION_TEARDOWN   = 0x4B;

    public static final char DISPLAY_START          = 0x28;
    public static final char DISPLAY_DATA           = 0x29;
    public static final char DISPLAY_STOP           = 0x2A;

    public static final char TAP_START              = 0x2B;
    public static final char TAP_DATA               = 0x2C;
    public static final char TAP_STOP               = 0x2D;

}
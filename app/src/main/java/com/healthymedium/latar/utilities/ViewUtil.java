package com.healthymedium.latar.utilities;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import android.widget.TextView;

import com.healthymedium.latar.Application;

public class ViewUtil {

    private static int navBarHeight = -1;
    private static int statusBarHeight = -1;

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static float dpToPx(float dp) {
        return (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int spToPx(int sp) {
        return (int) (sp * Resources.getSystem().getDisplayMetrics().scaledDensity);
    }

    public static float mmToPx(float mm) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, mm, Resources.getSystem().getDisplayMetrics());
    }

    public static int mmToPx(int mm) {
        return (int) mmToPx((float)mm);
    }

    public static float inToPx(float in) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_IN, in, Resources.getSystem().getDisplayMetrics());
    }

    public static int inToPx(int in) {
        return (int) inToPx((float)in);
    }

    public static float pxToIn(float px) {
        float value = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_IN,1, Resources.getSystem().getDisplayMetrics());
        return px/value;
    }

    public static int pxToIn(int px) {
        return (int) pxToIn((float)px);
    }

    public static int getColor(Context context, @ColorRes int id){
        return ContextCompat.getColor(context,id);
    }

    public static int getColor(@ColorRes int id){
        return ContextCompat.getColor(Application.getInstance(),id);
    }

    public static Drawable getDrawable(Context context, @DrawableRes int id){
        return ContextCompat.getDrawable(context,id);
    }

    public static Drawable getDrawable(@DrawableRes int id){
        return ContextCompat.getDrawable(Application.getInstance(),id);
    }

    public static String getString(Context context, @StringRes int id){
        return context.getString(id);
    }

    public static String getString(@StringRes int id){
        return Application.getInstance().getString(id);
    }

    public static String getStringConcat(@StringRes int ... ids){
        String string = new String();
        for(int i=0;i<ids.length;i++){
            string += Application.getInstance().getString(ids[i]);
        }
        return string;
    }

    public static String replaceToken(String input, @StringRes int format, String replacement){
        return input.replace(getString(format), replacement);
    }

    public static void underlineTextView(TextView textView){
        textView.setPaintFlags(textView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    public static int getStatusBarHeight() {
        if(statusBarHeight==-1){
            statusBarHeight = getStatusBarHeight(Application.getInstance());
        }
        return statusBarHeight;
    }

    private static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static int getNavBarHeight() {
        if(navBarHeight==-1){
            navBarHeight = getNavBarHeight(Application.getInstance());
        }
        return navBarHeight;
    }

    private static int getNavBarHeight(Context context) {
        int result = 0;

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();

        DisplayMetrics realMetrics = new DisplayMetrics();
        display.getRealMetrics(realMetrics);

        int realHeight = realMetrics.heightPixels;
        int realWidth = realMetrics.widthPixels;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);

        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;

        boolean hasSoftKeys = (realWidth>displayWidth) || (realHeight>displayHeight);

        // if the device has a navigation bar
        if(hasSoftKeys) {
            Resources resources = context.getResources();
            int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                return resources.getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }


}

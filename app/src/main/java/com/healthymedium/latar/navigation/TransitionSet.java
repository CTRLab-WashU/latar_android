package com.healthymedium.latar.navigation;

import android.support.annotation.AnimRes;
import com.healthymedium.latar.R;

public class TransitionSet {

    @AnimRes
    public int enter = 0;
    @AnimRes
    public int exit = 0;
    @AnimRes
    public int popEnter = 0;
    @AnimRes
    public int popExit = 0;

    public static TransitionSet getFadingDefault(){
        return getFadingDefault(true);
    }

    public static TransitionSet getFadingDefault(boolean animateEntry){
        TransitionSet set = new TransitionSet();
        if(animateEntry) {
            set.enter = R.anim.fade_in;
            set.popEnter =  R.anim.fade_in;
        }
        set.exit = R.anim.fade_out;
        set.popExit =  R.anim.fade_out;
        return set;
    }

    public static TransitionSet getSlidingDefault(){
        return getSlidingDefault(true);
    }

    public static TransitionSet getSlidingDefault(boolean animateEntry){
        TransitionSet set = new TransitionSet();
        if(animateEntry) {
            set.enter = R.anim.slide_in_right;
            set.popEnter =  R.anim.slide_in_left;
        }
        set.exit = R.anim.slide_out_left;
        set.popExit =  R.anim.slide_out_right;
        return set;
    }

    public static TransitionSet getSlidingInFadingOut(){
        TransitionSet set = new TransitionSet();
        set.enter = R.anim.slide_in_right;
        set.popEnter =  R.anim.slide_in_left;
        set.exit = R.anim.fade_out;
        set.popExit =  R.anim.fade_out;
        return set;
    }

    public static TransitionSet getFadingInSlidingOut(){
        TransitionSet set = new TransitionSet();
        set.enter = R.anim.fade_in;
        set.popEnter =  R.anim.fade_in;
        set.exit = R.anim.slide_out_left;
        set.popExit =  R.anim.slide_out_right;
        return set;
    }


}

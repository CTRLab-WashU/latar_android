package com.healthymedium.latar.navigation;

import android.support.annotation.IdRes;
import android.support.v4.app.FragmentManager;

import com.healthymedium.latar.BaseFragment;


public class NavigationController {

    protected static String tag = "NavigationController";

    protected FragmentManager fragmentManager;
    protected Listener listener;

    protected int currentFragmentId = -1;
    protected int containerViewId = -1;

    public NavigationController(FragmentManager fragmentManager, @IdRes int containerViewId) {
        this.fragmentManager = fragmentManager;
        this.containerViewId = containerViewId;
    }

    public FragmentManager getFragmentManager() {
        return fragmentManager;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public Listener getListener() {
        return listener;
    }

    public void removeListener() {
        listener = null;
    }

    public void open(BaseFragment fragment) {
        if (fragmentManager != null) {

            String tag = fragment.getSimpleTag();
            fragmentManager.beginTransaction()
                    .replace(containerViewId, fragment,tag)
                    .addToBackStack(tag)
                    .commitAllowingStateLoss();
            currentFragmentId = fragment.getId();
            if(listener!=null){
                listener.onOpen();
            }
        }
    }

    public void popBackStack() {
        if (fragmentManager != null) {
            fragmentManager.popBackStack();
            if(listener!=null){
                listener.onPopBack();
            }
        }
    }

    public int getBackStackEntryCount() {
        return fragmentManager.getBackStackEntryCount();
    }

    public void clearBackStack() {
        int count = fragmentManager.getBackStackEntryCount();
        for (int i = 0; i < count; i++) {
            int id = fragmentManager.getBackStackEntryAt(i).getId();
            fragmentManager.popBackStack(id, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    public BaseFragment getCurrentFragment(){
        return (BaseFragment) fragmentManager.findFragmentById(currentFragmentId);
    }

    public interface Listener {
        void onOpen();
        void onPopBack();
    }

}

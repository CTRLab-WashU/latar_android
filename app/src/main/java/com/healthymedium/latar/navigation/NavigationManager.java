package com.healthymedium.latar.navigation;

import android.support.v4.app.FragmentManager;
import com.healthymedium.latar.BaseFragment;
import com.healthymedium.latar.R;

public class NavigationManager {

    private static NavigationManager instance;
    private NavigationController defaultController;
    private NavigationController registeredController;

    private NavigationManager() {
        // Make empty constructor private
    }

    public static synchronized void initialize(final FragmentManager fragmentManager) {
        instance = new NavigationManager();
        instance.defaultController = new NavigationController(fragmentManager, R.id.content_frame);
    }

    public static synchronized NavigationManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(NavigationManager.class.getSimpleName() + " is not initialized, call initialize(..) method first.");
        }
        return instance;
    }

    public FragmentManager getFragmentManager() {
        return getController().getFragmentManager();
    }

    public NavigationController.Listener getListener() {
        return getController().getListener();
    }

    public void setListener(NavigationController.Listener listener) {
        getController().setListener(listener);
    }

    public void open(BaseFragment fragment) {
        getController().open(fragment);
    }

    public void popBackStack() {
        getController().popBackStack();
    }

    public int getBackStackEntryCount() {
        return getController().getBackStackEntryCount();
    }

    public void clearBackStack() {
        getController().clearBackStack();
    }

    public BaseFragment getCurrentFragment(){
        return getController().getCurrentFragment();
    }

    public void setController(NavigationController controller) {
        registeredController = controller;
    }

    public NavigationController getController() {
        if(registeredController!=null) {
            return registeredController;
        }
        return defaultController;
    }

    public void removeController() {
        registeredController = null;
    }

}

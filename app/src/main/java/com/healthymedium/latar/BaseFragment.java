package com.healthymedium.latar;

import android.support.v4.app.Fragment;

public class BaseFragment extends Fragment {

    String tag = getClass().getSimpleName();

    // convenience getters -------------------------------------------------------------------------

    public String getSimpleTag(){
        return tag;
    }

    public MainActivity getMainActivity(){
        return (MainActivity)getActivity();
    }

    public Application getApplication(){
        return (Application)getMainActivity().getApplication();
    }

}

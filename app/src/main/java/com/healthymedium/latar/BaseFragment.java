package com.healthymedium.latar;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.healthymedium.latar.navigation.TransitionSet;
import com.healthymedium.latar.network.Connection;

public class BaseFragment extends Fragment {

    TransitionSet transitions = new TransitionSet();
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

    public Connection getConnection(){
        return getMainActivity().getConnection();
    }

    // methods relating to transitions -------------------------------------------------------------

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, final int nextAnim) {
        if(nextAnim==0){
            return null;
        }

        Animation anim = AnimationUtils.loadAnimation(getMainActivity(), nextAnim);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if(nextAnim==transitions.enter){
                    onEnterTransitionStart(false);
                } else if(nextAnim==transitions.exit){
                    onExitTransitionStart(false);
                } else if(nextAnim==transitions.popEnter){
                    onEnterTransitionStart(true);
                } else if(nextAnim==transitions.popExit){
                    onExitTransitionStart(true);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(nextAnim==transitions.enter){
                    onEnterTransitionEnd(false);
                } else if(nextAnim==transitions.exit){
                    onExitTransitionEnd(false);
                } else if(nextAnim==transitions.popEnter){
                    onEnterTransitionEnd(true);
                } else if(nextAnim==transitions.popExit){
                    onExitTransitionEnd(true);
                }
            }
        });

        return anim;
    }


    protected void onEnterTransitionStart(boolean popped) {
        Log.v(tag,"onEnterTransitionStart");
    }

    protected void onEnterTransitionEnd(boolean popped) {
        Log.v(tag,"onEnterTransitionEnd");
    }

    protected void onExitTransitionStart(boolean popped) {
        Log.v(tag,"onExitTransitionStart");
    }

    protected void onExitTransitionEnd(boolean popped) {
        Log.v(tag,"onExitTransitionEnd");
    }

    public TransitionSet getTransitionSet() {
        return transitions;
    }

    public void setTransitionSet(TransitionSet transitions) {
        if(transitions!=null){
            this.transitions = transitions;
        }
    }

}

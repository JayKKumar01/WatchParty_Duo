package com.github.jaykkumar01.watchparty_duo.utils;


import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.view.OrientationEventListener;

public class AutoRotate {
    public static void set(Context context){
        Activity activity = (Activity) context;
        OrientationEventListener orientationEventListener = new OrientationEventListener(context) {
            @Override
            public void onOrientationChanged(int orientation) {
                int epsilon = 10;
                int leftL = 90;
                int rightL = 270;
                if (epsilonCheck(orientation,leftL,epsilon) || epsilonCheck(orientation, rightL, epsilon)){
                    if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
                        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
                        return;
                    }
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                }
            }
            private boolean epsilonCheck(int a, int b, int epsilon){
                return  a > b - epsilon && a < b + epsilon;
            }
        };
        orientationEventListener.enable();
    }
}


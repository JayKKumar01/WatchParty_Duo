package com.github.jaykkumar01.watchparty_duo.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class AspectRatio {

    public AspectRatio() {}

    public static void set(Activity activity){
        View view = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        setRatio(view);
    }

    private static void setRatio(View view){
        try {
            if (view instanceof ViewGroup){
                ViewGroup vg = (ViewGroup) view;
                for (int i=0; i< vg.getChildCount(); i++){
                    View child = vg.getChildAt(i);
                    setRatio(child);
                }
            }
            else if (view instanceof TextView){
                TextView text = (TextView) view;
//                Toast.makeText(context, text.getText(), Toast.LENGTH_SHORT).show();
                float size = text.getTextSize();
                //margin needed
                text.setTextSize(TypedValue.COMPLEX_UNIT_PX, value(size));
                text.setPadding(value(text.getPaddingLeft()),value(text.getPaddingTop()),value(text.getPaddingRight()),value(text.getPaddingBottom()));

            }
            else if (view instanceof ImageView){
                ImageView imageView = (ImageView) view;
                int width = imageView.getLayoutParams().width;
                int height = imageView.getLayoutParams().height;
                //ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) imageView.getLayoutParams();
                imageView.getLayoutParams().height = value(height);
                imageView.getLayoutParams().width = value(width);

                //padding
                imageView.setPadding(value(imageView.getPaddingLeft()), value(imageView.getPaddingTop())
                        , value(imageView.getPaddingRight()), value(imageView.getPaddingBottom()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    public static float value(float value) {
        int dpi = Resources.getSystem().getDisplayMetrics().densityDpi;
        int deviceWidth = Resources.getSystem().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
                ? Resources.getSystem().getDisplayMetrics().widthPixels : Resources.getSystem().getDisplayMetrics().heightPixels;

        return (float) (value * (double) deviceWidth / dpi * 0.3888888889);
    }
    static int value(int value) {
        int dpi = Resources.getSystem().getDisplayMetrics().densityDpi;
        int deviceWidth = Resources.getSystem().getDisplayMetrics().widthPixels;

        return (int) (value * (double) deviceWidth / dpi * 0.3888888889);
    }
}



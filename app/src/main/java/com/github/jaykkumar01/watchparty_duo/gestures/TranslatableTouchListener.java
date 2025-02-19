package com.github.jaykkumar01.watchparty_duo.gestures;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.github.jaykkumar01.watchparty_duo.R;

public class TranslatableTouchListener implements View.OnTouchListener {
    private float dX, dY;
    private long lastTapTime = 0;
    private static final int DOUBLE_TAP_THRESHOLD = 300;

    private final TextView textView;

    private float minTranslationX, minTranslationY;
    private float maxTranslationX, maxTranslationY;

    public TranslatableTouchListener(Activity activity,View view){
        textView = activity.findViewById(R.id.textView);
        ViewGroup parent = (ViewGroup) view.getParent();

        parent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                parent.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                minTranslationX = -view.getX();
                minTranslationY = -view.getY();

                maxTranslationX = parent.getWidth() - (view.getX() + view.getWidth());
                maxTranslationY = parent.getHeight() - (view.getY() + view.getHeight());
            }
        });

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        StringBuilder builder = new StringBuilder();

        builder.append("minTranslationX: ").append(minTranslationX).append(", minTranslationY: ").append(minTranslationY).append("\n");
        builder.append("maxTranslationX: ").append(maxTranslationX).append(", maxTranslationY: ").append(maxTranslationY).append("\n");

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastTapTime < DOUBLE_TAP_THRESHOLD) {
                    view.setTranslationX(0);
                    view.setTranslationY(0);
                    lastTapTime = 0;
                    return true;
                }
                lastTapTime = currentTime;

                dX = view.getTranslationX() - event.getRawX();
                dY = view.getTranslationY() - event.getRawY();
                return true;

            case MotionEvent.ACTION_MOVE:

                float translationX = event.getRawX() + dX;
                float translationY = event.getRawY() + dY;


                translationX = Math.max(minTranslationX,Math.min(translationX,maxTranslationX));
                translationY = Math.max(minTranslationY,Math.min(translationY,maxTranslationY));


                view.setTranslationX(translationX);
                view.setTranslationY(translationY);

                builder.append(translationX).append(" : ").append(translationY);
                textView.setText(builder.toString());
                return true;
            case MotionEvent.ACTION_UP:
                return true;


            default:
                return false;
        }
    }
}

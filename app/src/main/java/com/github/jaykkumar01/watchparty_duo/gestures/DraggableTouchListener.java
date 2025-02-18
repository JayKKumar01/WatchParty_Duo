package com.github.jaykkumar01.watchparty_duo.gestures;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.github.jaykkumar01.watchparty_duo.R;

public class DraggableTouchListener implements View.OnTouchListener {
    private final Activity activity;
    private final ViewParent scrollViewParent;

    public DraggableTouchListener(Activity activity){
        this.activity = activity;
        scrollViewParent = activity.findViewById(R.id.scrollView);
    }
    private float dX, dY;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent == null) return false;

        // Request the ScrollView not to intercept touch events
        if (scrollViewParent != null) {
            scrollViewParent.requestDisallowInterceptTouchEvent(true);
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                dX = view.getX() - event.getRawX();
                dY = view.getY() - event.getRawY();
                return true;

            case MotionEvent.ACTION_MOVE:
                float newX = event.getRawX() + dX;
                float newY = event.getRawY() + dY;

                // Prevent moving outside parent bounds
                float maxX = parent.getWidth() - view.getWidth();
                float maxY = parent.getHeight() - view.getHeight();

                newX = Math.max(0, Math.min(newX, maxX));
                newY = Math.max(0, Math.min(newY, maxY));

                view.setX(newX);
                view.setY(newY);
                return true;

            case MotionEvent.ACTION_UP:
                if (scrollViewParent != null) {
                    scrollViewParent.requestDisallowInterceptTouchEvent(false);
                }
                return true;

            default:
                return false;
        }
    }
}

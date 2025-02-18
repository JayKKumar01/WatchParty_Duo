package com.github.jaykkumar01.watchparty_duo.gestures;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class DraggableTouchListener implements View.OnTouchListener {
    private float dX, dY;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent == null) return false;

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
                return true;

            default:
                return false;
        }
    }
}

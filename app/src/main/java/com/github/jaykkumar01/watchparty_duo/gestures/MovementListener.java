package com.github.jaykkumar01.watchparty_duo.gestures;

import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

public class MovementListener implements View.OnTouchListener {
    private float dX, dY;
    private long lastTapTime = 0;
    private static final int DOUBLE_TAP_THRESHOLD = 300;
    private final View view;
    private float minTranslationX, minTranslationY;
    private float maxTranslationX, maxTranslationY;


    public MovementListener(View view) {
        this.view = view;
        resetBoundaries();
    }

    public void resetBoundaries() {
        ViewGroup parent = (ViewGroup) view.getParent();

        ViewTreeObserver.OnGlobalLayoutListener layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (parent.getWidth() == 0 || parent.getHeight() == 0) return;

                view.setTranslationX(0);
                view.setTranslationY(0);

                minTranslationX = -view.getX();
                minTranslationY = -view.getY();
                maxTranslationX = parent.getWidth() - (view.getX() + view.getWidth());
                maxTranslationY = parent.getHeight() - (view.getY() + view.getHeight());

                parent.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        };

        parent.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent event) {

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                long curTime = System.currentTimeMillis();
                if (System.currentTimeMillis() - lastTapTime < DOUBLE_TAP_THRESHOLD) {
                    view.setTranslationX(0);
                    view.setTranslationY(0);
                    lastTapTime = 0;
                    return true;
                }
                lastTapTime = curTime;

                dX = view.getTranslationX() - event.getRawX();
                dY = view.getTranslationY() - event.getRawY();
                return true;

            case MotionEvent.ACTION_MOVE:
                float translationX = event.getRawX() + dX;
                float translationY = event.getRawY() + dY;

                translationX = Math.max(minTranslationX, Math.min(translationX, maxTranslationX));
                translationY = Math.max(minTranslationY, Math.min(translationY, maxTranslationY));

                view.setTranslationX(translationX);
                view.setTranslationY(translationY);

                return true;

            case MotionEvent.ACTION_UP:
                return true;

            default:
                return false;
        }
    }
}

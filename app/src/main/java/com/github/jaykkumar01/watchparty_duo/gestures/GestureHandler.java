package com.github.jaykkumar01.watchparty_duo.gestures;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;

public class GestureHandler implements View.OnTouchListener {
    private final GestureDetector gestureDetector;
    private final ScaleGestureDetector scaleGestureDetector;

    public interface OnGestureCallback {
        void onSingleTap();
        void onZoom(boolean isZoomingIn);
    }

    public GestureHandler(Context context, OnGestureCallback callback) {
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                if (callback != null) {
                    callback.onSingleTap(); // Handle single tap (show/hide controls)
                }
                return true;
            }

            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                return true;
            }
        });

        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
                if (callback != null) {
                    boolean isZoomingIn = detector.getScaleFactor() > 1.0f; // Zoom in if scale > 1
                    callback.onZoom(isZoomingIn);
                }
                return true;
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }
}


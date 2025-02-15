package com.github.jaykkumar01.watchparty_duo.gestures;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;

public class ZoomPinchListener implements View.OnTouchListener {
    private final ScaleGestureDetector scaleGestureDetector;

    public interface OnZoomCallback {
        void onZoom(boolean isZoomingIn);
    }

    public ZoomPinchListener(Context context, OnZoomCallback callback) {
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(@NonNull ScaleGestureDetector detector) {
                if (callback != null) {
                    boolean isZoomingIn = detector.getScaleFactor() > 1.0f; // Zoom in if scale factor > 1
                    callback.onZoom(isZoomingIn);
                }
                return true;
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return scaleGestureDetector.onTouchEvent(event);
    }
}


package com.github.jaykkumar01.watchparty_duo.gestures;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class DoubleTapListener implements View.OnTouchListener {
    private final GestureDetector gestureDetector;

    public interface OnDoubleTapCallback {
        void onDoubleTap(boolean isDoubleTap);
    }

    public DoubleTapListener(View view, Context context, OnDoubleTapCallback callback) {
        this.gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (callback != null) {
                    callback.onDoubleTap(true); // Trigger callback
                }
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (callback != null) {
                    callback.onDoubleTap(false);
                }
                return super.onSingleTapConfirmed(e);
            }
        });

        // Set the touch listener directly to avoid manual setup in the activity/class using it
        view.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }
}

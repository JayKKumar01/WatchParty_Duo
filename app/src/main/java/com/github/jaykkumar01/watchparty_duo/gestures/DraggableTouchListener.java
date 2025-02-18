package com.github.jaykkumar01.watchparty_duo.gestures;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.github.jaykkumar01.watchparty_duo.R;

public class DraggableTouchListener implements View.OnTouchListener {
    private final ViewParent scrollViewParent;

    private float dX, dY;
    private float scaleFactor = 1f;
    private final float minScale = 0.2f;
    private final float maxScale = 3f;

    private float initialDistance;
    private float initialScaleFactor = 1f;
    private float originalFocalX, originalFocalY;
    private float originalX, originalY;

    private boolean isScaling = false;
    private boolean isDragging = false;

    private boolean hasOriginalState = false;
    private float resetX, resetY, resetScale = 1f;
    private long lastClickTime = 0;
    private static final int DOUBLE_CLICK_THRESHOLD = 300;

    public DraggableTouchListener(Activity activity) {
        scrollViewParent = activity.findViewById(R.id.scrollView);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent == null) return false;

        if (scrollViewParent != null) {
            scrollViewParent.requestDisallowInterceptTouchEvent(true);
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime < DOUBLE_CLICK_THRESHOLD) {
                    resetToOriginalState(view);
                    lastClickTime = 0;
                    return true;
                }
                lastClickTime = currentTime;

                if (!hasOriginalState) {
                    storeOriginalState(view);
                }

                dX = view.getX() - event.getRawX();
                dY = view.getY() - event.getRawY();
                isDragging = true;
                isScaling = false;
                return true;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    if (!hasOriginalState) {
                        storeOriginalState(view);
                    }

                    float x0 = event.getX(0);
                    float y0 = event.getY(0);
                    float x1 = event.getX(1);
                    float y1 = event.getY(1);

                    originalFocalX = (x0 + x1) / 2 + view.getX();
                    originalFocalY = (y0 + y1) / 2 + view.getY();

                    originalX = view.getX();
                    originalY = view.getY();
                    initialScaleFactor = scaleFactor;
                    initialDistance = (float) Math.sqrt(Math.pow(x1 - x0, 2) + Math.pow(y1 - y0, 2));

                    isScaling = true;
                    isDragging = false;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isScaling && event.getPointerCount() >= 2) {
                    float x0 = event.getX(0);
                    float y0 = event.getY(0);
                    float x1 = event.getX(1);
                    float y1 = event.getY(1);

                    float currentDistance = (float) Math.sqrt(Math.pow(x1 - x0, 2) + Math.pow(y1 - y0, 2));

                    if (initialDistance > 0) {
                        scaleFactor = initialScaleFactor * (currentDistance / initialDistance);
                        scaleFactor = Math.max(minScale, Math.min(scaleFactor, maxScale));
                    }

                    float newX = originalFocalX - (originalFocalX - originalX) * (scaleFactor / initialScaleFactor);
                    float newY = originalFocalY - (originalFocalY - originalY) * (scaleFactor / initialScaleFactor);

                    adjustPositionWithScale(view, parent, newX, newY, scaleFactor);
                    view.setScaleX(scaleFactor);
                    view.setScaleY(scaleFactor);

                    initialDistance = currentDistance;
                    initialScaleFactor = scaleFactor;
                } else if (isDragging && event.getPointerCount() == 1) {
                    float newX = event.getRawX() + dX;
                    float newY = event.getRawY() + dY;
                    adjustPositionWithScale(view, parent, newX, newY, scaleFactor);
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() == 1) {
                    initialDistance = 0;
                    isScaling = false;
                    isDragging = false;
                }
                if (scrollViewParent != null) {
                    scrollViewParent.requestDisallowInterceptTouchEvent(false);
                }
                return true;

            default:
                return false;
        }
        return false;
    }

    private void storeOriginalState(View view) {
        resetX = view.getX();
        resetY = view.getY();
        resetScale = scaleFactor;
        hasOriginalState = true;
    }

    private void resetToOriginalState(View view) {
        if (!hasOriginalState) return;

        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent == null) return;

        scaleFactor = resetScale;
        view.setScaleX(resetScale);
        view.setScaleY(resetScale);
        adjustPositionWithScale(view, parent, resetX, resetY, resetScale);
    }

    private void adjustPositionWithScale(View view, ViewGroup parent, float newX, float newY, float scale) {
        float viewWidth = view.getWidth();
        float viewHeight = view.getHeight();
        float parentWidth = parent.getWidth();
        float parentHeight = parent.getHeight();

        if (viewWidth <= 0 || viewHeight <= 0) return;

        float minX = (viewWidth / 2) * (scale - 1);
        float maxX = parentWidth - (viewWidth / 2) * (scale + 1);
        newX = Math.max(minX, Math.min(newX, maxX));

        float minY = (viewHeight / 2) * (scale - 1);
        float maxY = parentHeight - (viewHeight / 2) * (scale + 1);
        newY = Math.max(minY, Math.min(newY, maxY));

        view.setX(newX);
        view.setY(newY);
    }
}

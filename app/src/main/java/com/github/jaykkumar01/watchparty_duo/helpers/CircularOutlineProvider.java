package com.github.jaykkumar01.watchparty_duo.helpers;


import android.graphics.Outline;
import android.view.View;
import android.view.ViewOutlineProvider;

// Java
public class CircularOutlineProvider extends ViewOutlineProvider {
    @Override
    public void getOutline(View view, Outline outline) {
        int size = Math.min(view.getWidth(), view.getHeight());
        outline.setOval(0, 0, size, size);
    }
}


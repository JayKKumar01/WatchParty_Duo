package com.github.jaykkumar01.watchparty_duo.helpers;

import android.content.Context;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;
import android.widget.Toast;

public class DisplayHelper {
    public static int getDisplayRotation(Context context) {
        if (context == null) return Surface.ROTATION_0; // Default to 0 if context is null

        // Get the WindowManager from the provided context
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        if (windowManager != null) {
            Display display = windowManager.getDefaultDisplay();
            return display.getRotation();
        }
        Toast.makeText(context, "Window Manager null", Toast.LENGTH_SHORT).show();
        return Surface.ROTATION_0; // Default rotation if WindowManager is unavailable
    }
}


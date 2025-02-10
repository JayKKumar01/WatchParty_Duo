package com.github.jaykkumar01.watchparty_duo.utils;

import android.annotation.SuppressLint;
import android.util.Size;
import android.view.TextureView;

import androidx.constraintlayout.widget.ConstraintLayout;

public class CameraUtil {
    @SuppressLint("DefaultLocale")
    public static Size chooseOptimalSize(Size[] sizes, int targetHeight) {
        Size optimalSize = sizes[0];
        int minDiff = Integer.MAX_VALUE;

        // Loop through all sizes to find the size with the closest height
        for (Size size : sizes) {
            if (size.getHeight() > size.getWidth()){
                continue;
            }
            int height = size.getHeight();
            int diff = Math.abs(targetHeight - height);

            if (diff < minDiff) {
                minDiff = diff;
                optimalSize = size;
            }
        }

        float ratio = (float) optimalSize.getWidth() / optimalSize.getHeight();

        for (Size size : sizes) {
            if (size.getHeight() != optimalSize.getHeight() || size.getHeight() > size.getWidth()){
                continue;
            }
            float sizeAspectRatio = (float) size.getWidth() / size.getHeight();

            // Choose the size with the closest aspect ratio to the target
            if (sizeAspectRatio < ratio) {
                ratio = sizeAspectRatio;
                optimalSize = size;
            }
        }
        return optimalSize;
    }
}

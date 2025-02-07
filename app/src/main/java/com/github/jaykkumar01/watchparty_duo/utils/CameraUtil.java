package com.github.jaykkumar01.watchparty_duo.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SearchRecentSuggestionsProvider;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.util.Size;
import android.view.TextureView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.github.jaykkumar01.watchparty_duo.listeners.UpdateListener;

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





    private static void setTextureViewAspectRatio(TextureView textureView, float ratio) {
        // Ensure the TextureView has a parent and it's a ConstraintLayout
        if (textureView.getParent() instanceof ConstraintLayout) {
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) textureView.getLayoutParams();
            // Set the dimension ratio in the format "W,ratio:1"
            params.dimensionRatio = String.valueOf(ratio);
            textureView.setLayoutParams(params);
        } else {
            throw new IllegalArgumentException("TextureView must be inside a ConstraintLayout.");
        }
    }
}

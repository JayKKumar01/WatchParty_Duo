package com.github.jaykkumar01.watchparty_duo.renderers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.view.TextureView;

import com.github.jaykkumar01.watchparty_duo.constants.Packets;
import com.github.jaykkumar01.watchparty_duo.listeners.FeedListener;

public class TextureRenderer {
    private Bitmap reusableBitmap = null;
    private volatile boolean isDrawing = false;
    private final boolean isCamera;

    public TextureRenderer(boolean isCamera) {
        this.isCamera = isCamera;
    }

    public void updateTexture(TextureView textureView, byte[] imageData) {
        if (textureView == null || imageData == null || !textureView.isAvailable()) {
            return;
        }

        synchronized (this) {
            if (isDrawing) {
                return;
            }
            isDrawing = true;
        }

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;

            if (reusableBitmap != null) {
                options.inBitmap = reusableBitmap;
            }
            reusableBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);

            if (reusableBitmap == null) {
                return;
            }

            int bitmapWidth = reusableBitmap.getWidth();
            int bitmapHeight = reusableBitmap.getHeight();
            int viewWidth = textureView.getWidth();
            int viewHeight = textureView.getHeight();

            Matrix matrix = new Matrix();
            RectF srcRect = new RectF(0, 0, bitmapWidth, bitmapHeight);
            RectF dstRect = new RectF(0, 0, viewWidth, viewHeight);
            matrix.setRectToRect(srcRect, dstRect, Matrix.ScaleToFit.CENTER);

            Canvas canvas = textureView.lockCanvas();
            if (canvas != null) {
                canvas.drawBitmap(reusableBitmap, matrix, null);
                textureView.unlockCanvasAndPost(canvas);
                synchronized (this) {
                    if (!isCamera) {
                        Packets.imagePacketExecuted++;
                    }
                }
            }
        } finally {
            synchronized (this) {
                isDrawing = false;
            }
        }
    }
}

package com.github.jaykkumar01.watchparty_duo.renderers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.view.TextureView;
import com.github.jaykkumar01.watchparty_duo.listeners.FeedListener;

public class TextureRenderer {
    private final FeedListener feedListener;
    private Bitmap reusableBitmap = null;
    private volatile boolean isDrawing = false;
    private final boolean isCamera;

    private int framesDrawn = 0;
    private int framesSkipped = 0;
    private int framesReturned = 0; // Count frames that returned early
    private final Handler logHandler = new Handler(Looper.getMainLooper());

    public TextureRenderer(FeedListener feedListener, boolean isCamera) {
        this.isCamera = isCamera;
        this.feedListener = feedListener;
        startLogging();
    }

    public void updateTexture(TextureView textureView, byte[] imageData) {
        if (textureView == null || imageData == null || !textureView.isAvailable()) {
            synchronized (this) {
                framesReturned++; // Track returned frames
            }
            return;
        }

        synchronized (this) {
            if (isDrawing) {
                framesSkipped++; // Increment skipped frames if already drawing
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
                synchronized (this) {
                    framesReturned++; // If decoding failed, count as returned
                }
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
                    framesDrawn++; // Increment drawn frames counter
                }
            }
        } finally {
            synchronized (this) {
                isDrawing = false;
            }
        }
    }

    private void startLogging() {
        logHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateLogs((isCamera ? "Camera: " : "Remote: ") +
                        "Drawn: " + framesDrawn +
                        " | Skipped: " + framesSkipped +
                        " | Returned: " + framesReturned);
                framesDrawn = 0;
                framesSkipped = 0;
                framesReturned = 0;
                logHandler.postDelayed(this, 1000); // Log every second
            }
        }, 1000);
    }

    private void updateLogs(String logMessage) {
        if (feedListener != null) {
            feedListener.onUpdate(logMessage);
        }
    }
}

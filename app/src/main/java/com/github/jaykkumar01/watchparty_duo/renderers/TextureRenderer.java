package com.github.jaykkumar01.watchparty_duo.renderers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.view.TextureView;

public class TextureRenderer {
    private static Bitmap reusableBitmap = null;
    private static final Object lock = new Object();
    private static boolean isDrawing = false;

    private static boolean isCrop;

    public static void updateTexture(TextureView textureView, byte[] imageData) {
        if (textureView == null || imageData == null || !textureView.isAvailable()) return;

        synchronized (lock) {
            // If a drawing operation is already in progress, skip this update
            if (isDrawing) return;
            isDrawing = true;

            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inMutable = true;

                if (reusableBitmap != null) {
                    options.inBitmap = reusableBitmap;
                }
                reusableBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);

                if (reusableBitmap == null) return;

                int bitmapWidth = reusableBitmap.getWidth();
                int bitmapHeight = reusableBitmap.getHeight();
                int viewWidth = textureView.getWidth();
                int viewHeight = textureView.getHeight();

                int left = 0;
                int top = 0;

                if (bitmapWidth > bitmapHeight){
                    left = (bitmapWidth - bitmapHeight) / 2;
                    bitmapWidth = bitmapHeight;
                }else{
                    top = (bitmapHeight - bitmapWidth) / 2;
                    bitmapHeight = bitmapWidth;
                }



                Matrix matrix = new Matrix();
                RectF srcRect = new RectF(left, top, bitmapWidth, bitmapHeight);
                RectF dstRect = new RectF(0, 0, viewWidth, viewHeight);

                matrix.setRectToRect(srcRect, dstRect, Matrix.ScaleToFit.CENTER);

                Canvas canvas = textureView.lockCanvas();
                if (canvas != null) {
                    canvas.drawBitmap(reusableBitmap, matrix, null);
                    textureView.unlockCanvasAndPost(canvas);
                }
            } finally {
                isDrawing = false;
            }
        }
    }
}

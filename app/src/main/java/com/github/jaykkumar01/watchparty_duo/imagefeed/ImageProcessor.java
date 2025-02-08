package com.github.jaykkumar01.watchparty_duo.imagefeed;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.media.ImageReader;
import android.view.Surface;
import android.view.TextureView;

import com.github.jaykkumar01.watchparty_duo.converters.YUVConverter;
import com.github.jaykkumar01.watchparty_duo.listeners.ImageFeedListener;
import com.github.jaykkumar01.watchparty_duo.renderers.TextureRenderer;
import com.github.jaykkumar01.watchparty_duo.utils.BitmapUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageProcessor {
    private final Context context;
    private final CameraModel cameraModel;
    private final ImageFeedListener imageFeedListener;
    private final static ExecutorService executorService = Executors.newSingleThreadExecutor();
    private int displayRotation = -1;
    private final Matrix rotationMatrix = new Matrix();

    public ImageProcessor(Context context,CameraModel cameraModel, ImageFeedListener imageFeedListener) {
        this.context = context;
        this.cameraModel = cameraModel;
        this.imageFeedListener = imageFeedListener;
    }

    public void processImage(ImageReader reader, TextureView textureView) {
        executorService.execute(() -> {
            try (Image image = reader.acquireLatestImage()) {
                if (image == null) return;


                byte[] bytes = YUVConverter.toJpegImage(image,80);

                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Bitmap finalBitmap = fixFrontCameraOrientation(bitmap);
                if (finalBitmap != bitmap) {
                    bitmap.recycle(); // Recycle unused bitmap
                }
                byte[] finalBytes = BitmapUtils.getBytes(finalBitmap);
                finalBitmap.recycle(); // Recycle final bitmap after use

                if (imageFeedListener != null) {
                    imageFeedListener.onImageFeed(finalBytes, System.currentTimeMillis());
                }
                if (textureView != null) {
                    TextureRenderer.updateTexture(textureView, finalBytes);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private Bitmap fixFrontCameraOrientation(Bitmap bitmap) {
        if (bitmap == null) return null;

        int displayRotation = ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();

        if (this.displayRotation != displayRotation) {
            this.displayRotation = displayRotation;

            updateListener("Display Rotation: " + displayRotation);

            rotationMatrix.reset();
            int rotationDegrees = 0;

            switch (displayRotation) {
                case Surface.ROTATION_0: break;
                case Surface.ROTATION_90: rotationDegrees = 270; break;
                case Surface.ROTATION_180: rotationDegrees = 180; break;
                case Surface.ROTATION_270: rotationDegrees = 90; break;
            }

            // Calculate the correct rotation
            int finalRotation = (cameraModel.getSensorOrientation() - rotationDegrees + 360) % 360;
            rotationMatrix.postRotate(finalRotation);
        }

        // Create a new matrix for the current bitmap (to avoid cumulative flips)
        Matrix tempMatrix = new Matrix(rotationMatrix);
        tempMatrix.postScale(-1, 1, bitmap.getWidth() / 2f, bitmap.getHeight() / 2f);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), tempMatrix, true);
    }

    private void updateListener(String logMessage) {
        if (imageFeedListener != null){
            imageFeedListener.onUpdate(logMessage);
        }
    }
}

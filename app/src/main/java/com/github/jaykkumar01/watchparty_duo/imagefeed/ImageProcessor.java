package com.github.jaykkumar01.watchparty_duo.imagefeed;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
import android.media.ImageReader;
import android.net.wifi.hotspot2.pps.Credential;
import android.view.Surface;
import android.view.TextureView;

import com.github.jaykkumar01.watchparty_duo.constants.Feed;
import com.github.jaykkumar01.watchparty_duo.constants.FeedType;
import com.github.jaykkumar01.watchparty_duo.converters.YUVConverter;
import com.github.jaykkumar01.watchparty_duo.listeners.FeedListener;
import com.github.jaykkumar01.watchparty_duo.models.FeedModel;
import com.github.jaykkumar01.watchparty_duo.renderers.TextureRenderer;
import com.github.jaykkumar01.watchparty_duo.utils.BitmapUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ImageProcessor {
    private final Context context;
    private final CameraModel cameraModel;
    private final FeedListener feedListener;
    private final long frameIntervalMs;
    private final TextureView textureView;

    private int displayRotation = -1;
    private final Matrix rotationMatrix = new Matrix();
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Queue<FeedModel> imageQueue = new ConcurrentLinkedQueue<FeedModel>();

    public ImageProcessor(Context context, CameraModel cameraModel, FeedListener feedListener, TextureView textureView) {
        this.context = context;
        this.cameraModel = cameraModel;
        this.feedListener = feedListener;
        this.textureView = textureView;
        this.frameIntervalMs = (long) (1000.0 / Feed.FPS);
    }
    public void startScheduler(){
        if (scheduler.isShutdown()){
            scheduler = Executors.newSingleThreadScheduledExecutor();
        }

        updateListener("FPS: "+Feed.FPS +", Frame Interval : "+this.frameIntervalMs);
        scheduler.scheduleWithFixedDelay(() -> {
            synchronized (imageQueue) {
                processImage();
            }
        }, 0, frameIntervalMs, TimeUnit.MILLISECONDS);
    }

    public void onProcessImage(ImageReader reader) {
        Executors.newCachedThreadPool().execute(() -> {
            try (Image image = reader.acquireLatestImage()) {
                if (image == null) return;
                long timestamp = System.currentTimeMillis();

                byte[] bytes = YUVConverter.toJpegImage(image, 80);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Bitmap finalBitmap = fixFrontCameraOrientation(bitmap);
                if (finalBitmap != bitmap) {
                    bitmap.recycle();
                }
                byte[] finalBytes = BitmapUtils.getBytes(finalBitmap);
                synchronized (imageQueue) {
                    imageQueue.add(new FeedModel(finalBytes, timestamp));
                }
                finalBitmap.recycle();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void stopScheduler() {
        if (!scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    private void processImage() {
        if (imageQueue.isEmpty()) {
            return;
        }

        List<FeedModel> batch = new ArrayList<>(imageQueue);
        imageQueue.clear();

        FeedModel tempModel = batch.get(batch.size()/2);
        byte[] bytes = tempModel.getBytes();
        long timeStamp = tempModel.getTimestamp();
        batch.clear();

        if (feedListener != null) {
            feedListener.onFeed(bytes, timeStamp, FeedType.IMAGE_FEED);
        }
        if (textureView != null) {
            TextureRenderer.updateTexture(textureView, bytes);
        }
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
        if (feedListener != null) {
            feedListener.onUpdate(logMessage);
        }
    }
}

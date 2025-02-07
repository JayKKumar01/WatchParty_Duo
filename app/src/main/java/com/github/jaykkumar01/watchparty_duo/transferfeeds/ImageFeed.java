package com.github.jaykkumar01.watchparty_duo.transferfeeds;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.github.jaykkumar01.watchparty_duo.converters.YUVConverter;
import com.github.jaykkumar01.watchparty_duo.helpers.RangeCalculator;
import com.github.jaykkumar01.watchparty_duo.listeners.ImageFeedListener;
import com.github.jaykkumar01.watchparty_duo.listeners.UpdateListener;
import com.github.jaykkumar01.watchparty_duo.models.CameraModel;
import com.github.jaykkumar01.watchparty_duo.updates.AppData;
import com.github.jaykkumar01.watchparty_duo.utils.BitmapUtils;
import com.github.jaykkumar01.watchparty_duo.utils.CameraUtil;
import com.github.jaykkumar01.watchparty_duo.renderers.TextureRenderer;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@SuppressLint("DefaultLocale")
public class ImageFeed implements ImageReader.OnImageAvailableListener{
    private final Context context;
    private final TextureView textureView;
    private CameraCaptureSession cameraCaptureSession;
    private ImageReader imageReader;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper()); // Handler for the main thread
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final Matrix rotationMatrix = new Matrix(); // Reuse Matrix for orientation fixes
    private ImageFeedListener imageFeedListener;
    private UpdateListener updateListener;
    private int displayRotation = -1;
    private CameraModel cameraModel;


    public ImageFeed(Context context, TextureView textureView){
        this.context = context;
        this.textureView = textureView;
    }

    public void setUpdateListener(UpdateListener updateListener) {
        this.updateListener = updateListener;
    }

    public void setImageFeedListener(ImageFeedListener imageFeedListener){
        this.imageFeedListener = imageFeedListener;
    }
    public void openCamera() {
        try {
            cameraModel = new CameraModel(context);
            if (cameraModel.getCameraId() == null) {
                return;
            }

            updateListener("Ranges: "+Arrays.toString(cameraModel.getFpsRanges()));

            if (cameraModel.getOptimalFpsRange() == null){
                return;
            }
            updateListener("Optimal Range: "+ cameraModel.getOptimalFpsRange() + ", FPS: "+AppData.FPS);

            if (cameraModel.getConfigMap() == null) {
                return;
            }

            cameraModel.setImageFormat(ImageFormat.YUV_420_888);

            Size[] outputSizes = cameraModel.getOutputSizes();
            if (outputSizes == null || outputSizes.length == 0) return;

            updateListener("Output Sizes: "+Arrays.toString(outputSizes));

            Size previewSize = CameraUtil.chooseOptimalSize(outputSizes, AppData.IMAGE_HEIGHT);

            String format = String.format("Optimal Size: %s, Ratio: %.2f", previewSize.toString(), (float) previewSize.getWidth() / previewSize.getHeight());
            updateListener(format);

            imageReader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(), cameraModel.getImageFormat(), 2);
            imageReader.setOnImageAvailableListener(this, mainHandler);
            cameraModel.openCamera(stateCallback,mainHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updateListener(String logMessage) {
        if (updateListener != null){
            updateListener.onUpdate(logMessage);
        }
    }





    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraModel.createCaptureSession(camera,imageReader,mainHandler);
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e("ImageFeed", "Camera error: " + error);
            camera.close();
        }
    };




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
            int finalRotation = (cameraModel.getCameraRotation() - rotationDegrees + 360) % 360;
            rotationMatrix.postRotate(finalRotation);
        }

        // Create a new matrix for the current bitmap (to avoid cumulative flips)
        Matrix tempMatrix = new Matrix(rotationMatrix);
        tempMatrix.postScale(-1, 1, bitmap.getWidth() / 2f, bitmap.getHeight() / 2f);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), tempMatrix, true);
    }


    public void closeCamera() {
        if (imageReader != null) imageReader.close();
        if (cameraModel.getCameraCaptureSession() != null){
            cameraModel.getCameraCaptureSession().close();
        }
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        ////
        // update()
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
}

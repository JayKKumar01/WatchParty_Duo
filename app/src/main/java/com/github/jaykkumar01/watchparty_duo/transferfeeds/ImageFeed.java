package com.github.jaykkumar01.watchparty_duo.transferfeeds;

import android.Manifest;
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
import com.github.jaykkumar01.watchparty_duo.listeners.ImageFeedListener;
import com.github.jaykkumar01.watchparty_duo.listeners.UpdateListener;
import com.github.jaykkumar01.watchparty_duo.updates.AppData;
import com.github.jaykkumar01.watchparty_duo.utils.BitmapUtils;
import com.github.jaykkumar01.watchparty_duo.utils.CameraUtil;
import com.github.jaykkumar01.watchparty_duo.renderers.TextureRenderer;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ImageFeed implements ImageReader.OnImageAvailableListener{
    private final Context context;
    private final TextureView textureView;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private ImageReader imageReader;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper()); // Handler for the main thread
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final Matrix rotationMatrix = new Matrix(); // Reuse Matrix for orientation fixes

    private Range<Integer> fpsRange = null;

    private ImageFeedListener imageFeedListener;
    private UpdateListener updateListener;


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
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = getFrontCameraId(manager);
            if (cameraId == null) {
                return;
            }
            fpsRange = getMaxFpsRange(manager,cameraId);

            if (fpsRange == null){
                return;
            }

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap configMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (configMap == null) {
                return;
            }

            Size[] outputSizes = configMap.getOutputSizes(ImageFormat.YUV_420_888);
            if (outputSizes == null || outputSizes.length == 0) return;

            updateListener.onUpdate(Arrays.toString(outputSizes));

            Size previewSize = CameraUtil.chooseOptimalSize(outputSizes, AppData.IMAGE_HEIGHT,updateListener);
            imageReader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(), ImageFormat.YUV_420_888, 2);
            imageReader.setOnImageAvailableListener(this, mainHandler);

            manager.openCamera(cameraId, stateCallback, mainHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private String getFrontCameraId(CameraManager manager) throws CameraAccessException {
        for (String id : manager.getCameraIdList()) {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
            Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
            if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                return id;
            }
        }
        return null;
    }


    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCaptureSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e("ImageFeed", "Camera error: " + error);
            camera.close();
            cameraDevice = null;
        }
    };

    private Range<Integer> getMaxFpsRange(CameraManager cameraManager, String cameraId) {

        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            Range<Integer>[] fpsRanges = characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);

            if (fpsRanges != null && fpsRanges.length > 0) {
                if (updateListener != null){
                    updateListener.onUpdate(Arrays.toString(fpsRanges));
                }

                Range<Integer> maxFpsRange = fpsRanges[0]; // Initialize with first range
                Range<Integer> minFpsRange = fpsRanges[0]; // Initialize with first range

                // Find the maximum FPS range
                for (Range<Integer> range : fpsRanges) {

                    if (range.getUpper() > maxFpsRange.getUpper()) {
                        maxFpsRange = range;
                    }
                    if (range.getLower() < minFpsRange.getLower()) {
                        minFpsRange = range;
                    }
                }
                for (Range<Integer> range : fpsRanges) {

                    if (range.getUpper().equals(maxFpsRange.getUpper()) && range.getLower() > maxFpsRange.getLower()){
                        maxFpsRange = range;
                    }
                    if (range.getLower().equals(minFpsRange.getLower()) && range.getUpper() < minFpsRange.getUpper()) {
                        minFpsRange = range;
                    }
                }
                if (updateListener != null){
                    updateListener.onUpdate("maxFpsRange: ["+maxFpsRange.getLower()+","+maxFpsRange.getUpper()+"]");
                    updateListener.onUpdate("minFpsRange: ["+minFpsRange.getLower()+","+minFpsRange.getUpper()+"]");
                }
                return new Range<>(minFpsRange.getLower(),maxFpsRange.getUpper());
            } else {
                Log.e("ImageFeed", "No FPS range available");
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void createCaptureSession() {
        try {
            cameraDevice.createCaptureSession(Collections.singletonList(imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null) return;

                    cameraCaptureSession = session;

                    try {
                        CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                        captureBuilder.addTarget(imageReader.getSurface());
                        captureBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);

                        int lower = Math.max(fpsRange.getLower(),Math.min(fpsRange.getUpper(),AppData.FPS));
                        int upper = Math.min(fpsRange.getUpper(),Math.max(fpsRange.getLower(),AppData.FPS));

                        if (updateListener != null){
                            updateListener.onUpdate("Range: ["+lower+","+upper+"]");
                        }

                        // Set FPS range
                        Range<Integer> range = new Range<>(lower, upper);
                        captureBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, range);

                        session.setRepeatingRequest(captureBuilder.build(), null, mainHandler);
                    } catch (CameraAccessException e) {
                        Log.e("ImageFeed", "Capture session setup failed", e);
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                }
            }, mainHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }





    private Bitmap fixFrontCameraOrientation(Bitmap bitmap) {
        rotationMatrix.reset();
        int rotation = ((Activity) context).getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0: rotationMatrix.postRotate(270); break;
            case Surface.ROTATION_90: rotationMatrix.postRotate(0); break;
            case Surface.ROTATION_180: rotationMatrix.postRotate(90); break;
            case Surface.ROTATION_270: rotationMatrix.postRotate(180); break;
        }
        rotationMatrix.postScale(-1, 1, bitmap.getWidth() / 2f, bitmap.getHeight() / 2f);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotationMatrix, true);
    }
    public void closeCamera() {
        // Close resources and avoid NullPointerExceptions
        if (cameraCaptureSession != null) cameraCaptureSession.close();
        if (cameraDevice != null) cameraDevice.close();
        if (imageReader != null) imageReader.close();
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
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

                //  end
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

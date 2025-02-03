package com.github.jaykkumar01.watchparty_duo.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.Looper;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.github.jaykkumar01.watchparty_duo.updates.AppData;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.*;

public class CameraHelper implements ImageReader.OnImageAvailableListener {


    private final Context context;
    private final ImageView imageView;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private ImageReader imageReader;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public CameraHelper(Context context, ImageView imageView) {
        this.context = context;
        this.imageView = imageView;
    }

    public void startCamera() {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = getFrontCameraId(manager);
            if (cameraId == null) {
                Toast.makeText(context, "Front camera not found", Toast.LENGTH_SHORT).show();
                return;
            }

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Camera permission not granted", Toast.LENGTH_SHORT).show();
                return;
            }

            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap configMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (configMap == null) {
                Toast.makeText(context, "StreamConfigurationMap not found!", Toast.LENGTH_SHORT).show();
                return;
            }

//            Size largestSize = getLargestJpegSize(configMap);
            Size largestSize = new Size(480,360);

            imageReader = ImageReader.newInstance(largestSize.getWidth(), largestSize.getHeight(), ImageFormat.JPEG, 60);
            imageReader.setOnImageAvailableListener(this,null);

            manager.openCamera(cameraId, stateCallback, null);

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

    private Size getLargestJpegSize(StreamConfigurationMap configMap) {
        Size[] jpegSizes = configMap.getOutputSizes(ImageFormat.JPEG);
        if (jpegSizes != null && jpegSizes.length > 0) {
            return jpegSizes[0]; // Highest available resolution
        }
        return new Size(480, 360); // Default fallback
    }

    private void processImage(ImageReader reader) {
        executorService.execute(() -> {
            Image image = reader.acquireLatestImage();
            if (image == null) return;


            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            image.close();

            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            Bitmap finalBitmap = fixFrontCameraOrientation(bitmap);

            ((CameraActivity) context).runOnUiThread(() -> imageView.setImageBitmap(finalBitmap));
        });
    }

    private Bitmap fixFrontCameraOrientation(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        int rotation = ((CameraActivity) context).getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                matrix.postRotate(270);
                break;
            case Surface.ROTATION_90:
                matrix.postRotate(0);
                break;
            case Surface.ROTATION_180:
                matrix.postRotate(90);
                break;
            case Surface.ROTATION_270:
                matrix.postRotate(180);
                break;
        }
        matrix.postScale(-1, 1, bitmap.getWidth() / 2f, bitmap.getHeight() / 2f);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
        }
    };

    private void createCameraPreview() {
        try {
            cameraDevice.createCaptureSession(Collections.singletonList(imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null) return;
                    cameraCaptureSession = session;

                    scheduler.scheduleWithFixedDelay(() -> {
                        takePicture();
                    }, 300, 1000/ AppData.getInstance().getFPS(), TimeUnit.MILLISECONDS);
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(context, "Configuration failed", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void takePicture() {
        if (cameraDevice == null) return;
        try {
            CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureBuilder.addTarget(imageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
            cameraCaptureSession.capture(captureBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void closeCamera() {
        if (cameraCaptureSession != null) cameraCaptureSession.close();
        if (cameraDevice != null) cameraDevice.close();
        if (imageReader != null) imageReader.close();
        scheduler.shutdown();
        executorService.shutdown();
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        processImage(reader);
    }
}

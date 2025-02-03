package com.github.jaykkumar01.watchparty_duo.helpers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.camera2.*;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.Looper;
import android.util.Size;
import android.view.Surface;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.github.jaykkumar01.watchparty_duo.activities.CameraActivity;
import com.github.jaykkumar01.watchparty_duo.updates.AppData;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.concurrent.*;

public class CameraHelper implements ImageReader.OnImageAvailableListener {

    private final Context context;
    private final ImageView imageView;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private ImageReader imageReader;
    private final Handler mainHandler = new Handler(Looper.getMainLooper()); // Handler for the main thread
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final Matrix rotationMatrix = new Matrix(); // Reuse Matrix for orientation fixes

    private volatile int currentFPS = 30;  // Default FPS

    public CameraHelper(Context context, ImageView imageView) {
        this.context = context;
        this.imageView = imageView;
    }

    public void startCamera() {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = getFrontCameraId(manager);
            if (cameraId == null) {
                showToast("Front camera not found");
                return;
            }

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                showToast("Camera permission not granted");
                return;
            }

            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap configMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (configMap == null) {
                showToast("StreamConfigurationMap not found!");
                return;
            }

            // Use fixed resolution for simplicity
            Size previewSize = getPreviewSize(AppData.getInstance().getImageHeight());
            imageReader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(), ImageFormat.JPEG, 10);
            imageReader.setOnImageAvailableListener(this, mainHandler); // Using the mainHandler for the listener

            manager.openCamera(cameraId, stateCallback, mainHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
            showToast("Camera access failed");
        }
    }

    private Size getPreviewSize(int height) {
        int width = (int) (height * (4.0 / 3.0));
        return new Size(width, height);
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

    private void processImage(ImageReader reader) {
        try (Image image = reader.acquireLatestImage()) {
            if (image == null) return;

            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);

            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            Bitmap finalBitmap = fixFrontCameraOrientation(bitmap);

            // Posting to the main thread to update UI
            mainHandler.post(() -> imageView.setImageBitmap(finalBitmap));
        }
    }

    private Bitmap fixFrontCameraOrientation(Bitmap bitmap) {
        rotationMatrix.reset();
        int rotation = ((CameraActivity) context).getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0: rotationMatrix.postRotate(270); break;
            case Surface.ROTATION_90: rotationMatrix.postRotate(0); break;
            case Surface.ROTATION_180: rotationMatrix.postRotate(90); break;
            case Surface.ROTATION_270: rotationMatrix.postRotate(180); break;
        }
        rotationMatrix.postScale(-1, 1, bitmap.getWidth() / 2f, bitmap.getHeight() / 2f);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotationMatrix, true);
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
            showToast("Camera error occurred");
        }
    };

    private void createCameraPreview() {
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
                        CaptureRequest build = captureBuilder.build();

                        if (scheduler.isShutdown()){
                            scheduler = Executors.newSingleThreadScheduledExecutor();
                        }
                        scheduler.scheduleWithFixedDelay(
                                () -> showPicture(build),
                                300,
                                1000/currentFPS,
                                TimeUnit.MILLISECONDS
                        );
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                        showToast("Capture failed");
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    showToast("Camera configuration failed");
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            showToast("Camera preview setup failed");
        }
    }

    private void showPicture(CaptureRequest build) {
        try {
            cameraCaptureSession.capture(build, null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void closeCamera() {
        // Close resources and avoid NullPointerExceptions
        if (cameraCaptureSession != null) cameraCaptureSession.close();
        if (cameraDevice != null) cameraDevice.close();
        if (imageReader != null) imageReader.close();
        // Don't shut down scheduler; allow reuse
        if (!scheduler.isShutdown()) {
            scheduler.shutdownNow(); // Shut down now and reset
        }
        currentFPS = 30;
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        processImage(reader);
    }

    // Method to change FPS dynamically during execution
    public void setFPS(int fps) {
        if (fps <= 0) {
            showToast("FPS must be a positive number");
            return;
        }

        // Close the camera and stop the scheduled task
        closeCamera();

        // Update the current FPS
        this.currentFPS = fps;

        // Delay the restart to avoid quick reopen issues
        new Handler(Looper.getMainLooper()).postDelayed(this::startCamera, 500);
    }

    private void showToast(String message) {
        mainHandler.post(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
    }
}

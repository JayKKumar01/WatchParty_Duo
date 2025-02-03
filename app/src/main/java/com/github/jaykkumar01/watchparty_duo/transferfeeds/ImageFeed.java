package com.github.jaykkumar01.watchparty_duo.transferfeeds;

import android.Manifest;
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
import android.util.Size;
import android.view.Surface;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.github.jaykkumar01.watchparty_duo.activities.CameraActivity;
import com.github.jaykkumar01.watchparty_duo.activities.PlayerActivity;
import com.github.jaykkumar01.watchparty_duo.services.ConnectionService;
import com.github.jaykkumar01.watchparty_duo.updates.AppData;
import com.github.jaykkumar01.watchparty_duo.utils.Base;
import com.github.jaykkumar01.watchparty_duo.utils.BitmapUtils;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ImageFeed implements ImageReader.OnImageAvailableListener{
    private final Context context;
    private final ImageView imageView;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private ImageReader imageReader;
    private final Handler mainHandler = new Handler(Looper.getMainLooper()); // Handler for the main thread
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private static final Matrix rotationMatrix = new Matrix(); // Reuse Matrix for orientation fixes

    public  ImageFeed(Context context, ImageView imageView){
        this.context = context;
        this.imageView = imageView;
    }
    public void startCamera() {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = getFrontCameraId(manager);
            if (cameraId == null) {
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

            // Use fixed resolution for simplicity
            Size previewSize = getPreviewSize(AppData.getInstance().getImageHeight());
            imageReader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(), ImageFormat.JPEG, 10);
            imageReader.setOnImageAvailableListener(this, mainHandler); // Using the mainHandler for the listener

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

    private Size getPreviewSize(int height) {
        int width = (int) (height * (4.0 / 3.0));
        return new Size(width, height);
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
            camera.close();
            cameraDevice = null;
        }
    };

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
                        CaptureRequest build = captureBuilder.build();

                        if (scheduler.isShutdown()){
                            scheduler = Executors.newSingleThreadScheduledExecutor();
                        }
                        scheduler.scheduleWithFixedDelay(
                                () -> showPicture(build),
                                300,
                                1000/AppData.getInstance().getFPS(),
                                TimeUnit.MILLISECONDS
                        );
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
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

    private void showPicture(CaptureRequest build) {
        try {
            cameraCaptureSession.capture(build, null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void processImage(ImageReader reader) {
        try (Image image = reader.acquireLatestImage()) {
            if (image == null) return;

            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);

            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            Bitmap finalBitmap = fixFrontCameraOrientation(bitmap);
            byte[] imageFeedBytes = BitmapUtils.getBytes(finalBitmap);
            mainHandler.post(() -> {
                imageView.setImageBitmap(finalBitmap);
                if (ConnectionService.getInstance() != null){
                    ConnectionService.getInstance().sendImageFeed(imageFeedBytes);
                }
            });
        }
    }

    private Bitmap fixFrontCameraOrientation(Bitmap bitmap) {
        rotationMatrix.reset();
        int rotation = ((PlayerActivity) context).getWindowManager().getDefaultDisplay().getRotation();
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
        processImage(reader);
    }
}

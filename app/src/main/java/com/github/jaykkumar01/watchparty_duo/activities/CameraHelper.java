package com.github.jaykkumar01.watchparty_duo.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.github.jaykkumar01.watchparty_duo.R;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class CameraHelper {

    private Context context;
    private ImageView imageView;
    private TextureView textureView;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private ImageReader imageReader;

    public CameraHelper(Context context, ImageView imageView, TextureView textureView) {
        this.context = context;
        this.imageView = imageView;
        this.textureView = textureView;
    }

    public TextureView getTextureView() {
        return textureView;
    }

    public TextureView.SurfaceTextureListener getTextureListener() {
        return new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                startCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
        };
    }

    public void startCamera() {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = null;
            // Find the front-facing camera
            for (String id : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    cameraId = id;
                    break;
                }
            }

            if (cameraId == null) {
                Toast.makeText(context, "Front camera not found", Toast.LENGTH_SHORT).show();
                return;
            }

            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            Size[] jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    .getOutputSizes(ImageFormat.JPEG);

            // Select the largest resolution available for preview
            int width = 3060; // default, you can adjust it dynamically
            int height = 4080; // default, adjust dynamically

            if (jpegSizes != null && jpegSizes.length > 0) {
                // Choose the highest resolution available
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }

            imageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 2); // Increase the max images for buffer
            imageReader.setOnImageAvailableListener(reader -> {
                Image image = reader.acquireLatestImage();
                Toast.makeText(context, "W: "+image.getWidth()+", H: "+image.getHeight(), Toast.LENGTH_SHORT).show();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);
                image.close();

                // Fix orientation and mirroring for front camera
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                bitmap = fixFrontCameraOrientation(bitmap);

                Bitmap finalBitmap = bitmap;
                ((CameraActivity) context).runOnUiThread(() -> imageView.setImageBitmap(finalBitmap));
            }, null);

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Bitmap fixFrontCameraOrientation(Bitmap bitmap) {
        Matrix matrix = new Matrix();

        // Get the orientation of the camera and apply the necessary transformation
        int rotation = ((CameraActivity) context).getWindowManager().getDefaultDisplay().getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                matrix.postRotate(270); // Adjust for portrait orientation
                break;
            case Surface.ROTATION_90:
                matrix.postRotate(0); // Adjust for landscape orientation
                break;
            case Surface.ROTATION_180:
                matrix.postRotate(90); // Adjust for reverse portrait
                break;
            case Surface.ROTATION_270:
                matrix.postRotate(180); // Adjust for reverse landscape
                break;
        }

        // Mirror the image horizontally (only for front camera)
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
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };



    private void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            // Set the size for the texture view based on the camera resolution
            texture.setDefaultBufferSize(imageReader.getWidth(), imageReader.getHeight());
            Surface surface = new Surface(texture);

            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null) return;
                    cameraCaptureSession = session;
                    try {
                        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
                        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
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
            CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);

            cameraCaptureSession.capture(captureBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(context, "Picture taken", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}

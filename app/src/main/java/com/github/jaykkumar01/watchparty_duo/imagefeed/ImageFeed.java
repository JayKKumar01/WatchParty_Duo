package com.github.jaykkumar01.watchparty_duo.imagefeed;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.*;
import android.media.ImageReader;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import com.github.jaykkumar01.watchparty_duo.constants.Feed;
import com.github.jaykkumar01.watchparty_duo.listeners.FeedListener;
import com.github.jaykkumar01.watchparty_duo.utils.CameraUtil;

import java.util.Arrays;

public class ImageFeed{
    private final Context context;
    private final FeedListener feedListener;
    private final CameraModel cameraModel;
    private final CameraSessionManager sessionManager;



    public ImageFeed(Context context, FeedListener feedListener, TextureView textureView){
        this.context = context;
        this.feedListener = feedListener;
        this.cameraModel = new CameraModel(context);

        // Handler for the main thread
        this.sessionManager = new CameraSessionManager(context, cameraModel,feedListener,textureView);
    }

    public void initializeCamera(){
        updateListener("Ranges: "+ Arrays.toString(cameraModel.getFpsRanges()));
        updateListener("Optimal Range: "+ cameraModel.getOptimalFpsRange() + ", FPS: "+ Feed.FPS);

        sessionManager.openCamera(this::handleCameraSession);
    }



    private void handleCameraSession(CameraCaptureSession session, Surface surface) {

        try {
            CaptureRequest request = CameraSessionHelper.createCaptureRequest(
                    session.getDevice(),
                    surface,
                    cameraModel.getOptimalFpsRange()
            );
            if (request == null){
                Toast.makeText(context, "Capture Build Failed!", Toast.LENGTH_SHORT).show();
                return;
            }
            session.setRepeatingRequest(request, null, null);
        } catch (CameraAccessException e) {
            feedListener.onError("Session configuration failed: " + e.getMessage());
        }
    }

    public void releaseResources() {
        sessionManager.closeSession();
    }

    private void updateListener(String logMessage) {
        if (feedListener != null){
            feedListener.onUpdate(logMessage);
        }
    }
}

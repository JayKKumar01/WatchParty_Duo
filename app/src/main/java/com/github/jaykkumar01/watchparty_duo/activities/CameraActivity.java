package com.github.jaykkumar01.watchparty_duo.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.TextureView;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.jaykkumar01.watchparty_duo.R;

public class CameraActivity extends AppCompatActivity {

    private CameraHelper cameraHelper;

    TextureView textureView;
    ImageView imageView;
    Button btnCapture;
    ConstraintLayout imageViewLayout,textureViewLayout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imageView = findViewById(R.id.imageView);
        btnCapture = findViewById(R.id.btnCapture);
        textureView = findViewById(R.id.textureView);
        imageViewLayout = findViewById(R.id.imageViewLayout);
        textureViewLayout = findViewById(R.id.textureViewLayout);

        cameraHelper = new CameraHelper(this, imageView, textureView);

        btnCapture.setOnClickListener(v -> cameraHelper.takePicture());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            cameraHelper.startCamera();
        }
    }





    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Handle layout change for landscape orientation
            adjustLayoutForLandscape();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Handle layout change for portrait orientation
            adjustLayoutForPortrait();
        }
    }

    private void adjustLayoutForLandscape() {
        // Modify layout when in landscape mode
        ConstraintLayout.LayoutParams textureViewLayoutParams = (ConstraintLayout.LayoutParams) textureViewLayout.getLayoutParams();
        ConstraintLayout.LayoutParams imageViewLayoutParams = (ConstraintLayout.LayoutParams) imageViewLayout.getLayoutParams();


        textureViewLayoutParams.verticalBias = 0.5f;
        textureViewLayoutParams.horizontalBias = 0;
        textureViewLayoutParams.matchConstraintPercentWidth = 0.5f;
        textureViewLayoutParams.matchConstraintPercentHeight = 1f;

        imageViewLayoutParams.verticalBias = 0.5f;
        imageViewLayoutParams.horizontalBias = 1f;
        imageViewLayoutParams.matchConstraintPercentWidth = 0.5f;
        imageViewLayoutParams.matchConstraintPercentHeight = 1f;




        textureViewLayout.setLayoutParams(textureViewLayoutParams);
        imageViewLayout.setLayoutParams(imageViewLayoutParams);
    }


    private void adjustLayoutForPortrait() {
        // Modify layout when in portrait mode (stack vertically)
        ConstraintLayout.LayoutParams textureViewLayoutParams = (ConstraintLayout.LayoutParams) textureViewLayout.getLayoutParams();
        ConstraintLayout.LayoutParams imageViewLayoutParams = (ConstraintLayout.LayoutParams) imageViewLayout.getLayoutParams();


        textureViewLayoutParams.verticalBias = 0;
        textureViewLayoutParams.horizontalBias = 0.5f;
        textureViewLayoutParams.matchConstraintPercentWidth = 1f;
        textureViewLayoutParams.matchConstraintPercentHeight = 0.5f;

        imageViewLayoutParams.verticalBias = 1;
        imageViewLayoutParams.horizontalBias = 0.5f;
        imageViewLayoutParams.matchConstraintPercentWidth = 1f;
        imageViewLayoutParams.matchConstraintPercentHeight = 0.5f;




        textureViewLayout.setLayoutParams(textureViewLayoutParams);
        imageViewLayout.setLayoutParams(imageViewLayoutParams);
    }

    private void setToParent(ConstraintLayout.LayoutParams iewParams) {
        iewParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        iewParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
        iewParams.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
        iewParams.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
    }
}

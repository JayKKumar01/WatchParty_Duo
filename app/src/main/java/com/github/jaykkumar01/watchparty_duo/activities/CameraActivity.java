package com.github.jaykkumar01.watchparty_duo.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
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
import com.github.jaykkumar01.watchparty_duo.helpers.CameraHelper;
import com.google.android.material.textfield.TextInputEditText;

public class CameraActivity extends AppCompatActivity {

    private CameraHelper cameraHelper;
    ImageView imageView;
    ConstraintLayout imageViewLayout;
    private TextInputEditText etFPS; // Reference to the EditText for FPS input


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
        imageViewLayout = findViewById(R.id.imageViewLayout);

        cameraHelper = new CameraHelper(this, imageView);
        etFPS = findViewById(R.id.etJoinName);


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
        ConstraintLayout.LayoutParams imageViewLayoutParams = (ConstraintLayout.LayoutParams) imageViewLayout.getLayoutParams();

        imageViewLayoutParams.matchConstraintPercentWidth = 0.5f;
        imageViewLayoutParams.matchConstraintPercentHeight = 1f;

        imageViewLayout.setLayoutParams(imageViewLayoutParams);
    }


    private void adjustLayoutForPortrait() {
        ConstraintLayout.LayoutParams imageViewLayoutParams = (ConstraintLayout.LayoutParams) imageViewLayout.getLayoutParams();

        imageViewLayoutParams.matchConstraintPercentWidth = 1f;
        imageViewLayoutParams.matchConstraintPercentHeight = 0.5f;

        imageViewLayout.setLayoutParams(imageViewLayoutParams);
    }


    // Method that is triggered when the user clicks "Set FPS"
    public void changeFPS(View view) {
        if (etFPS.getText() == null){
            return;
        }
        String fpsText = etFPS.getText().toString();

        // Validate the input
        if (fpsText.isEmpty()) {
            return;
        }
        int fps = Integer.parseInt(fpsText);
        cameraHelper.setFPS(fps);
        etFPS.setText("");
    }
}

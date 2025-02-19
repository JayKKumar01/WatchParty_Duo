package com.github.jaykkumar01.watchparty_duo.activities;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.jaykkumar01.watchparty_duo.R;
import com.github.jaykkumar01.watchparty_duo.gestures.MovementListener;

public class TestingActivity extends AppCompatActivity {

    private View view;
    private ConstraintLayout parent;
    private MovementListener movementListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_testing);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        view = findViewById(R.id.view);
        parent = findViewById(R.id.parentView);
        movementListener = new MovementListener(view);
        view.setOnTouchListener(movementListener);
    }




    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        view.setTranslationX(0);
        view.setTranslationY(0);

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) parent.getLayoutParams();

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            params.dimensionRatio = "16:9";
        }else {
            params.dimensionRatio = "1:1";
        }
        parent.setLayoutParams(params);
        movementListener.resetBoundaries();
    }
}



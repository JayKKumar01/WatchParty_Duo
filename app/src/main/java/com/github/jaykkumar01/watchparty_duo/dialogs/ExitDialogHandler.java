package com.github.jaykkumar01.watchparty_duo.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.jaykkumar01.watchparty_duo.R;
import com.github.jaykkumar01.watchparty_duo.activities.FeedActivity;

public class ExitDialogHandler {

    private AlertDialog exitDialog;
    private final Context context;
    private final Handler handler = new Handler();
    private int countdown = 3;
    private TextView progressText;

    public ExitDialogHandler(Context context) {
        this.context = context;
    }

    public void showExitDialog() {
        if (exitDialog != null && exitDialog.isShowing()) {
            return; // Prevent multiple dialogs
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_connecting, null);

        progressText = dialogView.findViewById(R.id.progress_text);
        Button homeButton = dialogView.findViewById(R.id.home_button);

        if (progressText != null) {
            progressText.setText("Exiting... in " + countdown);
        }

        homeButton.setOnClickListener(v -> {
            goToHomepage();
        });

        builder.setView(dialogView);
        builder.setCancelable(false);
        exitDialog = builder.create();
        exitDialog.show();

        startCountdown();
    }

    private void startCountdown() {
        countdown = 3; // Reset countdown before starting
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (exitDialog != null && exitDialog.isShowing() && progressText != null) {
                    // Ensure UI update runs on the main thread
                    progressText.post(() -> progressText.setText("Exiting... in " + countdown));
                    countdown--;

                    if (countdown >= 0) {
                        handler.postDelayed(this, 1000);
                    } else {
                        dismissExitDialog();
                        goToHomepage();
                    }
                }
            }
        }, 1000);
    }

    public void dismissExitDialog() {
        if (exitDialog != null && exitDialog.isShowing()) {
            exitDialog.dismiss();
        }
    }

    private void goToHomepage() {
        Intent intent = new Intent(context, FeedActivity.class);
        ((Activity) context).finish();
        context.startActivity(intent);
    }
}

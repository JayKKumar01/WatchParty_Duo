package com.github.jaykkumar01.watchparty_duo.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.jaykkumar01.watchparty_duo.R;
import com.github.jaykkumar01.watchparty_duo.activities.FeedActivity;

@SuppressLint("SetTextI18n")
public class ConnectionDialogHandler {
    private AlertDialog connectingDialog;
    private TextView progressText;
    private final Context context;

    public ConnectionDialogHandler(Context context) {
        this.context = context;
    }

    public void showConnectingDialog() {
        if (connectingDialog != null && connectingDialog.isShowing()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_connecting, null);
        progressText = dialogView.findViewById(R.id.progress_text);
        Button homeButton = dialogView.findViewById(R.id.home_button);

        progressText.setText("Connecting... (Step 0/2)");

        homeButton.setOnClickListener(v -> {
            goToHomepage();
        });

        builder.setView(dialogView);
        builder.setCancelable(false);
        connectingDialog = builder.create();
        connectingDialog.show();
    }

    private void goToHomepage() {
        Intent intent = new Intent(context, FeedActivity.class);
        ((Activity) context).finish();
        context.startActivity(intent);
    }


    public void updateConnectingDialog(int step) {
        if (connectingDialog != null && connectingDialog.isShowing()) {
            progressText.setText("Connecting... (Step " + step + "/2)");
        }
    }

    public void dismissConnectingDialog() {
        if (connectingDialog != null && connectingDialog.isShowing()) {
            connectingDialog.dismiss();
        }
    }
}


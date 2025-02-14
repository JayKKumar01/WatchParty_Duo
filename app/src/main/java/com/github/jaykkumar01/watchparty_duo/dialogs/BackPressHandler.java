package com.github.jaykkumar01.watchparty_duo.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.lifecycle.LifecycleOwner;

import com.github.jaykkumar01.watchparty_duo.R;

public class BackPressHandler {

    public static void registerBackPressHandler(Activity activity, OnBackPressedDispatcher dispatcher, int gravity) {
        dispatcher.addCallback((LifecycleOwner) activity, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmationDialog(activity, gravity);
            }
        });
    }

    private static void showExitConfirmationDialog(Activity activity, int gravity) {
        Dialog dialog = new Dialog(activity);
        View view = LayoutInflater.from(activity).inflate(R.layout.custom_exit_dialog, null);
        dialog.setContentView(view);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setGravity(gravity);
        }

        // Find UI elements
        TextView dialogMessage = view.findViewById(R.id.dialogMessage);
        Button btnNo = view.findViewById(R.id.btnNo);
        Button btnYes = view.findViewById(R.id.btnYes);


        // Dismiss on No
        btnNo.setOnClickListener(v -> dialog.dismiss());

        // Exit on Yes
        btnYes.setOnClickListener(v -> {
            dialog.dismiss();
            activity.finish();
        });

        dialog.show();
    }
}

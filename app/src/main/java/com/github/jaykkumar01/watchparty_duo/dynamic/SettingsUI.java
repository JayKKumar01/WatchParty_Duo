package com.github.jaykkumar01.watchparty_duo.dynamic;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.github.jaykkumar01.watchparty_duo.R;
import com.github.jaykkumar01.watchparty_duo.constants.Feed;

public class SettingsUI {

    private static final String[] options = {"Bad", "Good", "Better", "Best"};
    private static final int[] latencyValues = {1000, 750, 500, 250};
    private static final int[] resolutionValues = {144, 240, 360, 480};
    private static final int[] fpsValues = {5, 12, 20, 30};
    private static final int DEFAULT_LATENCY = Feed.LATENCY;
    private static final int DEFAULT_RESOLUTION = Feed.RESOLUTION;
    private static final int DEFAULT_FPS = Feed.FPS;

    public static void showSettingsDialog(Context context, SettingsUICallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 30);
        layout.setBackgroundColor(ContextCompat.getColor(context,R.color.discord_bg));

        LinearLayout topLayout = new LinearLayout(context);
        topLayout.setOrientation(LinearLayout.HORIZONTAL);
        topLayout.setPadding(0, 0, 0, 20);

        TextView title = new TextView(context);
        title.setText(R.string.video_call_settings);
        title.setTextSize(18);
        title.setTextColor(ContextCompat.getColor(context,R.color.white));
        title.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        ImageButton btnReset = new ImageButton(context);
        btnReset.setImageResource(R.drawable.refresh);
        btnReset.setBackground(null);
        btnReset.setPadding(10, 10, 10, 10);

        topLayout.addView(title);
        topLayout.addView(btnReset);

        TextView tvLatency = createTextView(context, "Latency");
        RadioGroup rgLatency = createRadioGroup(context, latencyValues, Feed.LATENCY);

        TextView tvResolution = createTextView(context, "Resolution");
        RadioGroup rgResolution = createRadioGroup(context, resolutionValues, Feed.RESOLUTION);

        TextView tvFps = createTextView(context, "FPS");
        RadioGroup rgFps = createRadioGroup(context, fpsValues, Feed.FPS);

        Button btnSubmit = new Button(context);
        btnSubmit.setText(R.string.submit);
        btnSubmit.setTextColor(ContextCompat.getColor(context, R.color.theme_color));
        btnSubmit.setTypeface(null, android.graphics.Typeface.BOLD);


        layout.addView(topLayout);
        layout.addView(tvLatency);
        layout.addView(rgLatency);
        layout.addView(tvResolution);
        layout.addView(rgResolution);
        layout.addView(tvFps);
        layout.addView(rgFps);
        layout.addView(btnSubmit);

        builder.setView(layout);
        AlertDialog dialog = builder.create();

        btnSubmit.setOnClickListener(v -> {
            Feed.LATENCY = getSelectedValue(rgLatency, DEFAULT_LATENCY);
            Feed.RESOLUTION = getSelectedValue(rgResolution, DEFAULT_RESOLUTION);
            Feed.FPS = getSelectedValue(rgFps, DEFAULT_FPS);
            if (callback != null) {
                callback.onSubmit("Latency: " + Feed.LATENCY + " ms\n" +
                        "Resolution: " + Feed.RESOLUTION + "p\n" +
                        "FPS: " + Feed.FPS + " fps");
            }

            dialog.dismiss();
        });

        btnReset.setOnClickListener(v -> {
            Animation fade = new AlphaAnimation(1, 0);
            fade.setDuration(300);
            btnReset.startAnimation(fade);

            Feed.LATENCY = DEFAULT_LATENCY;
            Feed.RESOLUTION = DEFAULT_RESOLUTION;
            Feed.FPS = DEFAULT_FPS;

            updateRadioGroupSelection(rgLatency, latencyValues, DEFAULT_LATENCY);
            updateRadioGroupSelection(rgResolution, resolutionValues, DEFAULT_RESOLUTION);
            updateRadioGroupSelection(rgFps, fpsValues, DEFAULT_FPS);
        });

        dialog.show();
    }

    public interface SettingsUICallback {
        void onSubmit(String result);
    }

    private static TextView createTextView(Context context, String text) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextSize(18);
        textView.setTextColor(ContextCompat.getColor(context,R.color.white));
        return textView;
    }

    private static RadioGroup createRadioGroup(Context context, int[] values, int selectedValue) {
        RadioGroup radioGroup = new RadioGroup(context);
        radioGroup.setOrientation(RadioGroup.HORIZONTAL);

        for (int i = 0; i < options.length; i++) {
            RadioButton radioButton = new RadioButton(context);
            radioButton.setText(options[i]);
            radioButton.setTag(values[i]);
            radioButton.setTextColor(Color.WHITE);
            radioButton.setButtonTintList(ContextCompat.getColorStateList(context, R.color.theme_color));

            radioGroup.addView(radioButton);

            if (values[i] == selectedValue) {
                radioButton.setChecked(true);
            }
        }
        return radioGroup;
    }

    private static int getSelectedValue(RadioGroup radioGroup, int defaultValue) {
        int selectedId = radioGroup.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton selectedButton = radioGroup.findViewById(selectedId);
            return Integer.parseInt(selectedButton.getTag().toString());
        }
        return defaultValue;
    }

    private static void updateRadioGroupSelection(RadioGroup radioGroup, int[] values, int newValue) {
        for (int i = 0; i < values.length; i++) {
            RadioButton radioButton = (RadioButton) radioGroup.getChildAt(i);
            if ((int) radioButton.getTag() == newValue) {
                radioButton.setChecked(true);
                break;
            }
        }
    }
}

package com.github.jaykkumar01.watchparty_duo.peerjswebview;

import android.content.Context;
import android.util.Base64;
import android.webkit.WebView;
import android.widget.Toast;

import com.github.jaykkumar01.watchparty_duo.listeners.UpdateListener;
import com.github.jaykkumar01.watchparty_duo.utils.ObjectUtil;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebSocketSender {
    private ScheduledExecutorService senderExecutor = Executors.newSingleThreadScheduledExecutor();
    private final Queue<String> base64Queue = new ConcurrentLinkedQueue<>();
    private Context context;

    private UpdateListener updateListener;

    public WebSocketSender(Context context) {
        this.context = context;
    }

    public void setUpdateListener(UpdateListener updateListener){
        this.updateListener = updateListener;
    }

    public void initializeSender(WebView webView) {

        if (senderExecutor.isShutdown()){
            senderExecutor = Executors.newSingleThreadScheduledExecutor();
        }

        Random random = new Random();

        // Schedule batch sender every 333ms
        senderExecutor.scheduleWithFixedDelay(
                () -> {
                    synchronized (base64Queue) {
                        if (!base64Queue.isEmpty()) {
                            List<String> batch = new ArrayList<>(base64Queue);
                            base64Queue.clear();
                            String json = new Gson().toJson(batch);

                            webView.post(() -> {
                                webView.evaluateJavascript(
                                        "receiveFromAndroid(" + json + ")",
                                        null
                                );
                            });
                        }
                    }
                },
                0,
                333,
                TimeUnit.MILLISECONDS
        );
    }

    // Add image data with background conversion
    public void addImageData(byte[] imageBytes) {
        Executors.newCachedThreadPool().execute(() -> {
            String base64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
            synchronized (base64Queue) {
                base64Queue.add(base64);
            }
        });
    }
}

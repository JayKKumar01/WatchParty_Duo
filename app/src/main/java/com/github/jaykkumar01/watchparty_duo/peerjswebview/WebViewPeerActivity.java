package com.github.jaykkumar01.watchparty_duo.peerjswebview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.jaykkumar01.watchparty_duo.R;
import com.github.jaykkumar01.watchparty_duo.audiofeed.AudioFeed;
import com.github.jaykkumar01.watchparty_duo.constants.FeedType;
import com.github.jaykkumar01.watchparty_duo.helpers.LogUpdater;
import com.github.jaykkumar01.watchparty_duo.helpers.ProcessFeed;
import com.github.jaykkumar01.watchparty_duo.listeners.FeedListener;
import com.github.jaykkumar01.watchparty_duo.listeners.UpdateListener;
import com.github.jaykkumar01.watchparty_duo.models.FeedModel;
import com.github.jaykkumar01.watchparty_duo.imagefeed.ImageFeed;
//import com.github.jaykkumar01.watchparty_duo.transferfeeds.ImageFeed1;
import com.github.jaykkumar01.watchparty_duo.models.PacketModel;
import com.github.jaykkumar01.watchparty_duo.utils.Base;
import com.github.jaykkumar01.watchparty_duo.utils.ObjectUtil;
import com.github.jaykkumar01.watchparty_duo.utils.PermissionHandler;
import com.github.jaykkumar01.watchparty_duo.webviewutils.PeerListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressLint("SetTextI18n")
public class WebViewPeerActivity extends AppCompatActivity implements PeerListener, FeedListener, UpdateListener{

    private ScrollView logScrollView;
    private TextView logTextView;
    private WebView webView;
    private ConstraintLayout layoutConnection;
    private ConstraintLayout layoutConnect;
    private ConstraintLayout layoutJoin;
    private TextInputEditText etJoinName;
    private TextInputEditText etCode;
    private AppCompatButton btnConnect;
    private AppCompatButton btnJoin;
    private TextView tvName;

    private String userName;
    private long startTime;

    private final PacketModel packetModel = new PacketModel();
    private final Handler updateLogHandler = new Handler(Looper.getMainLooper());
    private ImageFeed imageFeed;
    private AudioFeed audioFeed;

    private WebSocketSender socketSender;


    private ConstraintLayout imageFeedLayout;
    private TextureView peerFeedTextureView,remoteFeedTextureView;
    private final Gson gson = new Gson();

    private ProcessFeed processFeed;
    private LogUpdater logUpdater;
    private boolean isMute = true;
    private boolean isRunning = true;
    private boolean isConnectionOpen = false;
    private ExecutorService packetExecutor = Executors.newCachedThreadPool();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_webview_peer);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        initWebView();
        logUpdater = new LogUpdater(logTextView,logScrollView);

        processFeed = new ProcessFeed(remoteFeedTextureView,this);


        boolean isTesting = false;


        imageFeed = new ImageFeed(this,this,isTesting ? remoteFeedTextureView: peerFeedTextureView);
        audioFeed = new AudioFeed(this,this);



        socketSender = new WebSocketSender(this);
        socketSender.setUpdateListener(this);

        if (isTesting){
            imageFeed.initializeCamera();
            socketSender.initializeSender(webView);
            layoutConnection.setVisibility(View.GONE);
            imageFeedLayout.setVisibility(View.VISIBLE);
        }
    }

    private void initViews() {
        logScrollView = findViewById(R.id.logScrollView);
        // Listener to detect user scroll events
        logScrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            boolean isUserScrolling = logScrollView.getScrollY() < logTextView.getHeight() - logScrollView.getHeight();
            logUpdater.setUserScrolling(isUserScrolling);
        });
        logTextView = findViewById(R.id.logTextView);

        webView = findViewById(R.id.webView);
        layoutConnection = findViewById(R.id.layoutConnection);
        layoutConnect = findViewById(R.id.layoutConnect);
        layoutJoin = findViewById(R.id.layoutJoin);
        etJoinName = findViewById(R.id.etJoinName);
        etCode = findViewById(R.id.etCode);
        btnConnect = findViewById(R.id.btnConnect);
        btnConnect.setEnabled(false);
        btnJoin = findViewById(R.id.btnJoin);
        tvName = findViewById(R.id.tvName);
        imageFeedLayout = findViewById(R.id.imageFeedLayout);
        peerFeedTextureView = findViewById(R.id.peerFeed);
        remoteFeedTextureView = findViewById(R.id.remoteFeed);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);

        settings.setUseWideViewPort(false);
        settings.setLoadWithOverviewMode(false);

        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);

        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL); // For maximum compatibility
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setDomStorageEnabled(true);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(true);

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                updateLogs("Loading page...");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                updateLogs("Page loaded: " + url);
                btnConnect.setEnabled(true);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, android.webkit.WebResourceError error) {
                super.onReceivedError(view, request, error);
                updateLogs("Page error: " + error.getDescription());
            }
        });

        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new JavaScriptInterface(WebViewPeerActivity.this,this), "Android");

        // Load your peerjs-related HTML/JS page here
        webView.loadUrl("file:///android_asset/index.html");
    }

    // Connect Logic
    public void connect(View view) {
        if (!PermissionHandler.hasPermission(this)) return;

        btnConnect.setText(R.string.connecting);
        btnConnect.setEnabled(false);

        userName = etJoinName.getText().toString().trim();
        if (userName.isEmpty()) {
            showToast("Please enter your name.");
            resetConnectButton();
            return;
        }

        int randomCode = Base.generateRandomCode(6);
        startTime = System.currentTimeMillis();

        callJavaScript("initPeer",randomCode+"");
    }

    // Join Logic
    public void join(View view) {
        btnJoin.setText(R.string.joining);
        btnJoin.setEnabled(false);

        String remoteId = etCode.getText().toString().trim();
        if (remoteId.isEmpty()) {
            showToast("Please enter Peer ID.");
            resetJoinButton();
            return;
        }

        startTime = System.currentTimeMillis();

        callJavaScript("connect",remoteId);
    }

    @Override
    public void onPeerOpen(String peerId) {
        runOnUiThread(() -> {
            updateLogs("Peer Opened: " + peerId);

            hideKeyboard();
            logElapsedTime("Peer Opened Successfully!");
            tvName.setText("Welcome " + userName + ", Your ID: " + peerId);
            layoutConnect.setVisibility(View.GONE);
            layoutJoin.setVisibility(View.VISIBLE);
            resetConnectButton();
        });
    }

    @Override
    public void onConnectionOpen(String peerId, String remoteId) {
        isConnectionOpen = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logElapsedTime("Peer Connected Successfully!");
                updateLogs("Peer: " + peerId + ", Remote: " + remoteId);
                hideKeyboard();
                layoutJoin.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                resetJoinButton();
                socketSender.initializeSender(webView);
                imageFeed.initializeCamera();
                processFeed.startAudioProcess();
                imageFeedLayout.setVisibility(View.VISIBLE);
                startLoggingImageUpdates();
            }
        });
    }

    @Override
    public void onBatchReceived(String jsonData) {
        if (packetExecutor.isShutdown()){
            packetExecutor = Executors.newCachedThreadPool();
        }
        packetExecutor.execute(() -> {
            try {

                List<FeedModel> batch = gson.fromJson(
                        jsonData,
                        new TypeToken<List<FeedModel>>(){}.getType()
                );

                List<FeedModel> imageFeeds = new ArrayList<>();
                List<FeedModel> audioFeeds = new ArrayList<>();

                for (FeedModel model: batch){
                    switch (model.getFeedType()){
                        case FeedType.IMAGE_FEED:
                            packetModel.imageFeedReceived();
                            imageFeeds.add(model);
                        break;
                        case FeedType.AUDIO_FEED:
                            packetModel.audioFeedReceived();
                            audioFeeds.add(model);
                        break;
                    }
                }
                Executors.newCachedThreadPool().execute(() -> processFeed.processImageFeed(imageFeeds));
                Executors.newCachedThreadPool().execute(() -> processFeed.processAudioFeed(audioFeeds));

            } catch (Exception e) {
                Log.e("WebSocketReceiver", "Error processing batch", e);
            }
        });

    }


    private void startLoggingImageUpdates() {
        updateLogHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateLogs(packetModel.toString());
                packetModel.reset();
                // Continue updating every second
                updateLogHandler.postDelayed(this, 1000);
            }
        }, 1000);
    }
    public void callJavaScript(String func, Object... args) {
        StringBuilder argString = new StringBuilder();
        if (args.length > 0) {
            if (args[0] instanceof String) {
                argString.append(ObjectUtil.preserveString((String) args[0]));
            } else if (args[0] instanceof byte[]) {
                argString.append(ObjectUtil.preserveBytes((byte[]) args[0]));
            } else {
                argString.append(args[0]);
            }
        }
        for (int i = 1; i < args.length; i++) {
            argString.append(",");
            if (args[i] instanceof String) {
                argString.append(ObjectUtil.preserveString((String) args[i]));
            } else if (args[i] instanceof byte[]) {
                argString.append(ObjectUtil.preserveBytes((byte[]) args[i]));
            } else {
                argString.append(args[i]);
            }
        }
        final String javascriptCommand = String.format("javascript:%s(%s)", func, argString.toString());
        if (webView != null) {
            webView.loadUrl(javascriptCommand);
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateLogs(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logUpdater.addLogMessage(message);
            }
        });
    }



    private void resetConnectButton() {
        btnConnect.setText(R.string.connect);
        btnConnect.setEnabled(true);
    }

    private void resetJoinButton() {
        btnJoin.setText(R.string.join);
        btnJoin.setEnabled(true);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void logElapsedTime(String message) {
        updateLogs(message + " Time taken: " + (System.currentTimeMillis() - startTime) / 1000.0 + " sec");
    }

    // Method to hide keyboard
    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onFeed(byte[] bytes, long millis,int feedType) {
        socketSender.addData(bytes,millis,feedType);
        if (feedType == FeedType.IMAGE_FEED) {
            packetModel.imageFeedSent();
        }else if (feedType == FeedType.AUDIO_FEED) {
            packetModel.audioFeedSent();
        }

    }

    @Override
    public void onError(String err) {

    }

    @Override
    public void onUpdate(String updateMessage) {
        updateLogs(updateMessage);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // Handle layout change for landscape orientation
//            adjustLayoutForLandscape();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Handle layout change for portrait orientation
//            adjustLayoutForPortrait();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        isRunning = true;
        if (isConnectionOpen) {
            imageFeed.initializeCamera();
            processFeed.startImageProcess();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        isRunning = false;
        imageFeed.releaseResources();
        processFeed.stopImageProcess();
    }

    public void mic(View view) {
        ImageView imageView = (ImageView) view;
        isMute = !isMute;
        if (isMute){
            audioFeed.stop();
        }else {
            audioFeed.start();
        }
        imageView.setImageResource(isMute ? R.drawable.mic_off : R.drawable.mic_on);
    }
}

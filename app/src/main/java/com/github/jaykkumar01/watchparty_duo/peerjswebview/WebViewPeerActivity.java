package com.github.jaykkumar01.watchparty_duo.peerjswebview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.jaykkumar01.watchparty_duo.R;
import com.github.jaykkumar01.watchparty_duo.peerjswebview.JavaScriptInterface;
import com.github.jaykkumar01.watchparty_duo.utils.Base;
import com.github.jaykkumar01.watchparty_duo.utils.ObjectUtil;
import com.github.jaykkumar01.watchparty_duo.utils.PermissionHandler;
import com.github.jaykkumar01.watchparty_duo.webviewutils.PeerListener;
import com.google.android.material.textfield.TextInputEditText;

@SuppressLint("SetTextI18n")
public class WebViewPeerActivity extends AppCompatActivity implements PeerListener {

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

    private int sentCount = 0;
    private int receivedCount = 0;
    private int totalBytesPerSecond = 0;
    private final Handler updateLogHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState){
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
    }

    private void initViews() {
        logScrollView = findViewById(R.id.logScrollView);
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
    }

    public void updateSettings(int newFps, int newChunkSize){
        callJavaScript("updateSettings",newFps, newChunkSize);
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
                logTextView.append("Loading page...\n");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                logTextView.append("Page loaded: " + url + "\n");
                btnConnect.setEnabled(true);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, android.webkit.WebResourceError error) {
                super.onReceivedError(view, request, error);
                logTextView.append("Page error: " + error.getDescription() + "\n");
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                logElapsedTime("All Peers Connected Successfully!");
                updateLogs("Peer: " + peerId + ", Remote: " + remoteId);
                hideKeyboard();
                layoutJoin.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                resetJoinButton();
                startLoggingImageUpdates();
            }
        });
    }

    @Override
    public void onReadImageFeed(String peerId, byte[] imageFeedBytes, long millis) {
        if (imageFeedBytes == null || imageFeedBytes.length == 0){
            return;
        }
        receivedCount++;
        totalBytesPerSecond += imageFeedBytes.length;

    }

    private void startLoggingImageUpdates() {
        updateLogHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateLogs("Updates: [" + sentCount + ", " + receivedCount + "], Size: "+(totalBytesPerSecond/1024.0) + " KB");

                // Reset sent and received counts
                sentCount = 0;
                receivedCount = 0;

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
        TextView logTextView = findViewById(R.id.logTextView);
        ScrollView logScrollView = findViewById(R.id.logScrollView);
        logTextView.append("\n" + message);
        logScrollView.post(() -> logScrollView.fullScroll(View.FOCUS_DOWN));
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
}

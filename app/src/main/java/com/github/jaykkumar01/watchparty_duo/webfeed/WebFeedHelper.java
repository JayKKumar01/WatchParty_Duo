package com.github.jaykkumar01.watchparty_duo.webfeed;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.github.jaykkumar01.watchparty_duo.activities.FeedActivity;
import com.github.jaykkumar01.watchparty_duo.peerjswebview.WebSocketSender;
import com.github.jaykkumar01.watchparty_duo.utils.ObjectUtil;

@SuppressLint("StaticFieldLeak")
public class WebFeedHelper {
    private final Context context;
    private WebView webView;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public WebFeedHelper(Context context) {
        this.context = context;
    }

    public void initWebView(WebFeedListener webFeedListener){
        mainHandler.post(() -> {
            setupWebView(webFeedListener);
        });
    }
    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView(WebFeedListener webFeedListener) {
        webView = new WebView(context);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(false);
        settings.setLoadWithOverviewMode(false);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setDomStorageEnabled(true);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(true);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                FeedActivity feedActivity = FeedActivity.getInstance();
                if (feedActivity != null){
                    feedActivity.onPageFinished(url);
                }
                callJavaScript("initPeer");
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
            }
        });

        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new WebBridge(webFeedListener), "Android");

        webView.loadUrl("file:///android_asset/index.html");
    }

    public void connect(String remoteId) {
        callJavaScript("connect",remoteId);
    }

    public void callJavaScript(String func, Object... args) {
        StringBuilder argString = new StringBuilder();
        if (args.length > 0) {
            if (args[0] instanceof String) {
                argString.append(ObjectUtil.preserveString((String) args[0]));
            } else {
                argString.append(args[0]);
            }
        }
        for (int i = 1; i < args.length; i++) {
            argString.append(",");
            if (args[i] instanceof String) {
                argString.append(ObjectUtil.preserveString((String) args[i]));
            } else {
                argString.append(args[i]);
            }
        }
        final String javascriptCommand = String.format("javascript:%s(%s)", func, argString.toString());
        if (webView != null) {
            mainHandler.post(() -> webView.loadUrl(javascriptCommand));

        }
    }

    public void destroy() {
        callJavaScript("closeConnectionAndDestroyPeer");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (webView != null) {
                    webView.stopLoading();
                    webView.clearHistory();
                    webView.clearCache(true);
                    webView.removeAllViews();
                    webView.destroy();
                }
            }
        };
        mainHandler.postDelayed(runnable,1000);
    }


    public WebView getWebView() {
        return webView;
    }
}

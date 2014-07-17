package com.williamsportsda.williamsport;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class WebViewActivity extends Activity {

    private WebView webView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("http://www.williamsportsda.com/live-video-player-app.php");
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //webView.getParent().removeView(webView);
        webView.setFocusable(true);
        webView.removeAllViews();
        webView.clearHistory();
        webView.destroy();
    }
}
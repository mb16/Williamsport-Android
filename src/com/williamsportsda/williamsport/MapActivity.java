package com.williamsportsda.williamsport;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class MapActivity extends Activity {

    private WebView webView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("http://maps.google.co.in/maps?q=16421 Lappans Road Williamsport MD 21795");
    }
}
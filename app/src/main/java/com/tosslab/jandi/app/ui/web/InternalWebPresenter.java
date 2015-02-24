package com.tosslab.jandi.app.ui.web;

import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.views.listeners.WebLoadingBar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Steve SeongUg Jung on 15. 2. 24..
 */
@EBean
public class InternalWebPresenter {

    @ViewById(R.id.web_internal_web)
    WebView webView;

    @ViewById(R.id.loading_internal_web)
    WebLoadingBar webLoadingBar;

    private WebViewClient webViewClient;
    private WebChromeClient webCromeClient;
    private String url;

    @AfterViews
    void initViews() {
        WebSettings webSettings = webView.getSettings();

        if (webViewClient != null) {
            webView.setWebViewClient(webViewClient);
        } else {
            webView.setWebViewClient(new WebViewClient());
        }

        if (webCromeClient != null) {
            webView.setWebChromeClient(webCromeClient);
        } else {
            webView.setWebChromeClient(new WebChromeClient());
        }
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setUserAgentString(webSettings.getUserAgentString() + " Jandi-Android-App");

        webView.loadUrl(url);
    }

    public void setWebViewClient(WebViewClient webViewClient) {
        this.webViewClient = webViewClient;

    }

    public void setWebCromeClient(WebChromeClient webCromeClient) {
        this.webCromeClient = webCromeClient;
    }

    public void setProgress(int newProgress) {
        if (newProgress > 0) {
            webLoadingBar.setProgress(newProgress);

            int visibility = webLoadingBar.getVisibility();

            int newVisibility = getVisibilityState(visibility, newProgress);

            if (newVisibility != -1) {
                webLoadingBar.setVisibility(newVisibility);
            }

        }
    }

    private int getVisibilityState(int visibility, int newProgress) {
        if (visibility != View.GONE && newProgress >= 100) {
            return View.GONE;
        } else if (visibility != View.VISIBLE && newProgress < 100) {
            return View.VISIBLE;
        } else {
            return -1;
        }
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

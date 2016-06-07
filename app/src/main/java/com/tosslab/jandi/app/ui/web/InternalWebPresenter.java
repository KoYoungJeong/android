package com.tosslab.jandi.app.ui.web;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.views.listeners.WebLoadingBar;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Steve SeongUg Jung on 15. 2. 24..
 */
@EBean
public class InternalWebPresenter {

    public static final String SUPPORT_URL = "http://support.jandi.com";
    @ViewById(R.id.web_internal_web)
    WebView webView;
    @ViewById(R.id.loading_internal_web)
    WebLoadingBar webLoadingBar;
    private WebViewClient webViewClient;
    private WebChromeClient webCromeClient;
    private String url;
    private ProgressWheel progressWheel;

    public void initObject(Activity activity) {
        progressWheel = new ProgressWheel(activity);
    }

    @AfterViews
    void initViews() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

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
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setUserAgentString(webSettings.getUserAgentString() + " Jandi-Android-App");

        loadUrl(url);
    }

    public void loadUrl(String url) {
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

    @UiThread
    public void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @UiThread
    public void showProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }

        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @UiThread
    public void showSuccessToast(Context context, String message) {
        ColoredToast.show(message);
    }

    @UiThread
    public void showErrorToast(Context context, String message) {
        ColoredToast.show(message);
    }

    public void moveOtherBrowser(Context context, String currentUrl) {

        String url;

        if (!currentUrl.startsWith("http://") && !currentUrl.startsWith("https://")) {
            url = "http://" + currentUrl;
        } else {
            url = currentUrl;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
        }

    }

    public void sendOtherApp(Context context, String currentTitle, String currentUrl) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, currentTitle);
        intent.putExtra(Intent.EXTRA_TEXT, currentUrl);

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void pauseWebView() {
        webView.onPause();
    }

    public void zendeskCookieRemove(Context context) {
        CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeAllCookie();
        cookieSyncManager.sync();
    }
}

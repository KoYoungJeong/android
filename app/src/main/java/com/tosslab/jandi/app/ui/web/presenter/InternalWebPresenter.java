package com.tosslab.jandi.app.ui.web.presenter;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.share.ShareSelectRoomEvent;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.client.MessageManipulator_;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;

import java.net.URISyntaxException;

import retrofit.RetrofitError;

/**
 * Created by tee on 16. 1. 13..
 */

@EBean
public class InternalWebPresenter {

    @SystemService
    ClipboardManager clipboardManager;

    private View view;

    public void setView(View view) {
        this.view = view;
    }

    public void zendeskCookieRemove(Context context) {
        CookieSyncManager cookieSyncManager = CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeAllCookie();
        cookieSyncManager.sync();
    }

    public String createMessage(String title, String url) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(title).append("\n").append(url);
        return buffer.toString();
    }

    public void copyToClipboard(String contentString) {
        final ClipData clipData = ClipData.newPlainText("", contentString);
        clipboardManager.setPrimaryClip(clipData);
    }

    public void loadWebPage(WebView webView, String url) {
        WebSettings webSettings = webView.getSettings();
        webView.setWebViewClient(initWebClient());
        webView.setWebChromeClient(initWebChromeClient());
        webView.setScrollBarStyle(android.view.View.SCROLLBARS_INSIDE_OVERLAY);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setUserAgentString(webSettings.getUserAgentString() + " Jandi-Android-App");
        webView.loadUrl(url);
    }

    private WebChromeClient initWebChromeClient() {
        return new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                InternalWebPresenter.this.view.setActionBarTitle(title);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                InternalWebPresenter.this.view.setWebLoadingProgress(newProgress);
            }
        };
    }

    private WebViewClient initWebClient() {
        return new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (!url.startsWith("http")) {
                    if (url.startsWith("intent")) {
                        Intent intent;
                        try {
                            intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                            return false;
                        }
                        return InternalWebPresenter.this.view.launchNewBrowser(intent.getDataString());
                    } else {
                        return InternalWebPresenter.this.view.launchNewBrowser(url);
                    }
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                String title = view.getTitle();
                InternalWebPresenter.this.view.setActionBarTitle(title);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                InternalWebPresenter.this.view.LaunchPageNotFoundActivity();
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                InternalWebPresenter.this.view.LaunchPageNotFoundActivity();
            }
        };
    }

    public void sendMessageToRoom(int entityId, int entityType, String text, Activity activity) throws RetrofitError {
        MessageManipulator messageManipulator = MessageManipulator_.getInstance_(activity);
        messageManipulator.initEntity(entityType, entityId);
        messageManipulator.sendMessage(text, null);
    }

    public String getAvailableUrl(String url) {
        String urlLowerCase = url.toLowerCase();

        if (!urlLowerCase.startsWith("http")) {
            url = "http://" + url;
        }

        return url;
    }

    @Background
    public void sendMessage(Activity activity, String title, String Url, ShareSelectRoomEvent event) {
        Context context = JandiApplication.getContext();
        view.showProgressWheel();
        int entityId = event.getRoomId();
        int entityType = event.getRoomType();
        try {
            String message = createMessage(title, Url);
            sendMessageToRoom(entityId, entityType, message, activity);
            view.showSuccessToast(context, context.getString(R.string.jandi_share_succeed,
                    context.getString(R.string.jandi_message_hint)));
        } catch (RetrofitError e) {
            e.printStackTrace();
            view.showErrorToast(context, context.getString(R.string.err_network));
        } finally {
            view.dismissProgressWheel();
        }
    }

    public interface View {
        void setActionBarTitle(String title);

        void showSuccessToast(Context context, String message);

        void showErrorToast(Context context, String message);

        void LaunchPageNotFoundActivity();

        void setWebLoadingProgress(int newProgress);

        boolean launchNewBrowser(String url);

        void showProgressWheel();

        void dismissProgressWheel();
    }

}

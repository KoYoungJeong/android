package com.tosslab.jandi.app.ui.web.web_;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.web.PageNotFoundActivity_;
import com.tosslab.jandi.app.views.listeners.WebLoadingBar;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.net.URISyntaxException;

/**
 * Created by tee on 16. 1. 13..
 */

@EActivity(R.layout.activity_internal_web)
@OptionsMenu(R.menu.internal_web)
public class InternalWebActivity extends BaseAppCompatActivity {

    @Extra
    String url;
    @Extra
    boolean hideActionBar;
    @Extra
    boolean helpSite;

    @ViewById(R.id.web_internal_web)
    WebView webView;

    @ViewById(R.id.loading_internal_web)
    WebLoadingBar webLoadingBar;

    private WebChromeClient webCromeClient;

    @AfterInject
    void initObject() {
        webView.setWebViewClient(initWebClient());
        webView.setWebChromeClient(initWebChromeClient());
    }

    @NonNull
    private WebChromeClient initWebChromeClient() {
        return new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                ActionBar supportActionBar = getSupportActionBar();
                if (supportActionBar != null) {
                    supportActionBar.setTitle(title);
                }
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                // ProgressWheel
            }
        };
    }

    @NonNull
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
                        try {
                            if (intent != null) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(intent.getDataString())));
                                return true;
                            }
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                            return false;
                        }
                    } else {
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                            return true;
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                            return false;
                        }
                    }
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                String title = view.getTitle();
                ActionBar actionBar = getSupportActionBar();

                if (actionBar != null) {
                    actionBar.setTitle(title);
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                goPageNotFound();
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                goPageNotFound();
            }

        };
    }

    protected void goPageNotFound() {
        PageNotFoundActivity_.intent(InternalWebActivity.this)
                .flags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .url(url)
                .start();
        finish();
        overridePendingTransition(0, 0);
    }

}

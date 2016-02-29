package com.tosslab.jandi.app.ui.web;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.share.ShareSelectRoomEvent;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.share.views.ShareSelectRoomActivity_;
import com.tosslab.jandi.app.ui.web.presenter.InternalWebPresenter;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.progresswheel.ProgressWheelUtil;
import com.tosslab.jandi.app.views.listeners.WebLoadingBar;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import de.greenrobot.event.EventBus;

/**
 * Created by tee on 16. 1. 13..
 */

@EActivity(R.layout.activity_internal_web)
@OptionsMenu(R.menu.internal_web)
public class InternalWebActivity extends BaseAppCompatActivity implements InternalWebPresenter.View {

    public static final int REQ_PAGE_ERROR = 0x00;
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
    ProgressWheelUtil progressWheelUtil;
    @Bean
    InternalWebPresenter internalWebPresenter;
    private String webTitle = null;

    @AfterInject
    void initObject() {
        progressWheelUtil = ProgressWheelUtil.makeInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @AfterViews
    void initView() {
        setUpActionBar();
        internalWebPresenter.setView(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        if (helpSite) {
            AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.Help);
        }

        url = internalWebPresenter.getAvailableUrl(url);
        loadWebPage(webView, url);
        webTitle = webView.getTitle();
    }

    @Override
    public void setActionBarTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    @Override
    public void LaunchPageNotFoundActivity() {
        PageNotFoundActivity_.intent(InternalWebActivity.this)
                .flags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                .startForResult(REQ_PAGE_ERROR);
        overridePendingTransition(0, 0);
    }

    @Override
    public void setWebLoadingProgress(int newProgress) {
        if (newProgress > 0) {
            webLoadingBar.setProgress(newProgress);
            int visibility = webLoadingBar.getVisibility();
            setVisibilityState(visibility, newProgress);
        }
    }

    private void setVisibilityState(int visibility, int newProgress) {
        if (visibility != View.GONE && newProgress >= 100) {
            webLoadingBar.setVisibility(View.GONE);
        } else if (visibility != View.VISIBLE && newProgress < 100) {
            webLoadingBar.setVisibility(View.VISIBLE);
        }
    }

    @OptionsItem(R.id.action_share_to_topic)
    public void onShareToTopicOptionSelect() {
        long teamId = EntityManager.getInstance().getTeamId();
        ShareSelectRoomActivity_
                .intent(this)
                .teamId(teamId)
                .start();
    }

    @OptionsItem(R.id.action_copy_link)
    public void onCopyLinkOptionSelect() {
        String message = internalWebPresenter.createMessage(webTitle, url);
        internalWebPresenter.copyToClipboard(message);
    }

    @OptionsItem(R.id.action_open_to_browser)
    public void onOpenBrowserOptionSelect() {
        String newUrl;
        if (!webView.getUrl().startsWith("http://") && !webView.getUrl().startsWith("https://")) {
            newUrl = "http://" + url;
        } else {
            newUrl = webView.getUrl();
        }
        launchNewBrowser(newUrl);
    }

    @Override
    public boolean launchNewBrowser(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        try {
            startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    @OptionsItem(R.id.action_share_to_app)
    public void onShareToAppOptionSelect() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, webTitle);
        intent.putExtra(Intent.EXTRA_TEXT, url);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    @OptionsItem(android.R.id.home)
    void onHomeOptionSelect() {
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.origin_activity_close_enter, R.anim.origin_activity_close_exit);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }

    @UiThread
    @Override
    public void showSuccessToast(Context context, String message) {
        ColoredToast.show(message);
    }

    @UiThread
    @Override
    public void showErrorToast(Context context, String message) {
        ColoredToast.showError(message);
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onDestroy() {
        if (helpSite) {
            internalWebPresenter.zendeskCookieRemove(getApplicationContext());
        }
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void setUpActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
            actionBar.setHomeAsUpIndicator(R.drawable.actionbar_icon_remove);
            actionBar.setIcon(new ColorDrawable(Color.TRANSPARENT));
            if (hideActionBar) {
                actionBar.hide();
            }
        }
    }

    public void onEvent(ShareSelectRoomEvent event) {
        internalWebPresenter.sendMessage(this, webTitle, url, event);
    }


    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            webView.loadUrl("about:blank");
            super.onBackPressed();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showProgressWheel() {
        progressWheelUtil.showProgressWheel(this);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void dismissProgressWheel() {
        progressWheelUtil.dismissProgressWheel(this);
    }

    @Override
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
                setActionBarTitle(title);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                setWebLoadingProgress(newProgress);
            }
        };
    }

    private WebViewClient initWebClient() {
        return new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return internalWebPresenter.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                String title = view.getTitle();
                setActionBarTitle(title);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                LaunchPageNotFoundActivity();
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                LaunchPageNotFoundActivity();
            }
        };
    }

    @OnActivityResult(REQ_PAGE_ERROR)
    public void PageErrorResult(int resultCode) {
        switch (resultCode) {
            case PageNotFoundActivity.RES_FINISH:
                finish();
                break;
            case PageNotFoundActivity.RES_RETRY:
                webView.reload();
                break;
            case PageNotFoundActivity.RES_BACK:
                onBackPressed();
                break;
        }
    }

}

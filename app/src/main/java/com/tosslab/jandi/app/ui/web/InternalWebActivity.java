package com.tosslab.jandi.app.ui.web;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.share.ShareSelectRoomEvent;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.search.filter.room.RoomFilterActivity;
import com.tosslab.jandi.app.ui.web.presenter.InternalWebPresenter;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.progresswheel.ProgressWheelUtil;
import com.tosslab.jandi.app.views.listeners.WebLoadingBar;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class InternalWebActivity extends BaseAppCompatActivity implements InternalWebPresenter.View {

    public static final int REQ_PAGE_ERROR = 0x00;
    public static final int REQ_SHARE = 0x01;

    @InjectExtra
    String url;
    @Nullable
    @InjectExtra
    boolean hideActionBar;
    @Nullable
    @InjectExtra
    boolean helpSite;
    @Nullable
    @InjectExtra
    boolean isAdminPage = false;
    @Nullable
    @InjectExtra
    boolean hasMenu = true;

    @Bind(R.id.web_internal_web)
    WebView webView;
    @Bind(R.id.loading_internal_web)
    WebLoadingBar webLoadingBar;
    ProgressWheelUtil progressWheelUtil;

    InternalWebPresenter internalWebPresenter;
    private String webTitle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internal_web);
        Dart.inject(this);
        ButterKnife.bind(this);

        initObject();
        initView();

        EventBus.getDefault().register(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (hasMenu) {
            getMenuInflater().inflate(R.menu.internal_web, menu);
        }
        return true;
    }

    void initObject() {
        progressWheelUtil = ProgressWheelUtil.makeInstance();
        internalWebPresenter = new InternalWebPresenter();
    }

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

        if (isAdminPage) {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setCookie(url, "_jd_.access_token=" + TokenUtil.getAccessToken());
            cookieManager.setCookie(url, "_jd_.refresh_token=" + TokenUtil.getRefreshToken());
            cookieManager.setCookie(url, "_jd_.token_type=bearer");
        }

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
        startActivityForResult(new Intent(InternalWebActivity.this, PageNotFoundActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION), REQ_PAGE_ERROR);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share_to_topic:
                onShareToTopicOptionSelect();
                break;
            case R.id.action_copy_link:
                onCopyLinkOptionSelect();
                break;
            case R.id.action_open_to_browser:
                onOpenBrowserOptionSelect();
                break;
            case R.id.action_share_to_app:
                onShareToAppOptionSelect();
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onShareToTopicOptionSelect() {
        RoomFilterActivity.startForResultWithTopicId(this, -1, REQ_SHARE);
    }

    public void onCopyLinkOptionSelect() {
        String message = internalWebPresenter.createMessage(webTitle, url);
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        final ClipData clipData = ClipData.newPlainText("", message);
        clipboardManager.setPrimaryClip(clipData);
    }

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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.origin_activity_close_enter, R.anim.origin_activity_close_exit);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }

    @Override
    public void showSuccessToast(Context context, String message) {
        ColoredToast.show(message);
    }

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

    public void onEventMainThread(ShareSelectRoomEvent event) {
        internalWebPresenter.sendMessage(this, webTitle, url, event);
    }


    @Override
    public void onBackPressed() {
        if (isAdminPage) {
            if (webView.getOriginalUrl().equals(url)) {
                webView.loadUrl("about:blank");
                super.onBackPressed();
                return;
            }
        }

        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            webView.loadUrl("about:blank");
            super.onBackPressed();
        }
    }

    @Override
    public void showProgressWheel() {
        progressWheelUtil.showProgressWheel(this);
    }

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
                if (!isAdminPage) {
                    setActionBarTitle(title);
                } else {
                    setActionBarTitle("관리자 메뉴");
                }
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
                if (!isAdminPage) {
                    String title = view.getTitle();
                    setActionBarTitle(title);
                }
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                LaunchPageNotFoundActivity();
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_PAGE_ERROR) {
            onPageErrorResult(resultCode);
        } else if (requestCode == REQ_SHARE) {
            if (resultCode != RESULT_OK) {
                return;
            }

            boolean isTopic = data.getBooleanExtra(RoomFilterActivity.KEY_IS_TOPIC, false);

            long roomId = -1;
            if (isTopic) {
                roomId = data.getLongExtra(RoomFilterActivity.KEY_FILTERED_ROOM_ID, -1);
            } else {
                roomId = data.getLongExtra(RoomFilterActivity.KEY_FILTERED_MEMBER_ID, -1);
            }
            ShareSelectRoomEvent event = new ShareSelectRoomEvent();
            event.setRoomId(roomId);
            EventBus.getDefault().post(event);
        }
    }

    public void onPageErrorResult(int resultCode) {
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

package com.tosslab.jandi.app.ui.web;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.ShareEntityEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntitySimpleListAdapter;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.views.listeners.WebLoadingBar;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 2. 24..
 */
@EBean
public class InternalWebPresenter {

    @RootContext
    Context context;

    @ViewById(R.id.web_internal_web)
    WebView webView;

    @ViewById(R.id.loading_internal_web)
    WebLoadingBar webLoadingBar;

    private WebViewClient webViewClient;
    private WebChromeClient webCromeClient;
    private String url;
    private ProgressWheel progressWheel;

    @AfterInject
    void initObject() {
        progressWheel = new ProgressWheel(context);
        progressWheel.init();
    }

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
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
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

    public boolean hasBackHistory() {
        return webView.canGoBack();
    }

    public void moveBack() {
        webView.goBack();
    }

    public String getCurrentUrl() {
        return webView.getUrl();
    }

    public void showShareEntity(List<FormattedEntity> entities, String text) {
        /**
         * CDP 리스트 Dialog 를 보여준 뒤, 선택된 CDP에 Share
         */
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);

        EntitySimpleListAdapter adapter = new EntitySimpleListAdapter(context, entities);

        dialog.setTitle(R.string.jandi_title_cdp_to_be_shared)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FormattedEntity item = ((EntitySimpleListAdapter) ((AlertDialog) dialog).getListView().getAdapter()).getItem(which);
                        int entityId = item.getId();
                        int entityType;
                        if (item.isPublicTopic()) {
                            entityType = JandiConstants.TYPE_PUBLIC_TOPIC;
                        } else if (item.isPrivateGroup()) {
                            entityType = JandiConstants.TYPE_PRIVATE_TOPIC;
                        } else {
                            entityType = JandiConstants.TYPE_DIRECT_MESSAGE;
                        }
                        EventBus.getDefault().post(new ShareEntityEvent(entityId, entityType, text));
                    }
                })
                .create().show();

    }

    public String getCurrentTitle() {
        return webView.getTitle();
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
    public void showSuccessToast(String message) {
        ColoredToast.show(context, message);
    }

    @UiThread
    public void showErrorToast(String message) {
        ColoredToast.showError(context, message);
    }

    public void moveOtherBrowser(String currentUrl) {

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

    public void sendOtherApp(String currentTitle, String currentUrl) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plan");
        intent.putExtra(Intent.EXTRA_SUBJECT, currentTitle);
        intent.putExtra(Intent.EXTRA_TEXT, currentUrl);

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
        }
    }
}

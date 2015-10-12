package com.tosslab.jandi.app.ui.web;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.ShareEntityEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.web.model.InternalWebModel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import java.net.URISyntaxException;
import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

/**
 * Created by Steve SeongUg Jung on 15. 2. 24..
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

    @Bean
    InternalWebPresenter internalWebPresenter;

    @Bean
    InternalWebModel internalWebModel;

    @AfterInject
    void initObject() {
        internalWebPresenter.initObject(this);
        internalWebPresenter.setWebViewClient(new WebViewClient() {
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
        });

        internalWebPresenter.setWebCromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                ActionBar supportActionBar = getSupportActionBar();
                if (supportActionBar != null) {
                    supportActionBar.setTitle(title);
                }
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                internalWebPresenter.setProgress(newProgress);
            }
        });


        String urlLowerCase = url.toLowerCase();
        if (!urlLowerCase.startsWith("http")) {
            url = "http://" + urlLowerCase;
        }

        internalWebPresenter.setUrl(url);
    }

    @AfterViews
    void initView() {
        setUpActionBar();
        if (helpSite) {
            AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.Help);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        internalWebPresenter.pauseWebView();
        EventBus.getDefault().unregister(this);
        super.onPause();

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

    @Override
    public void onBackPressed() {
        if (internalWebPresenter.hasBackHistory()) {
            internalWebPresenter.moveBack();
        } else {
            internalWebPresenter.loadUrl("about:blank");
            super.onBackPressed();
        }
    }

    @OptionsItem(R.id.action_share_to_topic)
    public void onShareToTopicOptionSelect() {

        List<FormattedEntity> entities = internalWebModel.getEntities();

        String title = internalWebPresenter.getCurrentTitle();
        String url = internalWebPresenter.getCurrentUrl();

        String message = internalWebModel.createMessage(title, url);

        internalWebPresenter.showShareEntity(this, entities, message);
    }

    public void onEvent(ShareEntityEvent shareEntityEvent) {

        shareMessage(shareEntityEvent);

    }

    @Background
    void shareMessage(ShareEntityEvent shareEntityEvent) {
        internalWebPresenter.showProgressWheel();
        int entityId = shareEntityEvent.getEntityId();
        int entityType = shareEntityEvent.getEntityType();
        String text = shareEntityEvent.getText();
        try {
            internalWebModel.sendMessage(entityId, entityType, text);
            internalWebPresenter.showSuccessToast(getApplicationContext(), getString(R.string.jandi_share_succeed, getString(R.string.jandi_message_hint)));
        } catch (RetrofitError e) {
            e.printStackTrace();
            internalWebPresenter.showErrorToast(getApplicationContext(), getString(R.string.err_network));
        } finally {
            internalWebPresenter.dismissProgressWheel();
        }
    }

    @OptionsItem(R.id.action_copy_link)
    public void onCopyLinkOptionSelect() {

        String message = internalWebModel.createMessage(
                internalWebPresenter.getCurrentTitle(), internalWebPresenter.getCurrentUrl());

        internalWebModel.copyToClipboard(message);
    }

    @OptionsItem(R.id.action_open_to_browser)
    public void onOpenBrowserOptionSelect() {
        internalWebPresenter.moveOtherBrowser(InternalWebActivity.this, internalWebPresenter.getCurrentUrl());
    }

    @OptionsItem(R.id.action_share_to_app)
    public void onShareToAppOptionSelect() {
        internalWebPresenter.sendOtherApp(InternalWebActivity.this,
                internalWebPresenter.getCurrentTitle(), internalWebPresenter.getCurrentUrl());
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
    protected void onDestroy() {
        if (helpSite) {
            internalWebPresenter.zendeskCookieRemove(getApplicationContext());
        }
        super.onDestroy();
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }
}

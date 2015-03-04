package com.tosslab.jandi.app.ui.web;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.ShareEntityEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.ui.web.model.InternalWebModel;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 2. 24..
 */
@EActivity(R.layout.activity_internal_web)
@OptionsMenu(R.menu.internal_web)
public class InternalWebActivity extends ActionBarActivity {

    @Extra
    String url;

    @Extra
    boolean hasActionBar = true;

    @Bean
    InternalWebPresenter internalWebPresenter;

    @Bean
    InternalWebModel internalWebModel;

    @AfterInject
    void initObject() {

        internalWebPresenter.setWebViewClient(new WebViewClient());

        internalWebPresenter.setWebCromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                getSupportActionBar().setTitle(title);
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                internalWebPresenter.setProgress(newProgress);
            }
        });

        internalWebPresenter.setUrl(url);
    }

    @AfterViews
    void initView() {
        setUpActionBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    private void setUpActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.jandi_actionb_remove);
        actionBar.setIcon(new ColorDrawable(Color.TRANSPARENT));

        if (!hasActionBar) {
            actionBar.hide();
        }
    }

    @Override
    public void onBackPressed() {
        if (internalWebPresenter.hasBackHistory()) {
            internalWebPresenter.moveBack();
        } else {
            super.onBackPressed();
        }
    }

    @OptionsItem(R.id.action_share_to_topic)
    public void onShareToTopicOptionSelect() {

        List<FormattedEntity> entities = internalWebModel.getEntities();

        String title = internalWebPresenter.getCurrentTitle();
        String url = internalWebPresenter.getCurrentUrl();

        String message = internalWebModel.createMessage(title, url);

        internalWebPresenter.showShareEntity(entities, message);

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
            internalWebPresenter.showSuccessToast(getString(R.string.jandi_share_succeed, getString(R.string.jandi_message_hint)));
        } catch (JandiNetworkException e) {
            internalWebPresenter.showErrorToast(getString(R.string.err_network));
        } finally {
            internalWebPresenter.dismissProgressWheel();
        }
    }

    @OptionsItem(R.id.action_copy_link)
    public void onCopyLinkOptionSelect() {

        String message = internalWebModel.createMessage(internalWebPresenter.getCurrentTitle(), internalWebPresenter.getCurrentUrl());

        internalWebModel.copyToClipboard(message);
    }

    @OptionsItem(R.id.action_open_to_browser)
    public void onOpenBrowserOptionSelect() {
        internalWebPresenter.moveOtherBrowser(internalWebPresenter.getCurrentUrl());
    }

    @OptionsItem(R.id.action_share_to_app)
    public void onShareToAppOptionSelect() {
        internalWebPresenter.sendOtherApp(internalWebPresenter.getCurrentTitle(), internalWebPresenter.getCurrentUrl());
    }

    @OptionsItem(android.R.id.home)
    void onHomeOptionSelect() {
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.ready, R.anim.abc_slide_out_bottom);
    }
}

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

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;

/**
 * Created by Steve SeongUg Jung on 15. 2. 24..
 */
@EActivity(R.layout.activity_internal_web)
public class InternalWebActivity extends ActionBarActivity {

    @Extra
    String url;

    @Bean
    InternalWebPresenter internalWebPresenter;

    @AfterInject
    void initObject() {

        internalWebPresenter.setWebViewClient(new WebViewClient());

        internalWebPresenter.setWebCromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                internalWebPresenter.setProgress(newProgress);
            }
        });

        internalWebPresenter.setUrl(url);
    }

    @AfterViews
    void initView() {
        setUpActionBar();
    }

    private void setUpActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.jandi_actionb_remove);
        actionBar.setIcon(new ColorDrawable(Color.TRANSPARENT));
    }

    @OptionsItem(android.R.id.home)
    void onHomeOptionSelect() {
        finish();
    }
}

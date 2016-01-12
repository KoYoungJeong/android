package com.tosslab.jandi.app.ui.web;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;

/**
 * Created by tee on 16. 1. 12..
 */

@EActivity(R.layout.activity_web_not_found_page)
public class PageNotFoundActivity extends BaseAppCompatActivity {

    @Extra
    String url;

    @AfterViews
    void initViews() {
        setUpActionbar();
    }

    @Click(R.id.bt_retry)
    public void retryButtonClicked() {
        InternalWebActivity_.intent(this)
                .flags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .url(url)
                .start();
        finish();
        overridePendingTransition(0, 0);
    }

    @OptionsItem(android.R.id.home)
    void onHomeOptionSelect() {
        finish();
    }

    private void setUpActionbar() {
        AppCompatActivity activity = this;
        if (activity.getSupportActionBar() == null) {
            Toolbar toolbar = (Toolbar) activity.findViewById(R.id.layout_search_bar);
            activity.setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        }

        ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }

}

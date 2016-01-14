package com.tosslab.jandi.app.ui.web;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;

/**
 * Created by tee on 16. 1. 12..
 */

@EActivity(R.layout.activity_web_not_found_page)
public class PageNotFoundActivity extends BaseAppCompatActivity {

    public static final int RES_FINISH = 0x00;
    public static final int RES_RETRY = 0x01;
    public static final int RES_BACK = 0X02;

    @AfterViews
    void initViews() {
        setUpActionbar();
    }

    @Click(R.id.bt_retry)
    public void retryButtonClicked() {
        setResult(RES_RETRY);
        finish();
        overridePendingTransition(0, 0);
    }

    @OptionsItem(android.R.id.home)
    void onHomeOptionSelect() {
        setResult(RES_FINISH);
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RES_BACK);
        finish();
        overridePendingTransition(0, 0);
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

package com.tosslab.jandi.app.ui.web;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class PageNotFoundActivity extends BaseAppCompatActivity {

    public static final int RES_FINISH = 0x00;
    public static final int RES_RETRY = 0x01;
    public static final int RES_BACK = 0X02;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_not_found_page);
        ButterKnife.bind(this);
        initViews();
    }

    void initViews() {
        setUpActionbar();
    }

    @OnClick(R.id.bt_retry)
    public void retryButtonClicked() {
        setResult(RES_RETRY);
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onHomeOptionSelect();
        }
        return super.onOptionsItemSelected(item);
    }

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

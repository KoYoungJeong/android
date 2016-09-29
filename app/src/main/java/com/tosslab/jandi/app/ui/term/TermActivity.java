package com.tosslab.jandi.app.ui.term;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.webkit.WebView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;

import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

public class TermActivity extends BaseAppCompatActivity {

    public static final String EXTRA_TERM_MODE = "term_mode";

    @Bind(R.id.web_term)
    WebView webView;

    private String termMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_term);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        termMode = intent.getStringExtra(EXTRA_TERM_MODE);

        initView();
    }

    void initView() {
        Mode mode = Mode.valueOf(termMode);

        setActionbarSetting(mode);

        String langCode = getLanguageCode();
        String url = getUrl(mode, langCode);

        webView.loadUrl(url);
    }

    private void setActionbarSetting(Mode mode) {

        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);

        // Set up the action bar.
        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        switch (mode) {
            case Privacy:
                actionBar.setTitle(getString(R.string.jandi_pp));
                break;
            case Agreement:
                actionBar.setTitle(getString(R.string.jandi_term_of_service));
                break;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private String getUrl(Mode mode, String langCode) {

        StringBuffer buffer = new StringBuffer();
        buffer.append("file:///android_asset/html/term/");

        switch (mode) {
            case Privacy:
                buffer.append("privacy_");
                break;
            case Agreement:
                buffer.append("agreement_");
                break;
        }

        buffer.append(langCode).append(".html");

        return buffer.toString();
    }

    public String getLanguageCode() {
        Locale locale = getResources().getConfiguration().locale;
        String langCode = locale.getLanguage().toLowerCase();

        if (TextUtils.equals(langCode, "ko")) {
            // if Korean, then return "kr"
            langCode = "kr";
        } else if (!TextUtils.equals(langCode, "jp")) {
            // if not Japanese, then return "en"
            langCode = "en";
        }

        return langCode;
    }

    public enum Mode {
        Privacy, Agreement
    }
}

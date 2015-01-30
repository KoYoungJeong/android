package com.tosslab.jandi.app.ui.term;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.webkit.WebView;

import com.tosslab.jandi.app.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import java.util.Locale;

/**
 * Created by Steve SeongUg Jung on 14. 12. 30..
 */
@EActivity(R.layout.activity_term)
public class TermActivity extends Activity {

    @ViewById(R.id.web_term)
    WebView webView;
    @Extra
    String termMode;

    @AfterViews
    void initView() {
        Mode mode = Mode.valueOf(termMode);

        setActionbarSetting(mode);

        String langCode = getLanguageCode();
        String url = getUrl(mode, langCode);

        webView.loadUrl(url);
    }

    private void setActionbarSetting(Mode mode) {
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        switch (mode) {
            case Privacy:
                actionBar.setTitle(getString(R.string.jandi_privacy_policy));
                break;
            case Agreement:
                actionBar.setTitle(getString(R.string.jandi_term_of_service));
                break;
        }

    }

    @OptionsItem(android.R.id.home)
    void onGoHome() {
        finish();
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

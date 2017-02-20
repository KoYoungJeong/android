package com.tosslab.jandi.app.ui.sign.signin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by tee on 2017. 2. 14..
 */

public class CaptchaActivity extends BaseAppCompatActivity {

    @Bind(R.id.web_captcha)
    WebView webView;

    @Bind(R.id.v_progress)
    ProgressBar progressBar;

    @Bind(R.id.layout_search_bar)
    Toolbar toolbar;

    @Bind(R.id.tv_next_button)
    TextView tvNextButton;

    private String captchaMessage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);

        setContentView(R.layout.activity_captcha);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        loadWebPage();
    }

    private void loadWebPage() {
        webView.getSettings().setJavaScriptEnabled(true);

        Locale locale = getResources().getConfiguration().locale;
        String lang = locale.getLanguage();

        String url = "https://www.jandi.io/landing/kr/mobile/recaptcha/android";

        if (TextUtils.equals(lang, "en")) {
            url = "https://www.jandi.io/landing/en/mobile/recaptcha/android";
        } else if (TextUtils.equals(lang, "ja")) {
            url = "https://www.jandi.io/landing/ja/mobile/recaptcha/android";
        } else if (TextUtils.equals(lang, "ko")) {
            url = "https://www.jandi.io/landing/kr/mobile/recaptcha/android";
        } else if (TextUtils.equals(lang, "zh-cn")) {
            url = "https://www.jandi.io/landing/zh-cn/mobile/recaptcha/android";
        } else if (TextUtils.equals(lang, "zh-tw")) {
            url = "https://www.jandi.io/landing/zh-tw/mobile/recaptcha/android";
        }

        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setOnTouchListener((v, event) -> (event.getAction() == MotionEvent.ACTION_MOVE));
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            public void onPageStarted(WebView view, String url,
                                      android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(android.view.View.VISIBLE);
            }

            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(android.view.View.INVISIBLE);
            }

            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                Toast.makeText(CaptchaActivity.this, "error!:" + description,
                        Toast.LENGTH_SHORT).show();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                tvNextButton.setEnabled(true);
                captchaMessage = message;
                result.confirm();
                return true;
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                LogUtil.e(newProgress + "");
                progressBar.setProgress(newProgress);
            }
        });

        webView.loadUrl(url);
    }

    @OnClick(R.id.tv_next_button)
    void onClickNextButton() {
        if (captchaMessage != null) {
            Intent intent = new Intent();
            intent.putExtra(SignInActivity.RESULT_CAPTCHAR, captchaMessage);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}

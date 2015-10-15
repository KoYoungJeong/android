package com.tosslab.jandi.app.ui.signup.verify;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.account.AccountHomeActivity_;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.signup.verify.presenter.SignUpVerifyPresenter;
import com.tosslab.jandi.app.ui.signup.verify.view.SignUpVerifyView;
import com.tosslab.jandi.app.ui.signup.verify.widget.VerificationCodeView;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.constant.property.ScreenViewProperty;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

/**
 * Created by tonyjs on 15. 5. 19..
 */
@EActivity(R.layout.activity_signup_verify)
public class SignUpVerifyActivity extends BaseAppCompatActivity implements SignUpVerifyView {

    @Extra("email")
    String email;

    @Bean
    SignUpVerifyPresenter presenter;

    @ViewById(R.id.vg_verification_code)
    VerificationCodeView verificationCodeView;

    @ViewById(R.id.btn_verify)
    Button btnVerify;

    @ViewById(R.id.tv_resend_email)
    TextView tvResendEmail;

    @ViewById(R.id.vg_invalidate_code)
    FrameLayout vgInvalidateCode;

    @ViewById(R.id.tv_invalidate_code)
    TextView tvInvalidateCode;
    @SystemService
    InputMethodManager inputMethodManager;
    private ProgressWheel progressWheel;

    @AfterViews
    void init() {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.ScreenView)
                        .property(PropertyKey.ScreenView, ScreenViewProperty.CONFIRM_VERIFICATION_NUMBER)
                        .build());

        setUpActionBar();

        String resendEmailText = getString(R.string.jandi_signup_resend_email);
        tvResendEmail.setText(Html.fromHtml(resendEmailText));

        presenter.setView(this);
        progressWheel = new ProgressWheel(this);
        verificationCodeView.setOnVerificationCodeChangedListener(() ->
                presenter.validateVerificationCode(verificationCodeView.getVerificationCode()));
        verificationCodeView.setOnActionDoneListener(this::hideKeyboard);

        AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.CodeVerification);
    }

    @Override
    protected void onStop() {
        hideKeyboard();
        super.onStop();
    }

    private void setUpActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        actionBar.setTitle(getString(R.string.jandi_signup_verify_title));
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setVerifyButtonEnabled(boolean valid) {
        btnVerify.setEnabled(valid);
        if (valid) {
            hideKeyboard();
        }
    }

    @UiThread
    @Override
    public void showProgress() {
        if (progressWheel == null || progressWheel.isShowing()) {
            return;
        }
        progressWheel.show();
    }

    @UiThread
    @Override
    public void hideProgress() {
        if (progressWheel == null || !progressWheel.isShowing()) {
            return;
        }
        progressWheel.dismiss();
    }

    @UiThread
    @Override
    public void showInvalidVerificationCode(int count) {
        String invalidateText = getString(R.string.jandi_signup_invalidate_code);
        invalidateText = String.format(invalidateText, count);
        tvInvalidateCode.setText(invalidateText);
        vgInvalidateCode.animate()
                .alpha(1.0f)
                .setDuration(300);
    }

    @UiThread
    @Override
    public void hideInvalidVerificationCode() {
        tvInvalidateCode.setText("");
        vgInvalidateCode.animate()
                .alpha(0.0f)
                .setDuration(300);
    }

    @UiThread
    @Override
    public void clearVerificationCode() {
        setValidateTextColor();
        verificationCodeView.clearAll();
    }

    @UiThread
    @Override
    public void setInvalidateTextColor() {
        verificationCodeView.setTextColorInvalidate();
    }

    @UiThread
    @Override
    public void setValidateTextColor() {
        verificationCodeView.setTextColorValidate();
    }

    @UiThread
    @Override
    public void hideResend() {
        tvResendEmail.setVisibility(View.INVISIBLE);
    }

    @Override
    public void hideKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(
                verificationCodeView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @UiThread
    @Override
    public void showResend() {
        tvResendEmail.setVisibility(View.VISIBLE);
    }

    @UiThread
    @Override
    public void showExpiredVerificationCode() {
        new AlertDialog.Builder(SignUpVerifyActivity.this,
                R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setMessage(getString(R.string.jandi_signup_expired_verification_code))
                .setPositiveButton(getString(R.string.jandi_confirm), (dialog, which) -> {
                    presenter.requestNewVerificationCode(email);
                })
                .setNegativeButton(getString(R.string.jandi_cancel), null)
                .create()
                .show();
    }

    @UiThread
    @Override
    public void showToast(String msg) {
        ColoredToast.show(this, msg);
    }

    @UiThread
    @Override
    public void showErrorToast(String msg) {
        ColoredToast.showError(this, msg);
    }

    @UiThread
    @Override
    public void moveToAccountHome() {
        AccountHomeActivity_.intent(this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_NEW_TASK)
                .start();
        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        finish();
    }

    @Click(R.id.btn_verify)
    void verify(View v) {
        if (!v.isEnabled()) {
            return;
        }
        presenter.verifyCode(email, verificationCodeView.getVerificationCode());

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.CodeVerification, AnalyticsValue.Action.LaunchJandi);
    }

    @Click(R.id.tv_resend_email)
    void resendEmail() {
        presenter.requestNewVerificationCode(email);

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.CodeVerification, AnalyticsValue.Action.Resend);
    }

    @OptionsItem(android.R.id.home)
    @Override
    public void finish() {
        super.finish();
    }

}

package com.tosslab.jandi.app.ui.signup.verify;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.mixpanel.MixpanelAccountAnalyticsClient;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResAccountActivate;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.account.AccountHomeActivity_;
import com.tosslab.jandi.app.ui.signup.verify.presenter.SignUpVerifyPresenter;
import com.tosslab.jandi.app.ui.signup.verify.view.SignUpVerifyView;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

/**
 * Created by tonyjs on 15. 5. 19..
 */
@EActivity(R.layout.activity_signup_verify)
public class SignUpVerifyActivity extends AppCompatActivity
        implements SignUpVerifyView, VerificationCodeView.OnVerificationCodeChangeListener {

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

    private ProgressWheel progressWheel;

    @SystemService
    InputMethodManager inputMethodManager;

    @AfterViews
    void init() {
        setUpActionBar();

        presenter.setView(this);
        progressWheel = new ProgressWheel(this);
        progressWheel.init();
        verificationCodeView.setListener(this);

        String resendEmailText = getString(R.string.jandi_signup_resend_email);
        tvResendEmail.setText(Html.fromHtml(resendEmailText));
    }

    @Override
    protected void onStop() {
        verificationCodeView.hideKeyboard();
        super.onStop();
    }

    private void setUpActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getString(R.string.jandi_signup_verify_title));
    }

    @Override
    public String getVerificationCode() {
        return verificationCodeView.getVerificationCode();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setVerifyButtonEnabled(boolean valid) {
        btnVerify.setEnabled(valid);
        if (valid) {
            verificationCodeView.hideKeyboard();
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

    @UiThread
    @Override
    public void showResend() {
        tvResendEmail.setVisibility(View.VISIBLE);
    }

    @UiThread
    @Override
    public void showExpiredVerificationCode() {
        new AlertDialog.Builder(this)
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

    @Override
    public void onChanged() {
        presenter.validateVerificationCode();
    }

    @Click(R.id.btn_verify)
    void verify(View v) {
        if (!v.isEnabled()) {
            return;
        }
        presenter.verifyCode(email);
    }

    @Click(R.id.tv_resend_email)
    void resendEmail() {
        presenter.requestNewVerificationCode(email);
    }

    @OptionsItem(android.R.id.home)
    @Override
    public void finish() {
        super.finish();
    }
}

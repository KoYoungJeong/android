package com.tosslab.jandi.app.ui.signup.verify;

import android.support.v7.app.AppCompatActivity;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.signup.verify.presenter.SignUpVerifyPresenter;
import com.tosslab.jandi.app.ui.signup.verify.view.SignUpVerifyView;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
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

    private ProgressWheel progressWheel;

    @SystemService
    InputMethodManager inputMethodManager;

    @AfterViews
    void init() {
        presenter.setView(this);
        progressWheel = new ProgressWheel(this);
        progressWheel.init();
        verificationCodeView.setListener(this);
    }

    @Override
    public String getVerificationCode() {
        return verificationCodeView.getVerificationCode();
    }

    //    @AfterTextChange(R.id.et_verification_code)
    public void onTextChanged() {
//        presenter.validateVerificationCode();
    }

    @Override
    public void setVerifyButtonEnabled(boolean valid) {
        ColoredToast.show(this, "valid ? " + valid);
        btnVerify.setEnabled(valid);
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
        ColoredToast.show(this, String.format("%d번 실패하였소.", count));
    }

    @Override
    public void onChanged() {
        presenter.validateVerificationCode();
    }

    @Click(R.id.btn_verify)
    void verify() {
        presenter.verifyVerificationCode();
    }

//    @Click(R.id.vg_verification_code)
//    void onVerificationCodeClick() {
//        if (verificationCodeView.requestFocus()) {
//            inputMethodManager.showSoftInput(verificationCodeView, InputMethodManager.SHOW_FORCED);
//        }
//    }

}

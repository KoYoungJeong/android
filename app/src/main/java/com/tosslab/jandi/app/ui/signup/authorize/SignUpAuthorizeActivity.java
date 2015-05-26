package com.tosslab.jandi.app.ui.signup.authorize;

import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.signup.authorize.presenter.SignUpAuthorizePresenter;
import com.tosslab.jandi.app.ui.signup.authorize.view.SignUpAuthorizeView;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

/**
 * Created by tonyjs on 15. 5. 19..
 */
@EActivity(R.layout.activity_signup_verify)
public class SignUpAuthorizeActivity extends AppCompatActivity implements SignUpAuthorizeView {

    @Extra("email")
    String email;
    @Extra("id")
    String id;

    @Bean
    SignUpAuthorizePresenter presenter;

    @ViewById(R.id.et_authorization_code)
    EditText etAuthorizationCode;

    @ViewById(R.id.btn_authorize)
    Button btnAuthorize;

    private ProgressWheel progressWheel;

    @AfterViews
    void init() {
        presenter.setView(this);
        progressWheel = new ProgressWheel(this);
        progressWheel.init();
    }

    @Override
    public String getAuthorizationCode() {
        CharSequence authorizationCode = etAuthorizationCode.getText();
        if (!TextUtils.isEmpty(authorizationCode)) {
            return authorizationCode.toString();
        }
        return null;
    }

    @AfterTextChange(R.id.et_authorization_code)
    public void onTextChanged() {
        presenter.validateAuthorizationCode();
    }

    @Override
    public void setVerifyButtonEnabled(boolean valid) {
        btnAuthorize.setEnabled(valid);
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
    public void showInvalidAuthorizationCode(int count) {
        ColoredToast.show(this, String.format("%d번 실패하였소.", count));
    }

    @Click(R.id.btn_authorize)
    void authorize() {
        presenter.verifyAuthorizationCode();
    }

}

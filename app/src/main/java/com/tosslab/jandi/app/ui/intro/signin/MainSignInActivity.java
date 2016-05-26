package com.tosslab.jandi.app.ui.intro.signin;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.widget.EditText;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.intro.signin.dagger.DaggerMainSignInComponent;
import com.tosslab.jandi.app.ui.intro.signin.dagger.MainSignInModule;
import com.tosslab.jandi.app.ui.intro.signin.presenter.MainSignInPresenter;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;

/**
 * Created by tee on 16. 5. 25..
 */

public class MainSignInActivity extends BaseAppCompatActivity implements MainSignInPresenter.View {

    @Inject
    MainSignInPresenter mainSignInPresenter;

    @Bind(R.id.et_layout_email)
    TextInputLayout etLayoutEmail;

    @Bind(R.id.et_layout_password)
    TextInputLayout etLayoutPassword;

    @Bind(R.id.et_email)
    EditText etEmail;

    @Bind(R.id.et_password)
    EditText etPassword;

    boolean emailFocus = true;
    boolean passwordFocus = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jandi_sign_in);
        ButterKnife.bind(this);
        DaggerMainSignInComponent.builder()
                .mainSignInModule(new MainSignInModule(this))
                .build()
                .inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @OnFocusChange(R.id.et_email)
    void onEmailFocused(boolean focused) {
        if (focused) {

        }
    }

    @OnFocusChange(R.id.et_password)
    void onPasswordFocused(boolean focused) {
    }

    @OnClick(R.id.tv_sign_in_button)
    void onClickSignInButton() {

    }

    @Override
    public void showErrorInsertEmail() {

    }

    @Override
    public void showErrorInvalidEmail() {

    }

    @Override
    public void showErrorInsertPassword() {

    }

    @Override
    public void showErrorInvalidPassword() {

    }

    @Override
    public void showErrorInvalidEmailOrPassword() {

    }

    @Override
    public void showSignInButtonEnabled() {

    }

    @Override
    public void showSignInButtonDisabled() {

    }

    @Override
    public void moveToSignUp() {

    }

    @Override
    public void showNetworkErrorToast() {

    }

    @Override
    public void showProgressDialog() {

    }

    @Override
    public void dismissProgressDialog() {

    }

}
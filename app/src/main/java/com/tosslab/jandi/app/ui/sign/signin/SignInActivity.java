package com.tosslab.jandi.app.ui.sign.signin;

import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.events.profile.ForgotPasswordEvent;
import com.tosslab.jandi.app.ui.account.AccountHomeActivity_;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.sign.signin.dagger.DaggerSignInComponent;
import com.tosslab.jandi.app.ui.sign.signin.dagger.SignInModule;
import com.tosslab.jandi.app.ui.sign.signin.presenter.SignInPresenter;
import com.tosslab.jandi.app.ui.sign.signup.SignUpActivity;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import de.greenrobot.event.EventBus;

/**
 * Created by tee on 16. 5. 25..
 */

public class SignInActivity extends BaseAppCompatActivity implements SignInPresenter.View {

    @Inject
    SignInPresenter signInPresenter;

    @Bind(R.id.et_layout_email)
    TextInputLayout etLayoutEmail;

    @Bind(R.id.et_layout_password)
    TextInputLayout etLayoutPassword;

    @Bind(R.id.et_email)
    EditText etEmail;

    @Bind(R.id.et_password)
    EditText etPassword;

    @Bind(R.id.btn_sign_in)
    TextView tvSignInButton;

    @Bind(R.id.tv_error_id_or_password)
    TextView tvErrorIdOrPassword;

    ProgressWheel progressWheel;
    private boolean isFirstFocus = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        setNeedUnLockPassCode(false);
        setShouldReconnectSocketService(false);

        DaggerSignInComponent.builder()
                .signInModule(new SignInModule(this))
                .build()
                .inject(this);

        ButterKnife.bind(this);

        tvSignInButton.setEnabled(false);
        etEmail.addTextChangedListener(new EtTextWatcher());
        etPassword.addTextChangedListener(new EtTextWatcher());
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpActionBar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void setUpActionBar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);

        // Set up the action bar.
        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }


    @OnFocusChange(R.id.et_email)
    void onEmailFocused(boolean focused) {
        if (focused && !isFirstFocus) {
            signInPresenter.checkPasswordValidation(etPassword.getText().toString());
            removeErrorEmail();
            tvErrorIdOrPassword.setVisibility(View.GONE);
        }

        if (etLayoutEmail.isErrorEnabled()) {
            setMarginTopPasswordLayout(0);
        } else {
            setMarginTopPasswordLayout(8);
        }

        isFirstFocus = false;
    }

    @OnFocusChange(R.id.et_password)
    void onPasswordFocused(boolean focused) {
        if (focused) {
            signInPresenter.checkEmailValidation(etEmail.getText().toString());
            removeErrorPassword();
            tvErrorIdOrPassword.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.btn_sign_in)
    void onClickSignInButton() {
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SignIn, AnalyticsValue.Action.Submit);
        if (NetworkCheckUtil.isConnected()) {
            signInPresenter.trySignIn(etEmail.getText().toString(), etPassword.getText().toString());
        } else {
            showNetworkErrorToast();
        }
    }

    @OnClick(R.id.btn_sign_up)
    void onClickSignUpButton() {
        Intent intent = new Intent(this, SignUpActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @OnClick(R.id.tv_forget_password)
    void onClickForgetPasswordButton() {
        DialogFragment dialogFragment =
                EditTextDialogFragment.newInstance(
                        EditTextDialogFragment.ACTION_FORGOT_PASSWORD, etEmail.getText().toString());
        dialogFragment.show(getFragmentManager(), "dialog");
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SignIn, AnalyticsValue.Action.ForgotPW);
    }

    @Override
    public void showErrorInsertEmail() {
        if (!etLayoutEmail.isErrorEnabled()) {
            etLayoutEmail.setErrorEnabled(true);
        }
        etLayoutEmail.setError(getString(R.string.jandi_err_input_email));

    }

    @Override
    public void showErrorInvalidEmail() {
        if (!etLayoutEmail.isErrorEnabled()) {
            etLayoutEmail.setErrorEnabled(true);
        }
        etLayoutEmail.setError(getString(R.string.jandi_err_invalid_email));
    }

    @Override
    public void removeErrorEmail() {
        etLayoutEmail.setError("");
        etLayoutEmail.setErrorEnabled(false);
    }

    @Override
    public void showErrorInsertPassword() {
        if (!etLayoutPassword.isErrorEnabled()) {
            etLayoutPassword.setErrorEnabled(true);
        }
        etLayoutPassword.setError(getString(R.string.jandi_err_input_password));
    }

    @Override
    public void showErrorInvalidPassword() {
        if (!etLayoutPassword.isErrorEnabled()) {
            etLayoutPassword.setErrorEnabled(true);
        }
        etLayoutPassword.setError(getString(R.string.jandi_password_strength_too_short));
    }

    @Override
    public void removeErrorPassword() {
        etLayoutPassword.setError("");
        etLayoutPassword.setErrorEnabled(false);
    }

    @Override
    public void showErrorInvalidEmailOrPassword() {
        tvErrorIdOrPassword.setVisibility(View.VISIBLE);
    }

    private void setMarginTopPasswordLayout(float marginDip) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) etLayoutPassword.getLayoutParams();
        DisplayMetrics displayMetrics = JandiApplication.getContext().getResources().getDisplayMetrics();
        int marginTop = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginDip, displayMetrics));
        int marginSide = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, displayMetrics));
        params.setMargins(marginSide, marginTop, marginSide, 0);
        etLayoutPassword.setLayoutParams(params);
    }

    @Override
    public void showNetworkErrorToast() {
        ColoredToast.show(R.string.err_network);
    }

    @Override
    public void showFailPasswordResetToast() {
        ColoredToast.show(getString(R.string.jandi_fail_send_password_reset_email));
    }

    @Override
    public void showSuccessPasswordResetToast() {
        ColoredToast.show(getString(R.string.jandi_sent_password_reset_mail));
    }

    @Override
    public void showProgressDialog() {
        if (progressWheel == null) {
            progressWheel = new ProgressWheel(this);
        }
        progressWheel.show();
    }

    @Override
    public void dismissProgressDialog() {
        if (progressWheel != null) {
            progressWheel.dismiss();
        }
    }

    @Override
    public void moveToTeamSelectionActivity(String myEmailId) {
        AccountHomeActivity_.intent(this)
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .shouldRefreshAccountInfo(false)
                .start();
        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        finish();
    }

    public void onEvent(ForgotPasswordEvent event) {
        String email = event.getEmail();
        signInPresenter.forgotPassword(email);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class EtTextWatcher implements TextWatcher {

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            if (etEmail.getText().length() > 0 && etPassword.getText().length() > 0) {
                tvSignInButton.setEnabled(true);
            } else {
                tvSignInButton.setEnabled(false);
            }
            if (etEmail.isFocused() && etLayoutEmail.isErrorEnabled()) {
                removeErrorEmail();
            }
            if (etPassword.isFocused() && etLayoutPassword.isErrorEnabled()) {
                removeErrorPassword();
            }
            tvErrorIdOrPassword.setVisibility(View.GONE);
        }
    }

}
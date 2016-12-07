package com.tosslab.jandi.app.ui.sign.signin;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.events.profile.ForgotPasswordEvent;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.sign.signin.dagger.DaggerSignInComponent;
import com.tosslab.jandi.app.ui.sign.signin.dagger.SignInModule;
import com.tosslab.jandi.app.ui.sign.signin.presenter.SignInPresenter;
import com.tosslab.jandi.app.ui.sign.signup.SignUpActivity;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimatorListener;
import com.tosslab.jandi.app.views.listeners.SimpleTextWatcher;

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
    TextView btnSignIn;

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

        btnSignIn.setEnabled(false);
        etEmail.addTextChangedListener(new TextInputWatcher());
        etPassword.addTextChangedListener(new TextInputWatcher());
        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (btnSignIn.isEnabled()) {
                    signInPresenter.trySignIn(
                            etEmail.getText().toString(), etPassword.getText().toString());
                }
                return true;
            }
            return false;
        });

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpActionBar();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
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
        SignUpActivity.startActivity(SignInActivity.this, etEmail.getText().toString());
        AnalyticsUtil.sendConversion("Android_Register", "957512006", "l9F-CIeql2MQxvLJyAM");
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SignIn, AnalyticsValue.Action.SignUp);

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

        startBounceAnimation(etLayoutEmail.getChildAt(etLayoutEmail.getChildCount() - 1));
    }

    @Override
    public void showErrorInvalidEmail() {
        if (!etLayoutEmail.isErrorEnabled()) {
            etLayoutEmail.setErrorEnabled(true);
        }
        etLayoutEmail.setError(getString(R.string.jandi_err_invalid_email));

        startBounceAnimation(etLayoutEmail.getChildAt(etLayoutEmail.getChildCount() - 1));
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

        startBounceAnimation(etLayoutPassword.getChildAt(etLayoutPassword.getChildCount() - 1));
    }

    @Override
    public void showErrorInvalidPassword() {
        if (!etLayoutPassword.isErrorEnabled()) {
            etLayoutPassword.setErrorEnabled(true);
        }
        etLayoutPassword.setError(getString(R.string.jandi_password_strength_too_short));

        startBounceAnimation(etLayoutPassword.getChildAt(etLayoutPassword.getChildCount() - 1));
    }

    private void startBounceAnimation(View view) {
        float startX = -UiUtils.getPixelFromDp(5);
        float endX = UiUtils.getPixelFromDp(5);

        ValueAnimator bounceAnim = ValueAnimator.ofFloat(startX, endX);
        bounceAnim.setDuration(50);
        bounceAnim.setRepeatCount(3);
        bounceAnim.setRepeatMode(ValueAnimator.REVERSE);
        bounceAnim.addUpdateListener(animation ->
                view.setTranslationX((Float) animation.getAnimatedValue()));
        bounceAnim.addListener(new SimpleEndAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setTranslationX(0);
            }
        });
        bounceAnim.start();
    }

    @Override
    public void removeErrorPassword() {
        etLayoutPassword.setError("");
        etLayoutPassword.setErrorEnabled(false);
    }

    @Override
    public void showErrorInvalidEmailOrPassword() {
        tvErrorIdOrPassword.setVisibility(View.VISIBLE);
        startBounceAnimation(tvErrorIdOrPassword);
    }

    private void setMarginTopPasswordLayout(float marginDip) {
        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) etLayoutPassword.getLayoutParams();

        int marginTop = (int) UiUtils.getPixelFromDp(marginDip);
        int marginSide = (int) UiUtils.getPixelFromDp(16f);
        params.setMargins(marginSide, marginTop, marginSide, 0);
        etLayoutPassword.setLayoutParams(params);
    }

    @Override
    public void showNetworkErrorToast() {
        ColoredToast.showError(R.string.err_network);
    }

    @Override
    public void showFailPasswordResetToast() {
        ColoredToast.showError(getString(R.string.jandi_fail_send_password_reset_email));
    }

    @Override
    public void showSuggestJoin(String email) {
        new AlertDialog.Builder(SignInActivity.this, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setMessage(R.string.jandi_sign_up_now)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_confirm, (dialog, which) -> {
                    SignUpActivity.startActivity(SignInActivity.this, email);
                })
                .create()
                .show();
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
        startActivity(Henson.with(this)
                .gotoTeamSelectListActivity()
                .shouldRefreshAccountInfo(false)
                .build().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP));
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

    private class TextInputWatcher extends SimpleTextWatcher {
        @Override
        public void afterTextChanged(Editable editable) {
            if (etEmail.getText().length() > 0 && etPassword.getText().length() > 0) {
                btnSignIn.setEnabled(true);
            } else {
                btnSignIn.setEnabled(false);
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
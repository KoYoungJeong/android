package com.tosslab.jandi.app.ui.sign.signup;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.sign.signup.dagger.DaggerSignUpComponent;
import com.tosslab.jandi.app.ui.sign.signup.dagger.SignUpModule;
import com.tosslab.jandi.app.ui.sign.signup.presenter.SignUpPresenter;
import com.tosslab.jandi.app.ui.term.TermActivity;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimatorListener;
import com.tosslab.jandi.app.views.listeners.SimpleTextWatcher;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;

public class SignUpActivity extends BaseAppCompatActivity implements SignUpPresenter.View {

    private static final String EXTRA_PRESERVED_EMAIL = "preserved_email";
    @Inject
    SignUpPresenter signUpPresenter;

    @Bind(R.id.et_layout_email)
    TextInputLayout etLayoutEmail;

    @Bind(R.id.et_layout_password)
    TextInputLayout etLayoutPassword;

    @Bind(R.id.et_email)
    EditText etEmail;

    @Bind(R.id.et_name)
    EditText etName;

    @Bind(R.id.et_password)
    EditText etPassword;

    @Bind(R.id.btn_sign_up)
    TextView btnSignUp;

    @Bind(R.id.tv_term_line)
    TextView tvTermLine;

    @Bind(R.id.scroll_view)
    ScrollView scrollView;

    ProgressWheel progressWheel;

    private android.view.View previousFocusView;

    private String preservedEmail;

    public static void startActivity(Context context, String preservedEmail) {
        Intent intent = new Intent(context, SignUpActivity.class);
        if (!TextUtils.isEmpty(preservedEmail)) {
            intent.putExtra(EXTRA_PRESERVED_EMAIL, preservedEmail);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setNeedUnLockPassCode(false);
        setShouldReconnectSocketService(false);

        ButterKnife.bind(this);

        DaggerSignUpComponent.builder()
                .signUpModule(new SignUpModule(this))
                .build()
                .inject(this);

        initExtra();

        btnSignUp.setEnabled(false);
        etEmail.addTextChangedListener(new TextInputWatcher());
        etPassword.addTextChangedListener(new TextInputWatcher());
        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (btnSignUp.isEnabled()) {
                    signUpPresenter.trySignUp(
                            etName.getText().toString(), etEmail.getText().toString(), etPassword.getText().toString());
                }
                return true;
            }
            return false;
        });

        if (!TextUtils.isEmpty(preservedEmail)) {
            etEmail.setText(preservedEmail);
        }
    }

    private void initExtra() {
        if (getIntent() != null) {
            preservedEmail = getIntent().getStringExtra(EXTRA_PRESERVED_EMAIL);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpActionBar();
        makeTermClickableMessage();
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
        if (focused) {
            if (previousFocusView == etPassword) {
                signUpPresenter.checkPasswordValidation(etPassword.getText().toString());
            }
            removeErrorEmail();
        }
        if (etLayoutEmail.isErrorEnabled()) {
            setMarginTop(etLayoutEmail, 0);
        } else {
            setMarginTop(etLayoutEmail, 8);
        }
        previousFocusView = etEmail;
    }

    @OnFocusChange(R.id.et_password)
    void onPasswordFocused(boolean focused) {
        if (focused) {
            int marginTopForNormal = 8;
            if (previousFocusView == etEmail) {
                signUpPresenter.checkEmailValidation(etEmail.getText().toString());
            }
            removeErrorPassword();
            if (etLayoutPassword.isErrorEnabled()) {
                setMarginTop(etLayoutPassword, 0);
            } else {
                setMarginTop(etLayoutPassword, marginTopForNormal);
            }

            previousFocusView = etPassword;
            scrollView.scrollTo(0, scrollView.getBottom());
        }
    }

    @OnClick(R.id.btn_sign_up)
    void onClickSignUpButton() {
        signUpPresenter.trySignUp(
                etName.getText().toString(),
                etEmail.getText().toString(),
                etPassword.getText().toString());
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SignUp, AnalyticsValue.Action.SignUpNow);
    }

    @Override
    public void showErrorInsertEmail() {
        etLayoutEmail.setErrorEnabled(true);
        etLayoutEmail.setError(getString(R.string.jandi_err_input_email));

        startBounceAnimation(etLayoutEmail.getChildAt(etLayoutEmail.getChildCount() - 1));
    }

    @Override
    public void showErrorInvalidEmail() {
        etLayoutEmail.setErrorEnabled(true);
        etLayoutEmail.setError(getString(R.string.jandi_err_invalid_email));

        startBounceAnimation(etLayoutEmail.getChildAt(etLayoutEmail.getChildCount() - 1));
    }

    @Override
    public void showErrorDuplicationEmail() {
        etLayoutEmail.setErrorEnabled(true);
        etLayoutEmail.setError(getString(R.string.jandi_duplicate_email));

        startBounceAnimation(etLayoutEmail.getChildAt(etLayoutEmail.getChildCount() - 1));
    }

    @Override
    public void showErrorInsertPassword() {
        etLayoutPassword.setErrorEnabled(true);
        etLayoutPassword.setError(getString(R.string.jandi_err_input_password));

        startBounceAnimation(etLayoutPassword.getChildAt(etLayoutPassword.getChildCount() - 1));
    }

    @Override
    public void showErrorShortPassword() {
        etLayoutPassword.setErrorEnabled(true);
        etLayoutPassword.setError(getString(R.string.jandi_password_strength_too_short));

        startBounceAnimation(etLayoutPassword.getChildAt(etLayoutPassword.getChildCount() - 1));
    }

    @Override
    public void showErrorWeakPassword() {
        etLayoutPassword.setErrorEnabled(true);
        etLayoutPassword.setError(getString(R.string.jandi_password_strength_weak));

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
    public void removeErrorEmail() {
        etLayoutEmail.setError("");
        etLayoutEmail.setErrorEnabled(false);
    }

    @Override
    public void removeErrorPassword() {
        etLayoutPassword.setError("");
        etLayoutPassword.setErrorEnabled(false);
    }

    private void setMarginTop(View view, float marginDip) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        int marginTop = (int) UiUtils.getPixelFromDp(marginDip);
        int marginSide = (int) UiUtils.getPixelFromDp(16f);
        params.setMargins(marginSide, marginTop, marginSide, 0);
        view.setLayoutParams(params);
    }

    @Override
    public void showProgressWheel() {
        if (progressWheel == null) {
            progressWheel = new ProgressWheel(this);
        }
        progressWheel.show();
    }

    @Override
    public void dismissProgressWheel() {
        if (progressWheel != null) {
            progressWheel.dismiss();
        }
    }

    @Override
    public void showNetworkErrorToast() {
        ColoredToast.showError(R.string.err_network);
    }

    @Override
    public void startSignUpRequestVerifyActivity() {
        JandiPreference.setEmailAuthSendTime();
        startActivity(Henson.with(this)
                .gotoSignUpVerifyActivity()
                .email(etEmail.getText().toString())
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK));
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

    private void makeTermClickableMessage() {
        String tos = getString(R.string.jandi_tos);
        String pp = getString(R.string.jandi_pp);
        String termMessage = getString(R.string.jandi_tab_tos_and_pp, tos, pp);

        SpannableString ss = new SpannableString(termMessage);

        ClickableSpan tosClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(android.view.View textView) {
                clickAgreeTosLink();
                AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SignUp, AnalyticsValue.Action.TermofService);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };

        ClickableSpan ppClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(android.view.View textView) {
                clickAgreePPLink();
                AnalyticsUtil.sendEvent(
                        AnalyticsValue.Screen.SignUp, AnalyticsValue.Action.PrivacyPolicy);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };

        ss.setSpan(tosClickableSpan,
                termMessage.indexOf(tos),
                termMessage.indexOf(tos) + tos.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        ss.setSpan(ppClickableSpan,
                termMessage.indexOf(pp),
                termMessage.indexOf(pp) + pp.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvTermLine.setText(ss);
        tvTermLine.setMovementMethod(LinkMovementMethod.getInstance());
    }

    void clickAgreeTosLink() {
        startActivity(new Intent(this, TermActivity.class)
                .putExtra(TermActivity.EXTRA_TERM_MODE, TermActivity.Mode.Agreement.name()));
    }

    void clickAgreePPLink() {
        startActivity(new Intent(this, TermActivity.class)
                .putExtra(TermActivity.EXTRA_TERM_MODE, TermActivity.Mode.Privacy.name()));
    }

    private class TextInputWatcher extends SimpleTextWatcher {
        @Override
        public void afterTextChanged(Editable editable) {
            if (etEmail.getText().length() > 0
                    && etPassword.getText().length() > 0) {
                btnSignUp.setEnabled(true);
            } else {
                btnSignUp.setEnabled(false);
            }
            if (etEmail.isFocused() && etLayoutEmail.isErrorEnabled()) {
                removeErrorEmail();
            }
            if (etPassword.isFocused() && etLayoutPassword.isErrorEnabled()) {
                removeErrorPassword();
            }
        }

    }
}
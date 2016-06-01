package com.tosslab.jandi.app.ui.intro.signup;

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
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.ads.conversiontracking.AdWordsConversionReporter;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.intro.signup.dagger.DaggerMainSignUpComponent;
import com.tosslab.jandi.app.ui.intro.signup.dagger.MainSignUpModule;
import com.tosslab.jandi.app.ui.intro.signup.presenter.MainSignUpPresenter;
import com.tosslab.jandi.app.ui.intro.signup.verify.SignUpVerifyActivity_;
import com.tosslab.jandi.app.ui.term.TermActivity;
import com.tosslab.jandi.app.ui.term.TermActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;

/**
 * Created by tee on 16. 5. 25..
 */

public class MainSignUpActivity extends BaseAppCompatActivity implements MainSignUpPresenter.View {

    @Inject
    MainSignUpPresenter mainSignUpPresenter;

    @Bind(R.id.et_layout_name)
    TextInputLayout etLayoutName;

    @Bind(R.id.et_layout_email)
    TextInputLayout etLayoutEmail;

    @Bind(R.id.et_layout_password)
    TextInputLayout etLayoutPassword;

    @Bind(R.id.et_name)
    EditText etName;

    @Bind(R.id.et_email)
    EditText etEmail;

    @Bind(R.id.et_password)
    EditText etPassword;

    @Bind(R.id.tv_sign_up_button)
    TextView tvSignUpButton;

    @Bind(R.id.tv_term_line)
    TextView tvTermLine;

    ProgressWheel progressWheel;

    private boolean isFirstFocus = true;

    private android.view.View previousFocusView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jandi_sign_up);
        ButterKnife.bind(this);
        DaggerMainSignUpComponent.builder()
                .mainSignUpModule(new MainSignUpModule(this))
                .build()
                .inject(this);
        tvSignUpButton.setEnabled(false);
        etName.addTextChangedListener(new EtTextWatcher());
        etEmail.addTextChangedListener(new EtTextWatcher());
        etPassword.addTextChangedListener(new EtTextWatcher());
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

    @OnFocusChange(R.id.et_name)
    void onNameFocused(boolean focused) {
        if (focused) {
            if (!isFirstFocus) {
                if (previousFocusView == etPassword) {
                    mainSignUpPresenter.checkPasswordValidation(etPassword.getText().toString());
                } else if (previousFocusView == etEmail) {
                    mainSignUpPresenter.checkEmailValidation(etEmail.getText().toString());
                }
                removeErrorName();
            }
            previousFocusView = etName;
        }
        if (etLayoutName.isErrorEnabled()) {
            setMarginTopEmailLayout(0);
        } else {
            setMarginTopEmailLayout(8);
        }
        isFirstFocus = false;
    }

    @OnFocusChange(R.id.et_email)
    void onEmailFocused(boolean focused) {
        if (focused) {
            if (previousFocusView == etPassword) {
                mainSignUpPresenter.checkPasswordValidation(etPassword.getText().toString());
            } else if (previousFocusView == etName) {
                mainSignUpPresenter.checkNameValidation(etName.getText().toString());
            }
            removeErrorEmail();
        }
        if (etLayoutEmail.isErrorEnabled()) {
            setMarginTopPasswordLayout(0);
        } else {
            setMarginTopPasswordLayout(8);
        }
        previousFocusView = etEmail;
    }

    @OnFocusChange(R.id.et_password)
    void onPasswordFocused(boolean focused) {
        if (focused) {
            if (previousFocusView == etEmail) {
                mainSignUpPresenter.checkEmailValidation(etEmail.getText().toString());
            } else if (previousFocusView == etName) {
                mainSignUpPresenter.checkNameValidation(etName.getText().toString());
            }
            removeErrorPassword();
            previousFocusView = etPassword;
        }
    }

    @OnClick(R.id.tv_sign_up_button)
    void onClickSignUpButton() {
        if (isValidInputElements()) {
            AdWordsConversionReporter.reportWithConversionId(JandiApplication.getContext(),
                    "957512006", "fVnsCMKD_GEQxvLJyAM", "0.00", true);
            mainSignUpPresenter.trySignUp(
                    etName.getText().toString()
                    , etEmail.getText().toString()
                    , etPassword.getText().toString());
        }
    }

    @Override
    public void showErrorInsertName() {
        etLayoutName.setErrorEnabled(true);
        etLayoutName.setError(getString(R.string.jandi_input_user_name));
    }

    @Override
    public void showErrorInsertEmail() {
        etLayoutEmail.setErrorEnabled(true);
        etLayoutEmail.setError(getString(R.string.jandi_err_input_email));
    }

    @Override
    public void showErrorInvalidEmail() {
        etLayoutEmail.setErrorEnabled(true);
        etLayoutEmail.setError(getString(R.string.jandi_err_invalid_email));
    }

    @Override
    public void showErrorDuplicationEmail() {
        etLayoutEmail.setErrorEnabled(true);
        etLayoutEmail.setError(getString(R.string.jandi_duplicate_email));
    }

    @Override
    public void showErrorInsertPassword() {
        etLayoutPassword.setErrorEnabled(true);
        etLayoutPassword.setError(getString(R.string.jandi_err_input_password));
    }

    @Override
    public void showErrorShortPassword() {
        etLayoutPassword.setErrorEnabled(true);
        etLayoutPassword.setError(getString(R.string.jandi_password_strength_too_short));
    }

    @Override
    public void showErrorWeakPassword() {
        etLayoutPassword.setErrorEnabled(true);
        etLayoutPassword.setError(getString(R.string.jandi_password_strength_weak));
    }

    @Override
    public void removeErrorName() {
        etLayoutName.setError("");
        etLayoutName.setErrorEnabled(false);
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

    private void setMarginTopEmailLayout(float marginDip) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) etLayoutEmail.getLayoutParams();
        DisplayMetrics displayMetrics = JandiApplication.getContext().getResources().getDisplayMetrics();
        int marginTop = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, marginDip, displayMetrics));
        int marginSide = (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, displayMetrics));
        params.setMargins(marginSide, marginTop, marginSide, 0);
        etLayoutEmail.setLayoutParams(params);
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
        ColoredToast.show(R.string.err_network);
    }

    @Override
    public void startSignUpRequestVerifyActivity() {
        SignUpVerifyActivity_.intent(this)
                .email(etEmail.getText().toString())
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .start();
    }

    private boolean isValidInputElements() {
        boolean isValid = true;
        isValid = mainSignUpPresenter.checkNameValidation(etName.getText().toString()) && isValid;
        isValid = mainSignUpPresenter.checkEmailValidation(etEmail.getText().toString()) && isValid;
        isValid = mainSignUpPresenter.checkPasswordValidation(etPassword.getText().toString()) && isValid;
        return isValid;
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
        String termMessage = getString(R.string.jandi_tab_tos_and_pp
                , tos, pp);

        SpannableString ss = new SpannableString(termMessage);

        ClickableSpan tosClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(android.view.View textView) {
                clickAgreeTosLink();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
            }
        };

        ClickableSpan ppClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(android.view.View textView) {
                clickAgreePPLink();
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
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
        TermActivity_.intent(this)
                .termMode(TermActivity.Mode.Agreement.name())
                .start();
    }

    void clickAgreePPLink() {
        TermActivity_.intent(this)
                .termMode(TermActivity.Mode.Privacy.name())
                .start();
    }

    private class EtTextWatcher implements TextWatcher {
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            if (etName.getText().length() > 0
                    && etEmail.getText().length() > 0
                    && etPassword.getText().length() > 0) {
                tvSignUpButton.setEnabled(true);
            } else {
                tvSignUpButton.setEnabled(false);
            }
        }
    }

}
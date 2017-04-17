package com.tosslab.jandi.app.ui.sign.changepassword;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.events.profile.ForgotPasswordEvent;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.sign.changepassword.dagger.ChangePasswordModule;
import com.tosslab.jandi.app.ui.sign.changepassword.dagger.DaggerChangePasswordComponent;
import com.tosslab.jandi.app.ui.sign.changepassword.presenter.ChangePasswordPresenter;
import com.tosslab.jandi.app.ui.sign.signin.SignInActivity;
import com.tosslab.jandi.app.ui.sign.signup.SignUpActivity;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.SignOutUtil;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimatorListener;
import com.tosslab.jandi.app.views.listeners.SimpleTextWatcher;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import de.greenrobot.event.EventBus;
import rx.Completable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;

/**
 * Created by tee on 2017. 4. 11..
 */

public class ChangePasswordActivity extends AppCompatActivity implements ChangePasswordPresenter.View {

    @Inject
    ChangePasswordPresenter resetPasswordPresenter;

    @Bind(R.id.et_layout_current_password)
    TextInputLayout etLayoutCurrentPassword;

    @Bind(R.id.et_layout_new_password)
    TextInputLayout etLayoutNewPassword;

    @Bind(R.id.et_layout_new_password_again)
    TextInputLayout etLayoutNewPasswordAgain;

    @Bind(R.id.et_current_password)
    EditText etCurrentPassword;

    @Bind(R.id.et_new_password)
    EditText etNewPassword;

    @Bind(R.id.et_new_password_again)
    EditText etNewPasswordAgain;

    @Bind(R.id.tv_reset_password_done_button)
    TextView tvResetPasswordDoneButton;

    @Bind(R.id.tv_forget_password_button)
    TextView tvForgetPasswordButton;

    @Bind(R.id.tv_current_password_desc)
    TextView tvCurrentPasswordDesc;

    @Bind(R.id.tv_new_password_again_desc)
    TextView tvNewPasswordAgainDesc;

    @Bind(R.id.scroll_view)
    ScrollView scrollView;

    @Bind(R.id.iv_current_password_clear_button)
    ImageView ivCurrentPasswordClearButton;

    @Bind(R.id.iv_new_password_clear_button)
    ImageView ivNewPasswordClearButton;

    @Bind(R.id.iv_new_password_again_clear_button)
    ImageView ivNewPasswordAgainClearButton;

    private ProgressWheel progressWheel;

    private boolean isFirstFocus = true;

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, ChangePasswordActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        ButterKnife.bind(this);
        setUpActionBar();
        DaggerChangePasswordComponent.builder()
                .changePasswordModule(new ChangePasswordModule(this))
                .build()
                .inject(this);

        tvResetPasswordDoneButton.setEnabled(false);

        etCurrentPassword.addTextChangedListener(new TextInputWatcher());
        etNewPassword.addTextChangedListener(new TextInputWatcher());
        etNewPasswordAgain.addTextChangedListener(new TextInputWatcher());

        etLayoutCurrentPassword.setErrorTextAppearance(R.style.TextInputLayoutError);
        etLayoutNewPassword.setErrorTextAppearance(R.style.TextInputLayoutError);
        etLayoutNewPasswordAgain.setErrorTextAppearance(R.style.TextInputLayoutError);

        etLayoutCurrentPassword.setHintTextAppearance(R.style.TextInputLayoutHint);
        etLayoutNewPassword.setHintTextAppearance(R.style.TextInputLayoutHint);
        etLayoutNewPasswordAgain.setHintTextAppearance(R.style.TextInputLayoutHint);

        SpannableString content = new SpannableString(tvForgetPasswordButton.getText());
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        tvForgetPasswordButton.setText(content);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Observable.defer(new Func0<Observable<Object>>() {
                    @Override
                    public Observable<Object> call() {
                        hideKeyboard();
                        return Observable.just(0);
                    }
                }).delay(300, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(i -> {
                            finish();
                        });
                break;
        }
        return true;
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void removeErrorCurrentPassword() {
        etLayoutCurrentPassword.setError("");
        etLayoutCurrentPassword.setErrorEnabled(false);
        tvCurrentPasswordDesc.setVisibility(View.VISIBLE);
    }

    @Override
    public void removeErrorNewPassword() {
        etLayoutNewPassword.setError("");
        etLayoutNewPassword.setErrorEnabled(false);
    }

    @Override
    public void removeErrorNewPasswordAgain() {
        etLayoutNewPasswordAgain.setError("");
        etLayoutNewPasswordAgain.setErrorEnabled(false);
        tvNewPasswordAgainDesc.setVisibility(View.VISIBLE);
    }

    @Override
    public void showErrorWeakNewPassword() {
        etLayoutNewPassword.setErrorEnabled(true);
        etLayoutNewPassword.setError(getString(R.string.password_new_type_confi_alert));
        startBounceAnimation(etLayoutNewPassword.getChildAt(etLayoutNewPassword.getChildCount() - 1));
    }

    @Override
    public void showErrorNotSameNewPassword() {
        tvNewPasswordAgainDesc.setVisibility(View.GONE);
        etLayoutNewPasswordAgain.setErrorEnabled(true);
        etLayoutNewPasswordAgain.setError(getString(R.string.jandi_incorrect_passcode));
        startBounceAnimation(etLayoutNewPasswordAgain.getChildAt(etLayoutNewPasswordAgain.getChildCount() - 1));
    }

    @Override
    public void showErrorNotValidCurrentPassword() {
        scrollView.scrollTo(0, 0);
        tvCurrentPasswordDesc.setVisibility(View.GONE);
        etLayoutCurrentPassword.setErrorEnabled(true);
        etLayoutCurrentPassword.setError(getString(R.string.password_current_type_alert));
        startBounceAnimation(etLayoutCurrentPassword.getChildAt(etLayoutCurrentPassword.getChildCount() - 1));
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


    @OnFocusChange(R.id.et_current_password)
    void onCurrentPasswordFocused(boolean focused) {
        if (focused && !isFirstFocus) {
            resetPasswordPresenter.checkNewPasswordAgainValidation(
                    etNewPassword.getText().toString(),
                    etNewPasswordAgain.getText().toString());
            resetPasswordPresenter.checkNewPasswordValidation(etNewPassword.getText().toString());
        }

        if (focused) {
            if (etLayoutCurrentPassword.isErrorEnabled()) {
                etCurrentPassword.setText("");
                removeErrorCurrentPassword();
            }
            ivNewPasswordClearButton.setVisibility(View.GONE);
            ivNewPasswordAgainClearButton.setVisibility(View.GONE);

            if (etCurrentPassword.length() > 0) {
                ivCurrentPasswordClearButton.setVisibility(View.VISIBLE);
            }
        }
    }

    @OnFocusChange(R.id.et_new_password)
    void onNewPasswordFocused(boolean focused) {
        if (focused && !isFirstFocus) {
            resetPasswordPresenter.checkNewPasswordAgainValidation(
                    etNewPassword.getText().toString(),
                    etNewPasswordAgain.getText().toString());
        }
        if (focused) {
            if (etLayoutNewPassword.isErrorEnabled()) {
                etNewPassword.setText("");
                removeErrorNewPassword();
            }
            ivCurrentPasswordClearButton.setVisibility(View.GONE);
            if (etNewPassword.length() > 0) {
                ivNewPasswordClearButton.setVisibility(View.VISIBLE);
            }
            ivNewPasswordAgainClearButton.setVisibility(View.GONE);
        }
        isFirstFocus = false;
    }

    @OnFocusChange(R.id.et_new_password_again)
    void onNewPasswordAgainFocused(boolean focused) {
        if (focused) {
            if (etNewPassword.getText().toString().isEmpty()) {
                etNewPassword.requestFocus();
                Completable.complete()
                        .delay(300, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            showErrorWeakNewPassword();
                        });
            } else {
                scrollView.scrollTo(0, scrollView.getBottom());
            }
            if (etLayoutNewPasswordAgain.isErrorEnabled()) {
                etNewPasswordAgain.setText("");
            }
            resetPasswordPresenter.checkNewPasswordValidation(etNewPassword.getText().toString());
            removeErrorNewPasswordAgain();
            ivCurrentPasswordClearButton.setVisibility(View.GONE);
            ivNewPasswordClearButton.setVisibility(View.GONE);
            if (etNewPasswordAgain.length() > 0) {
                ivNewPasswordAgainClearButton.setVisibility(View.VISIBLE);
            }
        }
    }

    @OnClick(R.id.iv_current_password_clear_button)
    void onCurrentPasswordClear() {
        etCurrentPassword.setText("");
    }

    @OnClick(R.id.iv_new_password_clear_button)
    void onNewPasswordClear() {
        etNewPassword.setText("");
    }

    @OnClick(R.id.iv_new_password_again_clear_button)
    void onNewPasswordAgainClear() {
        etNewPasswordAgain.setText("");
    }

    @Override
    public void setDoneButtonEnable(boolean enable) {
        if (enable) {
            tvResetPasswordDoneButton.setEnabled(true);
        } else {
            tvResetPasswordDoneButton.setEnabled(false);
        }
    }

    @OnClick(R.id.tv_reset_password_done_button)
    void onClickChangePasswordDone() {
        hideKeyboard();
        resetPasswordPresenter.setNewPassword(
                etCurrentPassword.getText().toString(),
                etNewPassword.getText().toString(),
                etNewPasswordAgain.getText().toString());
    }

    @OnClick(R.id.tv_forget_password_button)
    void onForgetPasswordButton() {
        TeamInfoLoader teamInfoLoader = TeamInfoLoader.getInstance();
        DialogFragment dialogFragment =
                EditTextDialogFragment.newInstance(
                        EditTextDialogFragment.ACTION_FORGOT_PASSWORD,
                        teamInfoLoader.getMember(teamInfoLoader.getMyId()).getEmail());
        dialogFragment.show(getFragmentManager(), "dialog");
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SignIn, AnalyticsValue.Action.ForgotPW);
    }

    @Override
    public void showSuccessDialog() {
        new AlertDialog.Builder(this, R.style.JandiTheme_AlertDialog_FixWidth_280)
                .setMessage(R.string.password_change_done)
                .setPositiveButton(R.string.jandi_confirm, (dialog, which) -> {
                    SignOutUtil.removeSignData();
                    BadgeUtils.clearBadge(JandiApplication.getContext());
                    JandiSocketService.stopService(JandiApplication.getContext());
                    Intent intent = new Intent(this, SignInActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .create()
                .show();
    }

    public void onEvent(ForgotPasswordEvent event) {
        String email = event.getEmail();
        resetPasswordPresenter.forgotPassword(email);
    }

    @Override
    public void showSuggestJoin(String email) {
        new AlertDialog.Builder(this, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setMessage(R.string.jandi_sign_up_now)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_confirm, (dialog, which) -> {
                    SignUpActivity.startActivity(this, email);
                })
                .create()
                .show();
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
    public void showPasswordResetEmailSendSucsess() {
        new AlertDialog.Builder(this, R.style.JandiTheme_AlertDialog_FixWidth_280)
                .setMessage(R.string.sent_auth_email_short)
                .setPositiveButton(R.string.jandi_confirm, (dialog, which) -> {
                })
                .create()
                .show();
    }


    private class TextInputWatcher extends SimpleTextWatcher {
        @Override
        public void afterTextChanged(Editable editable) {

            resetPasswordPresenter.checkCanSendChangePassword(
                    etCurrentPassword.getText().toString(),
                    etNewPassword.getText().toString(),
                    etNewPasswordAgain.getText().toString()
            );

            if (etCurrentPassword.isFocused() && etCurrentPassword.getText().length() > 0) {
                ivCurrentPasswordClearButton.setVisibility(View.VISIBLE);
            } else {
                ivCurrentPasswordClearButton.setVisibility(View.GONE);
            }

            if (etNewPassword.isFocused() && etNewPassword.getText().length() > 0) {
                ivNewPasswordClearButton.setVisibility(View.VISIBLE);
            } else {
                ivNewPasswordClearButton.setVisibility(View.GONE);
            }

            if (etNewPasswordAgain.isFocused() && etNewPasswordAgain.getText().length() > 0) {
                ivNewPasswordAgainClearButton.setVisibility(View.VISIBLE);
            } else {
                ivNewPasswordAgainClearButton.setVisibility(View.GONE);
            }

            if (etCurrentPassword.isFocused() && etLayoutCurrentPassword.isErrorEnabled()) {
                removeErrorCurrentPassword();
            }

            if (etNewPassword.isFocused() && etLayoutNewPassword.isErrorEnabled()) {
                removeErrorNewPassword();
            }

            if (etNewPasswordAgain.isFocused() && etLayoutNewPasswordAgain.isErrorEnabled()) {
                removeErrorNewPasswordAgain();
            }

            if (etLayoutNewPasswordAgain.isErrorEnabled()
                    && etNewPassword.getText().toString().equals(etNewPasswordAgain.getText().toString())) {
                removeErrorNewPasswordAgain();

            }
        }
    }

}

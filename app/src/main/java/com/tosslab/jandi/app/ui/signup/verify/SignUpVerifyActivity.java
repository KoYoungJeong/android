package com.tosslab.jandi.app.ui.signup.verify;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.account.AccountHomeActivity_;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.signup.verify.presenter.SignUpVerifyPresenter;
import com.tosslab.jandi.app.ui.signup.verify.view.SignUpVerifyView;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimationListener;
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
import org.androidannotations.annotations.LongClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.ViewsById;

import java.util.List;

/**
 * Created by tonyjs on 15. 5. 19..
 */
@EActivity(R.layout.activity_signup_verify)
public class SignUpVerifyActivity extends BaseAppCompatActivity implements SignUpVerifyView {

    private static final int MAX_VERIFY_CODE = 4;

    @Extra("email")
    String email;

    @Bean
    SignUpVerifyPresenter presenter;

    @ViewById(R.id.btn_verify)
    Button btnVerify;

    @ViewById(R.id.iv_signup_verify_code_cursor)
    ImageView ivFakeCursor;

    @ViewById(R.id.tv_resend_email)
    TextView tvResendEmail;

    @ViewById(R.id.tv_signup_verify_code)
    TextView tvVerifyCode;
    @ViewById(R.id.tv_signup_verify_explain)
    TextView tvExplain;

    @ViewsById(value = {
            R.id.iv_signup_verify_input_0,
            R.id.iv_signup_verify_input_1,
            R.id.iv_signup_verify_input_2,
            R.id.iv_signup_verify_input_3,
            R.id.iv_signup_verify_input_4,
            R.id.iv_signup_verify_input_5,
            R.id.iv_signup_verify_input_6,
            R.id.iv_signup_verify_input_7,
            R.id.iv_signup_verify_input_8,
            R.id.iv_signup_verify_input_9
    })
    List<View> ivInputCodes;

    @SystemService
    InputMethodManager inputMethodManager;
    private ProgressWheel progressWheel;
    private Blink blink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedUnLockPassCode(false);
    }

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
        progressWheel.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.CodeVerification);

        blink = new Blink(ivFakeCursor);
        startBlink();
    }

    @Override
    protected void onDestroy() {
        stopBlink();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void setUpActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.jandi_signup_verify_title));
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
        String invalidateText = getString(R.string.jandi_signup_invalidate_code, count);
        tvExplain.setText(invalidateText);
        int invalidTextColor = getResources().getColor(R.color.jandi_signup_invalid);
        tvExplain.setTextColor(invalidTextColor);
        tvVerifyCode.setTextColor(invalidTextColor);
        Animation animation = AnimationUtils.loadAnimation(SignUpVerifyActivity.this, R.anim.shake);
        animation.setAnimationListener(new SimpleEndAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                if (tvVerifyCode.length() == MAX_VERIFY_CODE) {
                    // 애니메이션 도중 사용자가 수정할 경우...
                    tvVerifyCode.setText("");
                }
                tvVerifyCode.setTextColor(getResources().getColor(R.color.jandi_text));
            }
        });
        tvVerifyCode.startAnimation(animation);
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
    public void changeExplainText() {
        tvExplain.setTextColor(getResources().getColor(R.color.account_home_help_dialog_text));
        tvExplain.setText(R.string.jandi_signup_verification_code);
    }

    @UiThread
    @Override
    public void clearVerifyCode() {
        tvVerifyCode.setText("");
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
    void verify() {

        presenter.verifyCode(email, tvVerifyCode.getText().toString());

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.CodeVerification,
                AnalyticsValue.Action.LaunchJandi);
    }

    @Click(R.id.tv_resend_email)
    void resendEmail() {
        presenter.requestNewVerificationCode(email);

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.CodeVerification,
                AnalyticsValue.Action.Resend);
    }

    @Click(value = {
            R.id.iv_signup_verify_input_0,
            R.id.iv_signup_verify_input_1,
            R.id.iv_signup_verify_input_2,
            R.id.iv_signup_verify_input_3,
            R.id.iv_signup_verify_input_4,
            R.id.iv_signup_verify_input_5,
            R.id.iv_signup_verify_input_6,
            R.id.iv_signup_verify_input_7,
            R.id.iv_signup_verify_input_8,
            R.id.iv_signup_verify_input_9
    })
    void onInputClick(View v) {
        int idx = ivInputCodes.indexOf(v);

        if (idx < 0) {
            return;
        }
        if (tvVerifyCode.getText().length() < MAX_VERIFY_CODE) {
            tvVerifyCode.append(String.valueOf(idx));
        }
    }

    @TextChange(R.id.tv_signup_verify_code)
    void onVerifyCodeTextInput(TextView tv) {
        int length = tv.length();
        boolean enabled = length == MAX_VERIFY_CODE;
        btnVerify.setEnabled(enabled);

        if (!enabled) {
            // invalid -> 애니메이션 도중 사용자가 새로 입력을 하려는 경우에 대비
            tvVerifyCode.setTextColor(getResources().getColor(R.color.jandi_text));
        }

        if (length > 0) {
            ivFakeCursor.setVisibility(View.GONE);
            stopBlink();
        } else {
            ivFakeCursor.setVisibility(View.VISIBLE);
            startBlink();
        }

    }

    private void stopBlink() {
        blink.cancel();
    }

    private void startBlink() {
        blink.uncancel();
        blink.postAtTime(blink, SystemClock.uptimeMillis() + 500);
    }

    @Click(R.id.iv_signup_verify_input_del)
    void onDelClick() {
        String lastWord = tvVerifyCode.getText().toString();
        if (lastWord.length() > 0) {
            tvVerifyCode.setText(lastWord.substring(0, lastWord.length() - 1));
        }
    }

    @LongClick(R.id.iv_signup_verify_input_del)
    void onDelLongClick() {
        String lastWord = tvVerifyCode.getText().toString();
        if (lastWord.length() > 0) {
            tvVerifyCode.setText("");
        }
    }

    @OptionsItem(android.R.id.home)
    void onHomeOptionSelected() {
        finish();
    }

    private static class Blink extends Handler implements Runnable {
        private final View blinkView;
        private boolean mCancelled;

        Blink(View blinkView) {
            super();
            this.blinkView = blinkView;
        }

        public void run() {
            if (mCancelled) {
                return;
            }

            removeCallbacks(Blink.this);

            if (blinkView.getVisibility() == View.VISIBLE) {
                blinkView.setVisibility(View.GONE);
            } else {
                blinkView.setVisibility(View.VISIBLE);
            }

            postAtTime(this, SystemClock.uptimeMillis() + 500);
        }

        void cancel() {
            if (!mCancelled) {
                removeCallbacks(Blink.this);
                mCancelled = true;
            }
        }

        void uncancel() {
            mCancelled = false;
        }
    }

}

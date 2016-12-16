package com.tosslab.jandi.app.ui.sign.signup.verify;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.sign.signup.verify.dagger.DaggerSignUpVerifyComponent;
import com.tosslab.jandi.app.ui.sign.signup.verify.dagger.SignUpVerifyModule;
import com.tosslab.jandi.app.ui.sign.signup.verify.presenter.SignUpVerifyPresenter;
import com.tosslab.jandi.app.ui.sign.signup.verify.view.SignUpVerifyView;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.analytics.sprinkler.ScreenViewProperty;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrScreenView;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimationListener;

import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import butterknife.OnTextChanged;

public class SignUpVerifyActivity extends BaseAppCompatActivity implements SignUpVerifyView {

    private static final int MAX_VERIFY_CODE = 4;

    @InjectExtra("email")
    String email;

    SignUpVerifyPresenter presenter;

    @Bind(R.id.iv_signup_verify_code_cursor)
    ImageView ivFakeCursor;

    @Bind(R.id.tv_resend_email)
    TextView tvResendEmail;

    @Bind(R.id.tv_email)
    TextView tvEmail;

    @Bind(value = {
            R.id.tv_signup_verify_code_1,
            R.id.tv_signup_verify_code_2,
            R.id.tv_signup_verify_code_3,
            R.id.tv_signup_verify_code_4
    })
    List<TextView> tvVerifyCodes;
    @Bind(R.id.tv_signup_verify_explain)
    TextView tvExplain;

    @Bind(value = {
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

    InputMethodManager inputMethodManager;
    private ProgressWheel progressWheel;
    private Blink blink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_verify);
        setNeedUnLockPassCode(false);
        setShouldReconnectSocketService(false);
        ButterKnife.bind(this);
        Dart.inject(this);

        DaggerSignUpVerifyComponent.builder()
                .signUpVerifyModule(new SignUpVerifyModule(this))
                .build()
                .inject(this);
        init();
    }

    void init() {
        SprinklrScreenView.sendLog(ScreenViewProperty.CONFIRM_VERIFICATION_NUMBER);
        inputMethodManager = ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE));
        setUpActionBar();

        String resendEmailText = getString(R.string.jandi_signup_resend_email);
        tvResendEmail.setText(Html.fromHtml(resendEmailText));
        tvEmail.setText(email);

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

    @Override
    public void showProgress() {
        if (progressWheel == null || progressWheel.isShowing()) {
            return;
        }
        progressWheel.show();
    }

    @Override
    public void hideProgress() {
        if (progressWheel == null || !progressWheel.isShowing()) {
            return;
        }
        progressWheel.dismiss();
    }

    @Override
    public void showInvalidVerificationCode(int count) {
        String invalidateText = getString(R.string.jandi_signup_invalidate_code, count);
        tvEmail.setVisibility(View.GONE);
        tvExplain.setText(invalidateText);
        int invalidTextColor = getResources().getColor(R.color.jandi_signup_invalid);
        tvExplain.setTextColor(invalidTextColor);
        setVerifyCodeTextColor(invalidTextColor);
        Animation animation = AnimationUtils.loadAnimation(SignUpVerifyActivity.this, R.anim.shake);
        animation.setAnimationListener(new SimpleEndAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                if (getVerifyCode().length() == MAX_VERIFY_CODE) {
                    // 애니메이션 도중 사용자가 수정할 경우...
                    clearVerifyCode();
                }
                setVerifyCodeTextColor(getResources().getColor(R.color.jandi_text));
            }
        });
        ((View) tvVerifyCodes.get(0).getParent()).startAnimation(animation);
    }

    private void setVerifyCodeTextColor(int color) {
        for (TextView tvVerifyCode : tvVerifyCodes) {
            tvVerifyCode.setTextColor(color);
        }
    }

    @Override
    public void showExpiredVerificationCode() {
        new AlertDialog.Builder(SignUpVerifyActivity.this,
                R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setMessage(getString(R.string.jandi_signup_expired_verification_code))
                .setPositiveButton(getString(R.string.jandi_confirm), (dialog, which) -> {
                    presenter.requestNewVerificationCode(email);
                })
                .setNegativeButton(getString(R.string.jandi_cancel), null)
                .create()
                .show();
    }

    @Override
    public void changeExplainText() {
        tvExplain.setTextColor(getResources().getColor(R.color.account_home_help_dialog_text));
        tvExplain.setText(R.string.jandi_signup_verification_code);
    }

    @Override
    public void clearVerifyCode() {
        for (TextView tvVerifyCode : tvVerifyCodes) {
            tvVerifyCode.setText("");
        }
    }

    @Override
    public void showToast(String msg) {
        ColoredToast.show(msg);
    }

    @Override
    public void showErrorToast(String msg) {
        ColoredToast.showError(msg);
    }

    @Override
    public void moveToSelectTeam() {

        AnalyticsUtil.sendConversion("Android_signup", "957512006", "M3MOCM6ij2MQxvLJyAM");

        startActivity(Henson.with(this)
                .gotoTeamSelectListActivity()
                .shouldRefreshAccountInfo(false)
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP));

        overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
        finish();
    }

    void verify() {
        String verifyCode = getVerifyCode();

        presenter.verifyCode(email, verifyCode);

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.CodeVerification,
                AnalyticsValue.Action.LaunchJandi);
    }

    @NonNull
    private String getVerifyCode() {
        StringBuilder builder = new StringBuilder();
        for (TextView tvVerifyCode : tvVerifyCodes) {
            builder.append(tvVerifyCode.getText().toString());
        }
        return builder.toString();
    }

    @OnClick(R.id.tv_resend_email)
    void resendEmail() {
        final long permitEmailSendTermMillis = 15 * 1000;
        if (JandiPreference.getEmailAuthSendTime() + permitEmailSendTermMillis < System.currentTimeMillis()) {
            presenter.requestNewVerificationCode(email);
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.CodeVerification,
                    AnalyticsValue.Action.Resend);
            JandiPreference.setEmailAuthSendTime();
        } else {
            ColoredToast.showGray(R.string.jandi_mail_sending);
        }
    }

    @OnClick(value = {
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
        int verifyCodeLength = getVerifyCode().length();
        if (verifyCodeLength < MAX_VERIFY_CODE) {
            tvVerifyCodes.get(verifyCodeLength).setText(String.valueOf(idx));
        }

        if (getVerifyCode().length() == MAX_VERIFY_CODE) {
            verify();
        }
    }

    @OnTextChanged(R.id.tv_signup_verify_code_1)
    void onVerifyCodeTextInput(TextView tv) {
        int length = getVerifyCode().length();
        boolean enabled = length == MAX_VERIFY_CODE;

        if (!enabled) {
            // invalid -> 애니메이션 도중 사용자가 새로 입력을 하려는 경우에 대비
            setVerifyCodeTextColor(getResources().getColor(R.color.jandi_text));
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

    @OnClick(R.id.iv_signup_verify_input_del)
    void onDelClick() {
        String lastWord = getVerifyCode();
        if (lastWord.length() > 0) {
            tvVerifyCodes.get(lastWord.length() - 1).setText("");
        }
    }

    @OnLongClick(R.id.iv_signup_verify_input_del)
    void onDelLongClick() {
        String lastWord = getVerifyCode();
        if (lastWord.length() > 0) {
            clearVerifyCode();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private static class Blink extends Handler implements Runnable {
        private final WeakReference<View> blinkViewRef;
        private boolean cancelled;

        Blink(View blinkView) {
            super();
            blinkViewRef = new WeakReference<View>(blinkView);
        }

        public void run() {
            if (cancelled) {
                return;
            }

            removeCallbacks(Blink.this);

            View blinkView = blinkViewRef.get();
            if (blinkView == null) {
                return;
            }

            if (blinkView.getVisibility() == View.VISIBLE) {
                blinkView.setVisibility(View.GONE);
            } else {
                blinkView.setVisibility(View.VISIBLE);
            }

            postAtTime(this, SystemClock.uptimeMillis() + 500);
        }

        void cancel() {
            if (!cancelled) {
                removeCallbacks(Blink.this);
                cancelled = true;
            }
        }

        void uncancel() {
            cancelled = false;
        }
    }

}

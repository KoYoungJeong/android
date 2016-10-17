package com.tosslab.jandi.app.ui.passcode;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.passcode.adapter.PassCodeAdapter;
import com.tosslab.jandi.app.ui.passcode.fingerprint.FingerprintAuthDialogFragment;
import com.tosslab.jandi.app.ui.passcode.fingerprint.FingerprintAuthDialogFragment_;
import com.tosslab.jandi.app.ui.passcode.presenter.PassCodePresenter;
import com.tosslab.jandi.app.utils.UnLockPassCodeManager;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.decoration.GridRecyclerViewDivider;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimationListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.ViewsById;

import java.util.List;

/**
 * Created by tonyjs on 15. 10. 7..
 */
@EActivity(R.layout.activity_passcode)
public class PassCodeActivity extends BaseAppCompatActivity
        implements PassCodePresenter.View, OnUnLockSuccessListener {
    public static final String KEY_CALLING_COMPONENT_NAME = "calling_component_name";

    public static final int MODE_TO_MODIFY_PASSCODE = 0;
    public static final int MODE_TO_SAVE_PASSCODE = 1;
    public static final int MODE_TO_UNLOCK = 2;

    @Extra
    int mode = MODE_TO_UNLOCK;

    @SystemService
    Vibrator vibrator;

    @Bean
    PassCodePresenter presenter;

    @ViewsById(value = {
            R.id.v_passcode_checker_1,
            R.id.v_passcode_checker_2,
            R.id.v_passcode_checker_3,
            R.id.v_passcode_checker_4
    })
    List<View> passCodeChecker;

    @ViewById(R.id.vg_passcode_checker)
    View vgPassCodeChecker;

    @ViewById(R.id.tv_passcode_title)
    TextView tvTitle;
    @ViewById(R.id.tv_passcode_subtitle)
    TextView tvSubTitle;

    @ViewById(R.id.gv_passcode)
    RecyclerView gvPassCode;
    private PassCodeAdapter adapter;

    private Animation shakeAnimation;

    private boolean hasStopped = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedUnLockPassCode(false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mode == MODE_TO_UNLOCK) {
            presenter.onDetermineUseFingerprint();
        }

        if (hasStopped) {
            presenter.clearPassCode();
        }
        hasStopped = false;
    }

    @Override
    protected void onStop() {
        super.onStop();

        hasStopped = true;

        if (shakeAnimation.hasStarted()) {
            shakeAnimation.cancel();
        }

    }

    @AfterViews
    void initViews() {
        final boolean isPassCodeSettingMode = (mode == MODE_TO_MODIFY_PASSCODE)
                || (mode == MODE_TO_SAVE_PASSCODE);

        Context baseContext = getBaseContext();
        adapter = new PassCodeAdapter(baseContext, position -> {
            if (position == PassCodeAdapter.CANCEL) {
                if (isPassCodeSettingMode) {
                    onBackPressed();
                }
            } else if (position == PassCodeAdapter.DELETE) {
                presenter.onDeletePassCode();
            } else {
                presenter.onPassCodeInput(position + 1, mode);
            }
        });

        adapter.showCancel(isPassCodeSettingMode);

        GridLayoutManager layoutManager = new GridLayoutManager(baseContext, 3);

        gvPassCode.setLayoutManager(layoutManager);
        gvPassCode.setAdapter(adapter);
        float dividerSize = baseContext.getResources().getDisplayMetrics().density * 10;
        gvPassCode.addItemDecoration(new GridRecyclerViewDivider(dividerSize));

        if (mode == MODE_TO_MODIFY_PASSCODE) {
            tvSubTitle.setText(getString(R.string.jandi_enter_new_passcode));
        }

        shakeAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);
        shakeAnimation.setAnimationListener(new SimpleEndAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                presenter.clearPassCode();
            }
        });

        presenter.setView(this);

        if (!isPassCodeSettingMode) {
            AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.InputPasscode);
        }
    }

    @Override
    public void onBackPressed() {
        if (mode == MODE_TO_UNLOCK) {
            moveTaskToBack(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void checkPassCode(int passCodeLength) {
        if (passCodeLength == 0) {
            clearPassCodeChecker();
            return;
        }

        for (int i = 0; i < passCodeChecker.size(); i++) {
            View checker = passCodeChecker.get(i);
            checker.setSelected(i <= passCodeLength - 1);
        }
    }

    @Override
    public void clearPassCodeChecker() {
        for (View checker : passCodeChecker) {
            checker.setSelected(false);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE, delay = 100)
    @Override
    public void clearPassCodeCheckerWithDelay() {
        clearPassCodeChecker();
    }

    @Override
    public void showNeedToValidateText() {
        tvTitle.setText(getString(R.string.jandi_confirm_passcode));
        tvSubTitle.setText(getString(R.string.jandi_confirm_new_passcode));
    }

    @Override
    public void showFail() {
        vibrator.vibrate(500);
        vgPassCodeChecker.startAnimation(shakeAnimation);

        if (mode == MODE_TO_SAVE_PASSCODE || mode == MODE_TO_MODIFY_PASSCODE) {
            tvTitle.setText(getString(R.string.jandi_enter_passcode));
        } else {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.InputPasscode, AnalyticsValue.Action.IncorrectPasscode);
        }
        tvSubTitle.setText(getString(R.string.jandi_incorrect_passcode));
    }

    @Override
    public void showSuccess() {
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.InputPasscode, AnalyticsValue.Action.CorrectPasscode);

        UnLockPassCodeManager.getInstance().setUnLocked(true);

        setResult(RESULT_OK);

        Intent intent = getIntent();
        Parcelable parcelableExtra = intent.getParcelableExtra(KEY_CALLING_COMPONENT_NAME);
        if (parcelableExtra != null) {
            intent.setComponent((ComponentName) parcelableExtra);
            startActivity(intent);
        }

        finish();
    }

    @Override
    public void showUnLockFromFingerprintDialog() {
        String tag = FingerprintAuthDialogFragment.class.getSimpleName();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment == null) {
            FingerprintAuthDialogFragment fragment1 = FingerprintAuthDialogFragment_.builder().build();
            fragment1.show(getSupportFragmentManager(), tag);
            fragment1.setOnFingerPrintErrorListener(() -> {
                vibrator.vibrate(500);
                vgPassCodeChecker.startAnimation(shakeAnimation);
                tvSubTitle.setText(R.string.jandi_fingerprint_cannot_do_temporary);
            });
        }
    }

    @Override
    public void onUnLockSuccess() {
        showSuccess();
    }
}

package com.tosslab.jandi.app.ui.intro;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.account.AccountHomeActivity_;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.intro.presenter.IntroActivityPresenter;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity_;
import com.tosslab.jandi.app.ui.sign.SignHomeActivity;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ApplicationUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

/**
 * Created by justinygchoi on 14. 11. 6..
 * 크게 3가지 체크가 이루어진다.
 * 1. 업데이트 해야할 최신 버전이 마켓에 업데이트되어 있으면 업데이트 안내가 뜬다.
 * 2. 자동 로그인 여부를 체크하여 이동한다.
 */
@Fullscreen
@EActivity(R.layout.activity_intro)
public class IntroActivity extends BaseAppCompatActivity implements IntroActivityPresenter.View {

    @Extra
    boolean startForInvite = false;

    @ViewById(R.id.iv_jandi_icon)
    ImageView ivJandiIcon;

    @Bean
    IntroActivityPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedUnLockPassCode(false);
        setShouldReconnectSocketService(false);
    }

    @AfterViews
    void startOn() {
        presenter.setView(this);
        presenter.checkNewVersion(getApplicationContext(), startForInvite);
        JandiPreference.setLastExecutedTime(System.currentTimeMillis());
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void moveTeamSelectActivity() {
        Intent intent = AccountHomeActivity_.intent(IntroActivity.this).get();
        startActivityWithAnimationAndFinish(intent);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void moveToMainActivity() {
        Intent intent = MainTabActivity_.intent(IntroActivity.this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK).get();
        startActivityWithAnimationAndFinish(intent);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void moveToSignHomeActivity() {
        Intent intent = new Intent(IntroActivity.this, SignHomeActivity.class);
        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivityWithAnimationAndFinish(intent);
    }

    private void startActivityWithAnimationAndFinish(final Intent intent) {
        if (ApplicationUtil.isActivityDestroyed(IntroActivity.this)) {
            return;
        }

        ivJandiIcon.animate()
                .alpha(0.0f)
                .setDuration(800)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (ApplicationUtil.isActivityDestroyed(IntroActivity.this)) {
                            return;
                        }

                        startActivity(intent);
                        finish();
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showCheckNetworkDialog() {
        AlertUtil.showConfirmDialog(IntroActivity.this,
                R.string.jandi_msg_network_offline_warn, (dialog, which) -> finish(), false);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showWarningToast(String message) {
        ColoredToast.showWarning(message);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showMaintenanceDialog() {
        AlertUtil.showConfirmDialog(IntroActivity.this,
                R.string.jandi_service_maintenance, (dialog, which) -> finish(),
                false);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showUpdateDialog() {
        AlertUtil.showConfirmDialog(IntroActivity.this, R.string.jandi_update_title,
                R.string.jandi_update_message, (dialog, which) -> {
                    final String appPackageName = getPackageName();
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                    } finally {
                        finish();   // 업데이트 안내를 확인하면 앱을 종료한다.
                    }
                },
                false);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void finishOnUiThread() {
        finish();
    }

}

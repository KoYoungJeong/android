package com.tosslab.jandi.app.ui.intro;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.account.AccountHomeActivity_;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.intro.home.MainHomeActivity;
import com.tosslab.jandi.app.ui.intro.presenter.IntroActivityPresenter;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity_;
import com.tosslab.jandi.app.utils.AlertUtil;
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

    @UiThread
    @Override
    public void moveTeamSelectActivity() {
        Animation.AnimationListener listener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ivJandiIcon.setVisibility(View.INVISIBLE);
                AccountHomeActivity_
                        .intent(IntroActivity.this)
                        .start();
                overridePendingTransition(0, 0);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        };
        fadeOutIcon(listener);

    }

    @UiThread
    @Override
    public void moveToMainActivity() {
        // Move MainActivity
        Animation.AnimationListener listener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ivJandiIcon.setVisibility(View.INVISIBLE);
                MainTabActivity_.intent(IntroActivity.this)
                        .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        .start();
                overridePendingTransition(0, 0);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        };

        fadeOutIcon(listener);
    }

    @UiThread
    @Override
    public void moveToIntroTutorialActivity() {
        // Move intro activity
        Animation.AnimationListener listener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ivJandiIcon.setVisibility(View.INVISIBLE);
                Intent intent = new Intent(IntroActivity.this, MainHomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        };

        fadeOutIcon(listener);
    }

    private void fadeOutIcon(Animation.AnimationListener listener) {
        Animation fadeoutAnim = new AlphaAnimation(1.1f, 0.0f);
        fadeoutAnim.setDuration(500);
        fadeoutAnim.setAnimationListener(listener);
        ivJandiIcon.startAnimation(fadeoutAnim);
    }

    @UiThread
    @Override
    public void showCheckNetworkDialog() {
        AlertUtil.showConfirmDialog(IntroActivity.this, R.string.jandi_cannot_connect_service_try_again, (dialog, which) -> finish(), false);
    }

    @UiThread
    @Override
    public void showWarningToast(String message) {
        ColoredToast.showWarning(message);
    }

    @UiThread
    @Override
    public void showMaintenanceDialog() {
        AlertUtil.showConfirmDialog(IntroActivity.this,
                R.string.jandi_service_maintenance, (dialog, which) -> finish(),
                false);
    }

    @UiThread
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

    @UiThread
    @Override
    public void finishOnUiThread() {
        finish();
    }

}

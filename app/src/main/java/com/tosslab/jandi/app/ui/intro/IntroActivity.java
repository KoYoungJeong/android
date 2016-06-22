package com.tosslab.jandi.app.ui.intro;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.account.AccountHomeActivity_;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.intro.dagger.DaggerIntroComponent;
import com.tosslab.jandi.app.ui.intro.dagger.IntroModule;
import com.tosslab.jandi.app.ui.intro.presenter.IntroActivityPresenter;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity_;
import com.tosslab.jandi.app.ui.sign.SignHomeActivity;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ApplicationUtil;
import com.tosslab.jandi.app.utils.JandiPreference;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by justinygchoi on 14. 11. 6..
 * 크게 3가지 체크가 이루어진다.
 * 1. 업데이트 해야할 최신 버전이 마켓에 업데이트되어 있으면 업데이트 안내가 뜬다.
 * 2. 자동 로그인 여부를 체크하여 이동한다.
 */
public class IntroActivity extends BaseAppCompatActivity implements IntroActivityPresenter.View {

    private static final String EXTRA_START_FOR_INVITE = "startForInvite";
    boolean startForInvite = false;

    @Bind(R.id.iv_jandi_icon)
    ImageView ivJandiIcon;

    @Inject
    IntroActivityPresenter presenter;

    public static void startActivity(Context context, boolean startForInvite) {
        Intent intent = new Intent(context, IntroActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(EXTRA_START_FOR_INVITE, startForInvite);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(LayoutParams.FLAG_FULLSCREEN, LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        setNeedUnLockPassCode(false);
        setShouldReconnectSocketService(false);

        ButterKnife.bind(this);
        DaggerIntroComponent.builder()
                .introModule(new IntroModule(this))
                .build()
                .inject(this);

        initExtra();

        startOn();
    }

    private void initExtra() {
        Intent intent = getIntent();
        if (intent != null) {
            startForInvite = intent.getBooleanExtra(EXTRA_START_FOR_INVITE, false);
        }
    }

    void startOn() {
        presenter.checkNewVersion(startForInvite);
        JandiPreference.setLastExecutedTime(System.currentTimeMillis());
    }

    //    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void moveTeamSelectActivity() {
        Intent intent = AccountHomeActivity_.intent(IntroActivity.this).get();
        startActivityWithAnimationAndFinish(intent);
    }

    //    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void moveToMainActivity() {
        Intent intent = MainTabActivity_.intent(IntroActivity.this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK).get();
        startActivityWithAnimationAndFinish(intent);
    }

    //    @UiThread(propagation = UiThread.Propagation.REUSE)
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

    //    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showMaintenanceDialog() {
        AlertUtil.showConfirmDialog(IntroActivity.this,
                R.string.jandi_service_maintenance, (dialog, which) -> finish(),
                false);
    }

    //    @UiThread(propagation = UiThread.Propagation.REUSE)
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

    //    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void finishOnUiThread() {
        finish();
    }

}

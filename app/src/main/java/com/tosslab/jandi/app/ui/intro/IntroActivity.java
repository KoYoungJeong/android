package com.tosslab.jandi.app.ui.intro;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.StartApiCalledEvent;
import com.tosslab.jandi.app.services.keep.KeepService;
import com.tosslab.jandi.app.services.socket.monitor.SocketServiceStarter;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.intro.dagger.DaggerIntroComponent;
import com.tosslab.jandi.app.ui.intro.dagger.IntroModule;
import com.tosslab.jandi.app.ui.intro.presenter.IntroActivityPresenter;
import com.tosslab.jandi.app.ui.sign.SignHomeActivity;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ApplicationUtil;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import rx.Completable;
import rx.android.schedulers.AndroidSchedulers;

public class IntroActivity extends BaseAppCompatActivity implements IntroActivityPresenter.View {

    private static final long ANIM_TIME = 1600;
    private static final long NO_ANIM_DELAY_TIME = 1500;
    private static final String EXTRA_START_FOR_INVITE = "startForInvite";
    private static final int ANIM_DELAY = 300;

    boolean startForInvite = false;
    @Bind(R.id.iv_jandi_icon)
    ImageView ivJandiIcon;
    @Inject
    IntroActivityPresenter presenter;
    private AnimationDrawable splashDrawable;
    private long delayStartTime;
    private boolean keepServiceRunning; // keepalive 가 동작중이기 때문에 애니메이션과 딜레이 필요없음

    public static void startActivity(Context context, boolean startForInvite) {
        Intent intent = new Intent(context, IntroActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(EXTRA_START_FOR_INVITE, startForInvite);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(LayoutParams.FLAG_FULLSCREEN, LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        keepServiceRunning = KeepService.isServiceRunning(this);
        if (keepServiceRunning) {
            setContentView(R.layout.activity_intro);
        } else {
            setContentView(R.layout.activity_intro_animation);
        }
        setNeedUnLockPassCode(false);
        setShouldReconnectSocketService(false);

        ButterKnife.bind(this);
        DaggerIntroComponent.builder()
                .introModule(new IntroModule(this))
                .build()
                .inject(this);

        initExtra();

        EventBus.getDefault().register(this);

        startOn();

        delayStartTime = System.currentTimeMillis();
        if (!keepServiceRunning) {

            splashDrawable = ((AnimationDrawable) ivJandiIcon.getDrawable());
            Completable.fromAction(Completable::complete)
                    .delay(ANIM_DELAY, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                        splashDrawable.start();
                    });
        }

    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public void onEventMainThread(StartApiCalledEvent event) {
        if (keepServiceRunning
                && !ApplicationUtil.isActivityDestroyed(this)) {
            moveMainActivityWithoutDelay();
        }
    }

    protected void moveMainActivityWithoutDelay() {
        LogUtil.d("moveMainActivityWithoutDelay()");
        overridePendingTransition(0, 0);
        startActivity(Henson.with(this)
                .gotoMainTabActivity()
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
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

    @Override
    public void moveTeamSelectActivity() {
        if (!ApplicationUtil.isActivityDestroyed(this)) {
            startActivityWithAnimationAndFinish(Henson.with(this)
                    .gotoTeamSelectListActivity()
                    .shouldRefreshAccountInfo(true)
                    .build().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
    }

    @Override
    public void moveToMainActivity(boolean needDelay) {

        if (needDelay) {
            startActivityWithAnimationAndFinish(Henson.with(this)
                    .gotoMainTabActivity()
                    .build()
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
                            | Intent.FLAG_ACTIVITY_NEW_TASK));
        } else {
            if (!ApplicationUtil.isActivityDestroyed(this)) {
                startActivity(Henson.with(this)
                        .gotoMainTabActivity()
                        .build()
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                | Intent.FLAG_ACTIVITY_NEW_TASK));
                overridePendingTransition(0, 0);
                finish();
            }
        }
    }

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

        Completable.fromAction(() -> {})
                .delay(keepServiceRunning ? NO_ANIM_DELAY_TIME + delayStartTime - System.currentTimeMillis() : ANIM_TIME + ANIM_DELAY + delayStartTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    if (!ApplicationUtil.isActivityDestroyed(IntroActivity.this)) {
                        LogUtil.d("startActivityWithAnimationAndFinish()");
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish();
                    }
                }, Throwable::printStackTrace);
    }

    @Override
    public void showMaintenanceDialog() {
        AlertUtil.showConfirmDialog(IntroActivity.this,
                R.string.jandi_service_maintenance, (dialog, which) -> finish(),
                false);
    }

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

    @Override
    public void startSocketService() {
        sendBroadcast(new Intent(SocketServiceStarter.START_SOCKET_SERVICE));
    }

}

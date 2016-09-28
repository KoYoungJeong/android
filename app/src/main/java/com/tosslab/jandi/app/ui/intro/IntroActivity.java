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
import com.tosslab.jandi.app.services.socket.monitor.SocketServiceStarter;
import com.tosslab.jandi.app.ui.account.AccountHomeActivity;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.intro.dagger.DaggerIntroComponent;
import com.tosslab.jandi.app.ui.intro.dagger.IntroModule;
import com.tosslab.jandi.app.ui.intro.presenter.IntroActivityPresenter;
import com.tosslab.jandi.app.ui.sign.SignHomeActivity;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ApplicationUtil;
import com.tosslab.jandi.app.utils.JandiPreference;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Completable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class IntroActivity extends BaseAppCompatActivity implements IntroActivityPresenter.View {

    private static final String EXTRA_START_FOR_INVITE = "startForInvite";
    private static final int ANIM_DELAY = 300;
    boolean startForInvite = false;

    @Bind(R.id.iv_jandi_icon)
    ImageView ivJandiIcon;

    @Inject
    IntroActivityPresenter presenter;
    private AnimationDrawable splashDrawable;

    private long animStartTime;

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
        setContentView(R.layout.activity_intro_animation);
        setNeedUnLockPassCode(false);
        setShouldReconnectSocketService(false);

        ButterKnife.bind(this);
        DaggerIntroComponent.builder()
                .introModule(new IntroModule(this))
                .build()
                .inject(this);

        initExtra();

        startOn();

        splashDrawable = ((AnimationDrawable) ivJandiIcon.getDrawable());
        animStartTime = System.currentTimeMillis();
        Completable.fromAction(Completable::complete)
                .delay(ANIM_DELAY, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    splashDrawable.start();
                });
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
        Intent intent = AccountHomeActivity.getActivity(IntroActivity.this, false);
        startActivity(intent);
        finish();
    }

    @Override
    public void moveToMainActivity() {

        startActivityWithAnimationAndFinish(Henson.with(this)
                .gotoMainTabActivity()
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK));
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
        if (ApplicationUtil.isActivityDestroyed(IntroActivity.this)) {
            return;
        }

        long animTime = 1600;

        Observable.just(true)
                .delay(animTime + ANIM_DELAY + animStartTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> {
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
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

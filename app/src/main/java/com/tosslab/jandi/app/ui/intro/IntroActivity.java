package com.tosslab.jandi.app.ui.intro;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
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

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;

public class IntroActivity extends BaseAppCompatActivity implements IntroActivityPresenter.View {

    private static final String EXTRA_START_FOR_INVITE = "startForInvite";
    boolean startForInvite = false;

    @Bind(R.id.iv_jandi_icon)
    ImageView ivJandiIcon;

    @Inject
    IntroActivityPresenter presenter;

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
        setContentView(R.layout.activity_intro);
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

    public void onEventMainThread(RetrieveTopicListEvent event) {
        presenter.cancelAll();

        moveToMainActivity();
    }

    @Override
    public void moveToMainActivity() {
        EventBus.getDefault().unregister(this);

        startActivity(Henson.with(this)
                .gotoMainTabActivity()
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
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
                        overridePendingTransition(0, 0);
                        finish();
                    }
                });
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

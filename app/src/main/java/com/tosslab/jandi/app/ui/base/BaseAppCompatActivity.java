package com.tosslab.jandi.app.ui.base;

import android.support.v7.app.AppCompatActivity;

import com.facebook.appevents.AppEventsLogger;
import com.tosslab.jandi.app.events.network.NetworkConnectEvent;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.utils.UnLockPassCodeManager;
import com.tosslab.jandi.app.utils.activity.ActivityHelper;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Completable;

public class BaseAppCompatActivity extends AppCompatActivity {

    private boolean shouldSetOrientation = true;
    private boolean needUnLockPassCode = true;
    private boolean shouldReconnectSocketService = true;

    public void setShouldSetOrientation(boolean shouldSetOrientation) {
        this.shouldSetOrientation = shouldSetOrientation;
    }

    public void setNeedUnLockPassCode(boolean needUnLockPassCode) {
        this.needUnLockPassCode = needUnLockPassCode;
    }

    public void setShouldReconnectSocketService(boolean shouldReconnectSocketService) {
        this.shouldReconnectSocketService = shouldReconnectSocketService;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Background 절전 모드일 때 앱이 deactive상태에서 복구시 네트워크 접속 상태임을 감지하는데 딜레이가 생겨
        // 잘못된 결과가 넘어 온다. 따라서 0.5초후에 다시한번 접속 상태임을 체크해 보고 상태가 변했다면 변했음을 알려주자.
        if (!NetworkCheckUtil.isConnected()) {
            Completable.complete()
                    .delay(500, TimeUnit.MILLISECONDS)
                    .subscribe(() -> {
                        if (NetworkCheckUtil.isConnected()) {
                            EventBus.getDefault().post(new NetworkConnectEvent(true));
                        }
                    });
        }

        AppEventsLogger.activateApp(this);

        if (shouldSetOrientation) {
            ActivityHelper.setOrientation(this);
        }

        if (needUnLockPassCode) {
            UnLockPassCodeManager.getInstance().unLockPassCodeIfNeed(this);
        }

        if (shouldReconnectSocketService && !JandiSocketService.checkSocketConnection(getApplicationContext())) {
            JandiSocketService.startServiceIfNeed(getApplicationContext());
        }
    }

    @Override
    protected void onPause() {
        AppEventsLogger.deactivateApp(this);
        super.onPause();
    }
}

package com.tosslab.jandi.app.ui.base;

import android.support.v7.app.AppCompatActivity;

import com.facebook.appevents.AppEventsLogger;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.utils.UnLockPassCodeManager;
import com.tosslab.jandi.app.utils.activity.ActivityHelper;

public class BaseAppCompatActivity extends AppCompatActivity {

    private boolean shouldSetOrientation = true;
    private boolean needUnLockPassCode = true;
    private boolean isLaidOut = false;
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
        AppEventsLogger.activateApp(this);

        if (shouldSetOrientation) {
            ActivityHelper.setOrientation(this);
        }

        if (needUnLockPassCode) {
            UnLockPassCodeManager.getInstance().unLockPassCodeIfNeed(this);
        }

        if (isLaidOut && shouldReconnectSocketService) {
            JandiSocketService.startServiceIfNeed(getApplicationContext());
        }

        isLaidOut = true;
    }

    @Override
    protected void onPause() {
        AppEventsLogger.deactivateApp(this);
        super.onPause();
    }
}

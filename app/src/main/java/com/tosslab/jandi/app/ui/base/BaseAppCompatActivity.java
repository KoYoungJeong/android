package com.tosslab.jandi.app.ui.base;

import android.support.v7.app.AppCompatActivity;

import com.facebook.appevents.AppEventsLogger;
import com.tosslab.jandi.app.utils.UnLockPassCodeManager;
import com.tosslab.jandi.app.utils.activity.ActivityHelper;

public class BaseAppCompatActivity extends AppCompatActivity {

    private boolean needUnLockPassCode = true;

    public void setNeedUnLockPassCode(boolean needUnLockPassCode) {
        this.needUnLockPassCode = needUnLockPassCode;
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);
        ActivityHelper.setOrientation(this);

        if (needUnLockPassCode) {
            UnLockPassCodeManager.getInstance().unLockPassCodeIfNeed(this);
        }
    }

    @Override
    protected void onPause() {
        AppEventsLogger.deactivateApp(this);
        super.onPause();
    }
}

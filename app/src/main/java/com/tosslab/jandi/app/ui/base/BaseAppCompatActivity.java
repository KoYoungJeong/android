package com.tosslab.jandi.app.ui.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.facebook.appevents.AppEventsLogger;
import com.tosslab.jandi.app.utils.UnLockPassCodeManager;

public class BaseAppCompatActivity extends AppCompatActivity {

    private static final String KEY_HAS_UNLOCKED = "has_unlocked";
    private boolean needUnLockPassCode = true;

    public void setNeedUnLockPassCode(boolean needUnLockPassCode) {
        this.needUnLockPassCode = needUnLockPassCode;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (needUnLockPassCode && savedInstanceState != null) {
            UnLockPassCodeManager.getInstance()
                    .setUnLocked(savedInstanceState.getBoolean(KEY_HAS_UNLOCKED, false));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_HAS_UNLOCKED, UnLockPassCodeManager.getInstance().hasUnLocked());
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppEventsLogger.activateApp(this);

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

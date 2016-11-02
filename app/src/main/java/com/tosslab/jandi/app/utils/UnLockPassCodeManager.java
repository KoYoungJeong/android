package com.tosslab.jandi.app.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.ui.passcode.PassCodeActivity;
import com.tosslab.jandi.app.ui.passcode.PassCodeActivity_;
import com.tosslab.jandi.app.utils.logger.LogUtil;

/**
 * Created by tonyjs on 15. 10. 12..
 */
public class UnLockPassCodeManager {

    public static final String TAG = UnLockPassCodeManager.class.getSimpleName();

    private static UnLockPassCodeManager sInstance;

    private boolean hasUnLocked = false;

    public static UnLockPassCodeManager getInstance() {
        if (sInstance == null) {
            LogUtil.i(TAG, "with");
            sInstance = new UnLockPassCodeManager();
        }
        return sInstance;
    }

    public void unLockPassCodeIfNeed(Activity activity) {
        String passCode = JandiPreference.getPassCode(JandiApplication.getContext());
        if (TextUtils.isEmpty(passCode)) {
            LogUtil.e(TAG, "TextUtils.isEmpty(passCode)");
            return;
        }

        if (hasUnLocked()) {
            LogUtil.e(TAG, "hasUnLocked");
            return;
        } else {
            LogUtil.d(TAG, "!hasUnLocked");
        }

        Intent intent = PassCodeActivity_.intent(activity)
                .mode(PassCodeActivity.MODE_TO_UNLOCK)
                .get();

        activity.overridePendingTransition(0, 0);
        activity.startActivity(intent);
    }

    public void unLockPassCodeFirstIfNeed(Context context, Intent intent) {
        String passCode = JandiPreference.getPassCode(JandiApplication.getContext());
        if (TextUtils.isEmpty(passCode)) {
            addFlagOfNewTask(context, intent);
            context.startActivity(intent);
            return;
        }

        if (hasUnLocked()) {
            addFlagOfNewTask(context, intent);
            context.startActivity(intent);
            return;
        }

        ComponentName componentName = intent.getComponent();

        intent.setClass(context, PassCodeActivity_.class);
        intent.putExtra(PassCodeActivity_.MODE_EXTRA, PassCodeActivity.MODE_TO_UNLOCK);
        if (context instanceof Activity) {
            intent.putExtra(PassCodeActivity.KEY_CALLING_COMPONENT_NAME, componentName);

            ((Activity) context).overridePendingTransition(0, 0);
        }

        addFlagOfNewTask(context, intent);
        context.startActivity(intent);
    }

    private void addFlagOfNewTask(Context context, Intent intent) {
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
    }

    public synchronized boolean hasUnLocked() {
        return hasUnLocked;
    }

    public synchronized void setUnLocked(boolean hasUnLocked) {
        this.hasUnLocked = hasUnLocked;
    }

}

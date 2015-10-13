package com.tosslab.jandi.app.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.ui.passcode.PassCodeActivity;
import com.tosslab.jandi.app.ui.passcode.PassCodeActivity_;

/**
 * Created by tonyjs on 15. 10. 12..
 */
public class UnLockPassCodeManager {

    private static UnLockPassCodeManager sInstance;

    private boolean isApplicationActivate = false;

    private boolean hasUnLocked = false;

    public static UnLockPassCodeManager getInstance() {
        if (sInstance == null) {
            sInstance = new UnLockPassCodeManager();
        }
        return sInstance;
    }

    public void unLockPassCodeIfNeed(Activity activity) {
        String passCode = JandiPreference.getPassCode(JandiApplication.getContext());
        if (TextUtils.isEmpty(passCode)) {
            return;
        }

        if (!isApplicationActivate) {
            return;
        }

        if (hasUnLocked) {
            return;
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
            if (!(context instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
            return;
        }

        if (hasUnLocked) {
            if (!(context instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
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

        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    public void setApplicationActivate(boolean isApplicationActivate) {
        this.isApplicationActivate = isApplicationActivate;
        if (!isApplicationActivate) {
            setUnLocked(false);
        }
    }

    public void setUnLocked(boolean hasUnLocked) {
        this.hasUnLocked = hasUnLocked;
    }
}

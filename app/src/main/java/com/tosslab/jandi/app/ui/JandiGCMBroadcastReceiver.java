package com.tosslab.jandi.app.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.utils.JandiPreference;

/**
 * Created by justinygchoi on 2014. 7. 9..
 */
public class JandiGCMBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Explicitly specify that GcmIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(),
                JandiGCMIntentService.class.getName());
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(comp)));
        sendRefreshEntities(context);
        setResultCode(Activity.RESULT_OK);
    }

    private void sendRefreshEntities(Context context) {
        Intent intent = new Intent();
        intent.setAction(JandiConstants.PUSH_REFRESH_ACTION);
        context.sendBroadcast(intent);
    }

    /**
     * 앱이 켜져 있는 동안엔 푸쉬를 받지 않습니다.
     * @param enabled
     */
    public static void enableCustomReceiver(Context context, boolean enabled) {
        ComponentName receiver = new ComponentName(context, JandiGCMBroadcastReceiver.class);
        PackageManager pm = context.getPackageManager();

        if (receiver != null && pm != null) {
            Log.d("JANDI", ">>EnableCustomReceiver()<< ATTENTION: Enabling custom receiver: " + enabled);

            if (enabled) {
                pm.setComponentEnabledSetting(receiver,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP);
            } else {
                pm.setComponentEnabledSetting(receiver,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);
            }
        }
    }
}

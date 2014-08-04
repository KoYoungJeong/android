package com.tosslab.jandi.app;

import android.app.Activity;

/**
 * Created by justinygchoi on 2014. 7. 16..
 */
public class BaseActivity extends Activity {

    @Override
    public void onResume() {
        super.onResume();
        JandiGCMBroadcastReceiver.enableCustomReceiver(this, false);
    }

    @Override
    public void onPause() {
        JandiGCMBroadcastReceiver.enableCustomReceiver(this, true);
        super.onPause();
    }

//    /**
//     * 앱이 켜져 있는 동안엔 푸쉬를 받지 않습니다.
//     * @param enabled
//     */
//    private void enableCustomReceiver(Context context, boolean enabled) {
//        ComponentName receiver = new ComponentName(context, JandiGCMBroadcastReceiver.class);
//        PackageManager pm = context.getPackageManager();
//
//        if (receiver != null && pm != null) {
//            Log.d("JANDI", ">>EnableCustomReceiver()<< ATTENTION: Enabling custom receiver: " + enabled);
//
//            if (enabled) {
//                pm.setComponentEnabledSetting(receiver,
//                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
//                        PackageManager.DONT_KILL_APP);
//            } else {
//                pm.setComponentEnabledSetting(receiver,
//                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
//                        PackageManager.DONT_KILL_APP);
//            }
//        }
//    }
}

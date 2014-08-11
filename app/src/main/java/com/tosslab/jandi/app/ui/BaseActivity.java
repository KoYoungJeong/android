package com.tosslab.jandi.app.ui;

import android.app.Activity;
import android.content.Intent;

import com.tosslab.jandi.app.JandiGCMBroadcastReceiver;
import com.tosslab.jandi.app.utils.JandiPreference;

/**
 * Created by justinygchoi on 2014. 8. 11..
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

    protected void returnToLoginActivity() {
        JandiPreference.clearMyToken(this);
        Intent intent = new Intent(this, LoginActivity_.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}

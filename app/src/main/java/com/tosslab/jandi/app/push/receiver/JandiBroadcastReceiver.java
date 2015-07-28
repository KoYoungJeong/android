package com.tosslab.jandi.app.push.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.tosslab.jandi.app.push.to.PushTO;
import com.tosslab.jandi.app.utils.JandiPreference;

/**
 * Created by justinygchoi on 14. 12. 3..
 */
public class JandiBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (TextUtils.isEmpty(JandiPreference.getRefreshToken(context))) {
            // 이전에 JANDI 를 설치하고 삭제한 경우, 해당 디바이스 ID 가 남아있어 push 가 전송될 수 있다.
            // 새로 설치하고 아직 sign-in 을 하지 않은 경우 이전 사용자에 대한 push 가 전송됨으로 이를 무시한다.
            return;
        }

        intent.setClass(context, JandiPushIntentService.class);
        context.startService(intent);
    }
}

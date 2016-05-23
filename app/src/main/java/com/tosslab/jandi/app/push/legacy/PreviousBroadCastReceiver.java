package com.tosslab.jandi.app.push.legacy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.TokenUtil;

public class PreviousBroadCastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (TextUtils.isEmpty(TokenUtil.getRefreshToken()) || JandiPreference.isParsePushRemoved()) {
            // 이전에 JANDI 를 설치하고 삭제한 경우, 해당 디바이스 ID 가 남아있어 push 가 전송될 수 있다.
            // 새로 설치하고 아직 sign-in 을 하지 않은 경우 이전 사용자에 대한 push 가 전송됨으로 이를 무시한다.
            return;
        }

        intent.setClass(context, PreviousJandiPushIntentService.class);
        context.startService(intent);
    }
}

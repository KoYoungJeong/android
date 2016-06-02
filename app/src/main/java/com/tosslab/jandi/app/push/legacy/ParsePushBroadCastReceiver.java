package com.tosslab.jandi.app.push.legacy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.tosslab.jandi.app.local.orm.repositories.PushTokenRepository;
import com.tosslab.jandi.app.network.models.PushToken;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.parse.PushUtil;

import java.util.List;

public class ParsePushBroadCastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        List<PushToken> pushTokenList = PushTokenRepository.getInstance().getPushTokenList();
        String refreshToken = TokenUtil.getRefreshToken();
        if (TextUtils.equals(action, "com.tosslab.jandi.app.Push")) {
            refreshPushIfNeed(pushTokenList, refreshToken);
        } else if (TextUtils.equals(action, Intent.ACTION_BOOT_COMPLETED)) {
            refreshPushIfNeed(pushTokenList, refreshToken);
        }
    }

    private void refreshPushIfNeed(List<PushToken> pushTokenList, String refreshToken) {
        if (!TextUtils.isEmpty(refreshToken) && pushTokenList.isEmpty()) {
            PushUtil.registPush();
        }
    }
}

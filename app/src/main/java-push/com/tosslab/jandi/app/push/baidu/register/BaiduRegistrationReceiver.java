package com.tosslab.jandi.app.push.baidu.register;

import android.content.Context;

import com.baidu.android.pushservice.PushMessageReceiver;
import com.tosslab.jandi.app.local.orm.repositories.PushTokenRepository;
import com.tosslab.jandi.app.network.models.PushToken;
import com.tosslab.jandi.app.push.PushTokenRegister;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.List;

/*
 * Push消息处理receiver。请编写您需要的回调函数， 一般来说： onBind是必须的，用来处理startWork返回值；
 *onMessage用来接收透传消息； onSetTags、onDelTags、onListTags是tag相关操作的回调；
 *onNotificationClicked在通知被点击时回调； onUnbind是stopWork接口的返回值回调

 * 返回值中的errorCode，解释如下：
 *0 - Success
 *10001 - Network Problem
 *10101  Integrate Check Error
 *30600 - Internal Server Error
 *30601 - Method Not Allowed
 *30602 - Request Params Not Valid
 *30603 - Authentication Failed
 *30604 - Quota Use Up Payment Required
 *30605 -Data Required Not Found
 *30606 - Request Time Expires Timeout
 *30607 - Channel Token Timeout
 *30608 - Bind Relation Not Found
 *30609 - Bind Number Too Many

 * 当您遇到以上返回错误时，如果解释不了您的问题，请用同一请求的返回值requestId和errorCode联系我们追查问题。
 *
 */

public class BaiduRegistrationReceiver extends PushMessageReceiver {
    public static final String TAG = BaiduRegistrationReceiver.class
            .getSimpleName();

    @Override
    public void onBind(Context context, int errorCode, String appid,
                       String userId, String channelId, String requestId) {

        LogUtil.d(TAG, "onBind() called with: errorCode = [" + errorCode + "], appid = [" + appid + "], userId = [" + userId + "], channelId = [" + channelId + "], requestId = [" + requestId + "]");
        if (errorCode == 0) {
            // channelId = push key
            PushTokenRepository.getInstance().upsertPushToken(new PushToken("baidu", channelId));
            PushTokenRegister.getInstance().updateToken();
        }
    }

    @Override
    public void onMessage(Context context, String message,
                          String customContentString) {
    }

    @Override
    public void onNotificationClicked(Context context, String title,
                                      String description, String customContentString) {
    }

    @Override
    public void onNotificationArrived(Context context, String title,
                                      String description, String customContentString) {
    }

    @Override
    public void onSetTags(Context context, int errorCode,
                          List<String> sucessTags, List<String> failTags, String requestId) {
    }

    @Override
    public void onDelTags(Context context, int errorCode,
                          List<String> sucessTags, List<String> failTags, String requestId) {
    }

    @Override
    public void onListTags(Context context, int errorCode, List<String> tags,
                           String requestId) {
    }

    @Override
    public void onUnbind(Context context, int errorCode, String requestId) {
    }


}

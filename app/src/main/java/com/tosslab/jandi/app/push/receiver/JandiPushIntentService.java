package com.tosslab.jandi.app.push.receiver;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.events.push.MessagePushEvent;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.json.JacksonMapper;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.push.monitor.PushMonitor;
import com.tosslab.jandi.app.push.queue.PushHandler;
import com.tosslab.jandi.app.push.to.BaseMessagePushInfo;
import com.tosslab.jandi.app.push.to.BasePushInfo;
import com.tosslab.jandi.app.push.to.MarkerPushInfo;
import com.tosslab.jandi.app.ui.settings.Settings;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.io.IOException;
import java.util.List;

import de.greenrobot.event.EventBus;

public class JandiPushIntentService extends IntentService {
    public static final String TAG = "JANDI.JandiPushIntentService";
    private static final String EXTRA_CONTENT = "content";

    public JandiPushIntentService() {
        super(TAG);
    }

    public static void startService(Context context, String customContent) {
        Intent intent = new Intent(context, JandiPushIntentService.class);
        intent.putExtra(EXTRA_CONTENT, customContent);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String accountId = AccountUtil.getAccountId(getApplicationContext());

        if (TextUtils.isEmpty(accountId)) {
            LogUtil.e(TAG, "Account Id is empty.");
            return;
        }

        String content = intent.getStringExtra(EXTRA_CONTENT);

        BasePushInfo basePushInfo = parsingPushTO(content);

        if (basePushInfo == null) {
            LogUtil.e(TAG, "messagePushInfo == null");
            return;
        }

        if (basePushInfo instanceof MarkerPushInfo) {
            BadgeUtils.setBadge(JandiApplication.getContext(), basePushInfo.getBadgeCount());
            return;
        }

        if (!isPushForMyAccountId(basePushInfo)) {
            LogUtil.e(TAG, "Push is not for me.");
            return;
        }

        BaseMessagePushInfo messagePushInfo = (BaseMessagePushInfo) basePushInfo;

        // writerId 가 본인 ID 면 작성자가 본인인 노티이기 때문에 무시한다.
        if (isMyEntityId(messagePushInfo.getWriterId())) {
            return;
        }

        long roomId = messagePushInfo.getRoomId();

        boolean isShowingEntity = PushMonitor.getInstance().hasEntityId(roomId);
        boolean userWantsNotification = isPushOn();

        // 해당 채팅방에 진입해 있거나 푸시 알림 설정 Off 였을 때
        if (isShowingEntity || !userWantsNotification) {
            BadgeUtils.setBadge(JandiApplication.getContext(), basePushInfo.getBadgeCount());
            postEvent(roomId, messagePushInfo.getRoomType());
            return;
        }
        PushHandler.getInstance()
                .addPushQueue(messagePushInfo);
    }

    private boolean isPushOn() {
        return PreferenceManager.getDefaultSharedPreferences(JandiPushIntentService.this)
                .getBoolean(Settings.SETTING_PUSH_AUTO_ALARM, true);
    }

    private boolean isMyEntityId(long writerId) {
        List<ResAccountInfo.UserTeam> userTeams = AccountRepository.getRepository().getAccountTeams();

        for (ResAccountInfo.UserTeam userTeam : userTeams) {
            if (userTeam.getMemberId() == writerId) {
                return true;
            }
        }
        return false;
    }

    private void postEvent(long roomId, String roomType) {
        EventBus eventBus = EventBus.getDefault();
        if (eventBus.hasSubscriberForEvent(MessagePushEvent.class)) {
            eventBus.post(new MessagePushEvent(roomId, roomType));
        }
    }

    private BasePushInfo parsingPushTO(String content) {
        if (TextUtils.isEmpty(content)) {
            LogUtil.e(TAG, "extras has not data.");
            return null;
        }

        try {
            return JacksonMapper.getInstance().getObjectMapper().readValue(content, BasePushInfo.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new BasePushInfo();
    }

    private boolean isPushForMyAccountId(BasePushInfo basePushInfo) {
        return TextUtils.equals(basePushInfo.getAccountId(), AccountRepository.getRepository().getAccountInfo().getId());
    }


}

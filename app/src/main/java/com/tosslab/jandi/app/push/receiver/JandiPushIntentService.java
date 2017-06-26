package com.tosslab.jandi.app.push.receiver;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.events.push.MessagePushEvent;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.json.JsonMapper;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.push.monitor.PushMonitor;
import com.tosslab.jandi.app.push.queue.PushHandler;
import com.tosslab.jandi.app.push.to.BaseMessagePushInfo;
import com.tosslab.jandi.app.push.to.BasePushInfo;
import com.tosslab.jandi.app.push.to.MarkerPushInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.settings.Settings;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.PushWakeLock;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observable;

public class JandiPushIntentService extends IntentService {
    public static final String TAG = "JandiPushIntentService";
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

        if (intent == null || !intent.hasExtra(EXTRA_CONTENT)) {
            LogUtil.e(TAG, "Intent or Intent.extra is empty.");
            return;
        }

        String accountUUid = AccountUtil.getAccountUUID(getApplicationContext());

        if (TextUtils.isEmpty(accountUUid)) {
            LogUtil.e(TAG, "Account Id is empty.");
            return;
        }

        String content = intent.getStringExtra(EXTRA_CONTENT);

        BasePushInfo basePushInfo = parsingPushTO(content);

        String deviceId = TokenUtil.getTokenObject().getDeviceId();

        if (!(TextUtils.equals(basePushInfo.getDeviceId(), deviceId))) {
            return;
        }

        if (basePushInfo == null) {
            LogUtil.e(TAG, "messagePushInfo == null");
            return;
        }

        if (!isPushForMyAccountId(basePushInfo)) {
            LogUtil.e(TAG, "Push is not for me.");
            return;
        }

        Date sentAt = basePushInfo.getSentAt();
        if (sentAt != null && JandiPreference.getPushLastSentAt() < sentAt.getTime()) {
            Log.e("log", basePushInfo.toString());
            JandiPreference.setPushLastSentAt(sentAt.getTime());
            BadgeUtils.setBadge(JandiApplication.getContext(), basePushInfo.getBadgeCount());
        } else {
            return;
        }

        if (basePushInfo instanceof MarkerPushInfo) {
//            if (basePushInfo.getBadgeCount() == 0) {
//                PushHandler.getInstance().removeNotificationAll();
//            }
//            // 마커가 업데이트 된 roomId 와 마지막으로 받은 푸쉬 메세지의 roomId 가 같으면 노티를 지움.
//            PushHandler.getInstance().removeNotificationIfNeed(basePushInfo.getRoomId());
            BadgeUtils.setBadge(JandiApplication.getContext(), basePushInfo.getBadgeCount());
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
        boolean isRingIng = messagePushInfo.isRingIng(); // 타 플랫폼 active && 토픽 푸쉬 on

        boolean isActive = !JandiApplication.isApplicationDeactive()
                && !JandiApplication.isPushPopupActivityActive()
                && PushWakeLock.isScreenOn(JandiApplication.getContext());

        long currentTeamId = TeamInfoLoader.getInstance().getTeamId();

        // 해당 채팅방에 진입해 있거나
        // 푸시 알림 설정 Off 이거나
        // 타 플랫폼이 active 이고 현재 플랫폼이 inactive 인 경우이거나
        // 해당 토픽 푸시 설정이 off 인 경우
        // 하지만 해당 단말기가 active 상태이고 푸쉬가 온 팀과 현재 보고 있는 팀이 같다면 패스
        if (isShowingEntity || !userWantsNotification || !isRingIng) {
            if (isActive && (currentTeamId == messagePushInfo.getTeamId())) {
            } else {
                postEvent(roomId, messagePushInfo.getRoomType());
                return;
            }
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

    public List<Team> getTeams() {
        List<Team> teams = new ArrayList<>();
        Observable.from(AccountRepository.getRepository().getAccountTeams())
                .map(Team::createTeam)
                .toList()
                .subscribe(teamList -> {
                    teams.addAll(teamList);
                });
        return teams;
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
            return JsonMapper.getInstance().getObjectMapper().readValue(content, BasePushInfo.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new BasePushInfo();
    }

    private boolean isPushForMyAccountId(BasePushInfo basePushInfo) {
        return TextUtils.equals(basePushInfo.getAccountUuid(), AccountUtil.getAccountUUID(JandiApplication.getContext()));
    }


}

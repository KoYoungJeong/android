package com.tosslab.jandi.app.push.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.koushikdutta.ion.Ion;
import com.parse.ParseInstallation;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager_;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.spring.JacksonMapper;
import com.tosslab.jandi.app.push.PushInterfaceActivity_;
import com.tosslab.jandi.app.push.monitor.PushMonitor;
import com.tosslab.jandi.app.push.to.PushTO;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.parse.ParseUpdateUtil;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by Steve SeongUg Jung on 15. 4. 10..
 */
@EBean
public class JandiPushReceiverModel {
    private static final String JSON_KEY_DATA = "com.parse.Data";
    public static final String TAG = JandiPushReceiverModel.class.getSimpleName();

    @SystemService
    AudioManager audioManager;

    public PendingIntent generatePendingIntent(Context context, int chatId, int chatType, int teamId) {
        Intent intent = new Intent(context, PushInterfaceActivity_.class);
        if (chatType >= 0 && chatId >= 0) {
            intent.putExtra(JandiConstants.EXTRA_ENTITY_ID, chatId);
            intent.putExtra(JandiConstants.EXTRA_ENTITY_TYPE, chatType);
            intent.putExtra(JandiConstants.EXTRA_IS_FROM_PUSH, true);
            intent.putExtra(JandiConstants.EXTRA_TEAM_ID, teamId);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public int getEntityType(String roomType) {
        if (TextUtils.equals(roomType, PushTO.RoomType.CHANNEL.getName())) {
            return JandiConstants.TYPE_PUBLIC_TOPIC;
        } else if (TextUtils.equals(roomType, PushTO.RoomType.PRIVATE_GROUP.getName())) {
            return JandiConstants.TYPE_PRIVATE_TOPIC;
        } else if (TextUtils.equals(roomType, PushTO.RoomType.CHAT.getName())) {
            return JandiConstants.TYPE_DIRECT_MESSAGE;
        } else {
            return -1;
        }
    }

    public ResLeftSideMenu getTeamInfo(int teamId) {
        ResLeftSideMenu resLeftSideMenu = null;
        try {
            resLeftSideMenu = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(teamId);
            LeftSideMenuRepository.getRepository().upsertLeftSideMenu(resLeftSideMenu);
        } catch (RetrofitError retrofitError) {
            retrofitError.printStackTrace();
        }
        return resLeftSideMenu;
    }

    public boolean isTopicPushOn(ResLeftSideMenu leftSideMenu, int roomId) {
        boolean isTopicPushOn = true;

        ResLeftSideMenu.User user = leftSideMenu.user;
        List<ResLeftSideMenu.MessageMarker> markers = user.u_messageMarkers;
        for (int i = 0; i < markers.size(); i++) {
            ResLeftSideMenu.MessageMarker messageMarker = markers.get(i);
            if (messageMarker.entityId == roomId) {
                isTopicPushOn = messageMarker.subscribe;
                break;
            }
        }

        return isTopicPushOn;
    }

    public void updateBadgeCount(Context context, int badgeCount) {
        LogUtil.e(TAG, "badgeCount - " + badgeCount);
        BadgeUtils.setBadge(context, badgeCount);
        JandiPreference.setBadgeCount(context, badgeCount);
    }

    public PushTO parsingPushTO(Bundle extras) {
        if (extras == null || !extras.containsKey(JSON_KEY_DATA)) {
            LogUtil.e(TAG, "extras has not data.");
            return null;
        }

        LogUtil.i(TAG, "extras data >");
        LogUtil.d(TAG, extras.toString());
        LogUtil.i(TAG, "< extras data");

        try {
            String jsonData = extras.getString(JSON_KEY_DATA);
            LogUtil.e(TAG, jsonData);
            ObjectMapper mapper = JacksonMapper.getInstance().getObjectMapper();
            return mapper.readValue(jsonData, PushTO.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean isMyEntityId(int writerId) {
        List<ResAccountInfo.UserTeam> userTeams = AccountRepository.getRepository().getAccountTeams();

        for (ResAccountInfo.UserTeam userTeam : userTeams) {
            if (userTeam.getMemberId() == writerId) {
                return true;
            }
        }
        return false;
    }

    public boolean isMentionToMe(List<PushTO.Mention> mentions, ResLeftSideMenu leftSideMenu) {
        boolean isMentionToMe = false;
        if (mentions == null || mentions.isEmpty()) {
            return isMentionToMe;
        }

        int myTeamMemberId = leftSideMenu.user.id;
        List<ResLeftSideMenu.Entity> joinEntities = leftSideMenu.joinEntities;

        logJoinEntities(joinEntities);

        for (PushTO.Mention mention : mentions) {
            int entityId = mention.getId();
            String mentionType = mention.getType();
            if ("room".equals(mentionType)) {
                if (amIJoined(joinEntities, entityId)) {
                    isMentionToMe = true;
                    break;
                }
            } else {
                if (myTeamMemberId == entityId) {
                    isMentionToMe = true;
                    break;
                }
            }
        }

        return isMentionToMe;
    }

    private boolean amIJoined(List<ResLeftSideMenu.Entity> joinEntities, int mentionedEntityId) {
        if (joinEntities != null && !joinEntities.isEmpty()) {
            for (ResLeftSideMenu.Entity joinEntity : joinEntities) {
                if (joinEntity.id == mentionedEntityId) {
                    LogUtil.d(TAG, "I am joined topic.");
                    return true;
                }
            }
        }

        return false;
    }

    private void logJoinEntities(List<ResLeftSideMenu.Entity> joinEntities) {

        if (joinEntities == null) {
            LogUtil.e(TAG, "joinEntities == null");
        } else {
            for (ResLeftSideMenu.Entity joinEntity : joinEntities) {
                LogUtil.d(TAG, "topic joinEntityId = " + joinEntity.id);
            }
        }
    }

    public boolean isPushFromSelectedTeam(Context context, int teamId) {
        return teamId == EntityClientManager_.getInstance_(context).getSelectedTeamId();
    }

    public boolean isPushOn() {
        if (ParseUpdateUtil.PARSE_ACTIVATION_OFF.equals(
                ParseInstallation.getCurrentInstallation().getString(ParseUpdateUtil.PARSE_ACTIVATION))) {
            return false;
        }

        return true;
    }

    private Notification getNotification(Context context,
                                         String notificationTitle,
                                         int teamId, int roomId, String roomType, String roomName,
                                         String message, Bitmap writerProfile,
                                         int badgeCount) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(notificationTitle);
        builder.setContentText(message);

        int roomTypeInt = getEntityType(roomType);
        if (roomTypeInt == JandiConstants.TYPE_DIRECT_MESSAGE) {
            roomName = context.getString(R.string.jandi_tab_direct_message);
        }

        builder.setStyle(getBigTextStyle(notificationTitle, message, roomName));

        int led = 0;

        if (JandiPreference.isAlarmLED(context)) {
            led = Notification.DEFAULT_LIGHTS;
        }

        int sound = 0;

        if (JandiPreference.isAlarmSound(context)) {
            if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                sound = Notification.DEFAULT_SOUND;
            }
        }

        int vibrate = 0;

        if (JandiPreference.isAlarmVibrate(context)) {
            vibrate = Notification.DEFAULT_VIBRATE;
        }

        builder.setDefaults(led | sound | vibrate);
        builder.setSmallIcon(R.drawable.jandi_icon_push_notification);
        builder.setPriority(Notification.PRIORITY_HIGH);
        builder.setAutoCancel(true);
        builder.setNumber(badgeCount);

        // 노티를 터치할 경우엔 자동 삭제되나, 노티를 삭제하지 않고 앱으로 진입했을 때,
        // 해당 채팅 방에 들어갈 때만 이 노티가 삭제되도록...
        JandiPreference.setChatIdFromPush(context, roomId);

        // 노티를 터치할 경우 실행 intent 설정
        PendingIntent pendingIntent = generatePendingIntent(context, roomId, roomTypeInt, teamId);
        builder.setContentIntent(pendingIntent);

        if (writerProfile != null) {    // 작성자의 프로필 사진
            builder.setLargeIcon(writerProfile);
        }

        return builder.build();
    }

    private NotificationCompat.BigTextStyle getBigTextStyle(String title, String message, String summary) {
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title);
        bigTextStyle.bigText(message);
        bigTextStyle.setSummaryText(summary);
        return bigTextStyle;
    }

    public void showNotification(Context context, PushTO.PushInfo pushInfo,
                                 boolean isMentionMessage, int badgeCount) {
        String createdAt = pushInfo.getCreatedAt();
        if (isPreviousMessage(createdAt)) {
            return;
        }

        PushMonitor.getInstance().setLastNotifiedCreatedAt(createdAt);

        String message = pushInfo.getMessageContent();
        String writerName = pushInfo.getWriterName();
        String roomName = pushInfo.getRoomName();

        int teamId = pushInfo.getTeamId();
        int roomId = pushInfo.getRoomId();
        String roomType = pushInfo.getRoomType();
        String writerThumb = pushInfo.getWriterThumb();
        Bitmap profileImage = null;
        if (writerThumb != null) {
            try {
                profileImage = Ion.with(context)
                        .load(JandiConstantsForFlavors.SERVICE_ROOT_URL + writerThumb)
                        .asBitmap()
                        .get();
            } catch (Exception e) {
            }
        }

        String notificationTitle = writerName;
        if (isMentionMessage) {
            notificationTitle =
                    context.getResources().getString(R.string.jandi_mention_push_message, writerName);
        }

        Notification notification =
                getNotification(context, notificationTitle,
                        teamId, roomId, roomType, roomName,
                        message, profileImage,
                        badgeCount);

        sendNotification(context, notification);
    }

    private boolean isPreviousMessage(String createdAt) {
        if (TextUtils.isEmpty(createdAt)) {
            LogUtil.e(TAG, "createdAt is empty string.");
            return false;
        }
        LogUtil.d(TAG, createdAt);
        long createdAtTime = DateTransformator.getTimeFromISO(createdAt);

        String lastNotifiedCreatedAt = PushMonitor.getInstance().getLastNotifiedCreatedAt();
        if (!TextUtils.isEmpty(lastNotifiedCreatedAt)) {
            LogUtil.i(TAG, lastNotifiedCreatedAt);
            long preCreatedAtTime = DateTransformator.getTimeFromISO(lastNotifiedCreatedAt);
            if (createdAtTime < preCreatedAtTime) {
                LogUtil.i(TAG, "createdAtTime < preCreatedAtTime");
                return true;
            }
        }
        return false;
    }

    void sendNotification(Context context, Notification notification) {
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notification != null) {
            nm.cancel(JandiConstants.NOTIFICATION_ID);
            nm.notify(JandiConstants.NOTIFICATION_ID, notification);
        }
    }
}

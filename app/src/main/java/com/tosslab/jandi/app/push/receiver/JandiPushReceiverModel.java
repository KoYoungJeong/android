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
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.koushikdutta.ion.Ion;
import com.parse.ParseInstallation;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.BadgeCountRepository;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.json.JacksonMapper;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.push.PushInterfaceActivity_;
import com.tosslab.jandi.app.push.monitor.PushMonitor;
import com.tosslab.jandi.app.push.to.PushTO;
import com.tosslab.jandi.app.spannable.SpannableLookUp;
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
    public static final String TAG = JandiPushReceiverModel.class.getSimpleName();
    private static final String JSON_KEY_DATA = "com.parse.Data";
    private static final String JSON_KEY_CHANNEL = "com.parse.Channel";
    private static final int PENDING_INTENT_REQUEST_CODE = 20140626;
    @SystemService
    AudioManager audioManager;

    public PendingIntent generatePendingIntent(Context context, int chatId, int chatType, int teamId, String roomType) {

        PushInterfaceActivity_.IntentBuilder_ intentBuilder = PushInterfaceActivity_.intent(context);
        intentBuilder.flags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        if (chatType >= 0 && chatId >= 0) {
            intentBuilder.entityId(chatId)
                    .entityType(chatType)
                    .isFromPush(true)
                    .teamId(teamId)
                    .roomType(roomType);
        }

        Intent intent = intentBuilder.get();

        // 노티피케이션은 해제 됐지만 PendingIntent 가 살아있는 경우가 있어 cancel 을 호출해줌.
        PendingIntent.getActivity(context,
                PENDING_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                .cancel();

        return PendingIntent.getActivity(context,
                PENDING_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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

    public int getBadgeCount(int teamId) {
        return BadgeCountRepository.getRepository().findBadgeCountByTeamId(teamId);
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

    public void updateBadgeCount(Context context, int teamId, int badgeCount) {
        BadgeCountRepository repository = BadgeCountRepository.getRepository();
        repository.upsertBadgeCount(teamId, badgeCount);
        BadgeUtils.setBadge(context, repository.getTotalBadgeCount());
    }

    public boolean isPushForMyAccountId(Bundle extras, String accountId) {
        if (extras != null && extras.containsKey(JSON_KEY_CHANNEL)) {
            String value = extras.getString(JSON_KEY_CHANNEL);
            if (!TextUtils.isEmpty(value)) {
                LogUtil.d(TAG, value);
                return value.contains(accountId);
            } else {
                LogUtil.e(TAG, "Channel data is empty.");
            }
        }
        return false;
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

    public void convertPlainMarkdownContent(Context context, PushTO pushTO) {
        String content = pushTO.getInfo().getMessageContent();
        SpannableStringBuilder contentWrapper = new SpannableStringBuilder(content);
        SpannableLookUp.text(contentWrapper)
                .markdown(true)
                .lookUp(context);
        pushTO.getInfo().setMessageContent(contentWrapper.toString());
    }

    public ResLeftSideMenu getLeftSideMenuFromDB(int teamId) {
        return LeftSideMenuRepository.getRepository().findLeftSideMenuByTeamId(teamId);
    }

    public ResLeftSideMenu getLeftSideMenuFromServer(int teamId) {
        ResLeftSideMenu leftSideMenu = null;
        try {
            leftSideMenu = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(teamId);
        } catch (RetrofitError e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
        return leftSideMenu;
    }

    public void upsertLeftSideMenu(ResLeftSideMenu leftSideMenu) {
        LeftSideMenuRepository.getRepository().upsertLeftSideMenu(leftSideMenu);
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
            return false;
        }

        long myTeamMemberId = leftSideMenu.user.id;
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

    public boolean isPushOn() {
        return !ParseUpdateUtil.PARSE_ACTIVATION_OFF.equals(
                ParseInstallation.getCurrentInstallation().getString(ParseUpdateUtil.PARSE_ACTIVATION));

    }

    private NotificationCompat.Builder getNotification(Context context,
                                                       String notificationTitle,
                                                       int teamId, int roomId, String roomType, int roomTypeInt, String roomName,
                                                       String message, Bitmap writerProfile,
                                                       int badgeCount) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(notificationTitle);

        builder.setDefaults(getNotificationDefaults(context))
                .setSmallIcon(R.drawable.icon_push_notification)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setSubText(roomName)
                .setContentTitle(notificationTitle)
                .setContentText(message)
                .setNumber(badgeCount)
                .setStyle(getBigTextStyle(notificationTitle, message, roomName));

        // 노티를 터치할 경우엔 자동 삭제되나, 노티를 삭제하지 않고 앱으로 진입했을 때,
        // 해당 채팅 방에 들어갈 때만 이 노티가 삭제되도록...
        JandiPreference.setChatIdFromPush(context, roomId);

        // 노티를 터치할 경우 실행 intent 설정
        PendingIntent pendingIntent = generatePendingIntent(context, roomId, roomTypeInt, teamId, roomType);
        builder.setContentIntent(pendingIntent);

        if (writerProfile != null) {    // 작성자의 프로필 사진
            builder.setLargeIcon(writerProfile);
        }

        return builder;
    }

    private int getNotificationDefaults(Context context) {
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

        return led | sound | vibrate;
    }

    private NotificationCompat.BigTextStyle getBigTextStyle(String title, String message, String summary) {
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title)
                .bigText(message)
                .setSummaryText(summary);
        return bigTextStyle;
    }

    public void showNotification(Context context, PushTO.PushInfo pushInfo, boolean isMentionMessage) {
        String createdAt = pushInfo.getCreatedAt();
        if (isPreviousMessage(createdAt)) {

            NotificationCompat.Builder lastNotificationBuilder = PushMonitor.getInstance().getLastNotificationBuilder();
            if (lastNotificationBuilder != null) {
                int badgeCount = BadgeCountRepository.getRepository().getTotalBadgeCount();
                lastNotificationBuilder.setDefaults(0);
                lastNotificationBuilder.setNumber(badgeCount);
                sendNotification(context, lastNotificationBuilder.build());
            }

            return;
        }

        PushMonitor.getInstance().setLastNotifiedCreatedAt(createdAt);

        String writerName = pushInfo.getWriterName();

        int teamId = pushInfo.getTeamId();
        int roomId = pushInfo.getRoomId();
        String writerThumb = pushInfo.getWriterThumb();
        Bitmap profileImage = null;
        if (!TextUtils.isEmpty(writerThumb)) {
            try {
                profileImage = Ion.with(context)
                        .load(writerThumb)
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

        int badgeCount = BadgeCountRepository.getRepository().getTotalBadgeCount();

        String roomType = pushInfo.getRoomType();
        int roomTypeInt = getEntityType(roomType);
        String roomName = getRoomName(context, pushInfo, roomTypeInt);

        String message = pushInfo.getMessageContent();
        String outMessage = getOutMessage(roomTypeInt, message);


        NotificationCompat.Builder notificationBuilder =
                getNotification(context, notificationTitle,
                        teamId, roomId, roomType, roomTypeInt, roomName,
                        outMessage, profileImage,
                        badgeCount);
        PushMonitor.getInstance().setLastNotificationBuilder(notificationBuilder);

        sendNotification(context, notificationBuilder.build());
    }

    private String getRoomName(Context context, PushTO.PushInfo pushInfo, int roomTypeInt) {
        String roomName;
        if (roomTypeInt == JandiConstants.TYPE_DIRECT_MESSAGE) {
            roomName = context.getString(R.string.jandi_tab_direct_message);
        } else {
            roomName = pushInfo.getRoomName();
        }
        return roomName;
    }

    private String getOutMessage(int roomTypeInt, String message) {
        String pushPreviewInfo = JandiPreference.getPushPreviewInfo();
        String outMessage;
        if (pushPreviewInfo.equals(JandiPreference.PREF_VALUE_PUSH_PREVIEW_ALL_MESSAGE)) {
            outMessage = message;
        } else if (pushPreviewInfo.equals(JandiPreference.PREF_VALUE_PUSH_PREVIEW_PUBLIC_ONLY)) {
            if (roomTypeInt == JandiConstants.TYPE_PUBLIC_TOPIC) {
                outMessage = message;
            } else {
                outMessage = JandiApplication.getContext().getString(R.string.jandi_no_preview_push_message);
            }
        } else if (pushPreviewInfo.equals(JandiPreference.PREF_VALUE_PUSH_NO_PREVIEW)) {
            outMessage = JandiApplication.getContext().getString(R.string.jandi_no_preview_push_message);
        } else {
            outMessage = message;
        }

        return outMessage;
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

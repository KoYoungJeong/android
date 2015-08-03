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
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.EntityClientManager_;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.spring.JacksonMapper;
import com.tosslab.jandi.app.push.PushInterfaceActivity_;
import com.tosslab.jandi.app.push.to.PushTO;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.parse.ParseUpdateUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 4. 10..
 */
@EBean
public class JandiPushReceiverModel {
    public static final long NONE_CREATED_AT = -1;
    public static final String JSON_KEY_DATA = "com.parse.Data";

    @SystemService
    AudioManager audioManager;

    // 이전 Push Message 작성 시간
    private long preCreatedAt = NONE_CREATED_AT;

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
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

//        pendingIntent.cancel();

        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public int getEntityType(PushTO.PushInfo pushInfo) {
        String entityType = pushInfo.getRoomType();

        if (TextUtils.equals(entityType, PushTO.RoomType.CHANNEL.getName())) {
            return JandiConstants.TYPE_PUBLIC_TOPIC;
        } else if (TextUtils.equals(entityType, PushTO.RoomType.PRIVATE_GROUP.getName())) {
            return JandiConstants.TYPE_PRIVATE_TOPIC;
        } else if (TextUtils.equals(entityType, PushTO.RoomType.CHAT.getName())) {
            return JandiConstants.TYPE_DIRECT_MESSAGE;
        } else {
            return -1;
        }
    }

    public void updateEntityAndBadge(Context context, int unreadCountFromPush) {
        EntityClientManager jandiEntityClient = EntityClientManager_.getInstance_(context);
        ResLeftSideMenu resLeftSideMenu = jandiEntityClient.getTotalEntitiesInfo();
        JandiEntityDatabaseManager.getInstance(context).upsertLeftSideMenu(resLeftSideMenu);
        EntityManager.getInstance(context).refreshEntity(resLeftSideMenu);

//        int totalUnreadCount = BadgeUtils.getTotalUnreadCount(resLeftSideMenu);
        LogUtil.e(JandiPushReceiverModel.class.getSimpleName(), "totalUnreadCount - " + unreadCountFromPush);
        BadgeUtils.setBadge(context, unreadCountFromPush);
        JandiPreference.setBadgeCount(context, unreadCountFromPush);
    }

    public PushTO parsingPushTO(Bundle extras) {

        if (extras == null || !extras.containsKey(JSON_KEY_DATA)) {
            return null;
        }

        try {
            String jsonData = extras.getString(JSON_KEY_DATA);
            ObjectMapper mapper = JacksonMapper.getInstance().getObjectMapper();
            return mapper.readValue(jsonData, PushTO.class);
        } catch (IOException e) {
            return null;
        }

    }

    public boolean isMyEntityId(Context context, int writerId) {
        List<ResAccountInfo.UserTeam> userTeams = JandiAccountDatabaseManager.getInstance(context).getUserTeams();

        for (ResAccountInfo.UserTeam userTeam : userTeams) {
            if (userTeam.getMemberId() == writerId) {
                return true;
            }
        }
        return false;
    }

    private Notification generateNotification(Context context, PushTO.PushInfo pushInfo) {
        return generateNotification(context, pushInfo, null);
    }

    private Notification generateNotification(Context context, PushTO.PushInfo pushInfo, Bitmap writerProfile) {
        String message = pushInfo.getMessageContent();
        String chatName = pushInfo.getRoomName();
        String writerName = pushInfo.getWriterName();

        int chatId = pushInfo.getRoomId();
        int chatType = getEntityType(pushInfo);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(writerName);
        builder.setContentText(message);
        if (chatType == JandiConstants.TYPE_DIRECT_MESSAGE) {
            chatName = context.getString(R.string.jandi_tab_direct_message);
        }
        builder.setStyle(getBigTextStyle(writerName, message, chatName));

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
        builder.setNumber(JandiPreference.getBadgeCount(context));

        // 노티를 터치할 경우엔 자동 삭제되나, 노티를 삭제하지 않고 앱으로 진입했을 때, 해당 채팅 방에 들어갈 때만
        // 이 노티가 삭제되도록...
        JandiPreference.setChatIdFromPush(context, chatId);

        // 노티를 터치할 경우 실행 intent 설정
        PendingIntent pendingIntent = generatePendingIntent(context, chatId, chatType, pushInfo.getTeamId());
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

    public boolean isPushOn() {
        if (ParseUpdateUtil.PARSE_ACTIVATION_OFF.equals(
                ParseInstallation
                        .getCurrentInstallation()
                        .getString(ParseUpdateUtil.PARSE_ACTIVATION))) {
            return false;
        }

        return true;
    }

    public void sendNotificationWithProfile(final Context context, final PushTO.PushInfo pushInfo) {
        // 이전 푸쉬 메세지가 현재 푸쉬 메세지보다 더 최근에 작성되었다면 무시.
        String createdAt = pushInfo.getCreatedAt();
        long createdAtTime = DateTransformator.getTimeFromISO(createdAt);
        if (preCreatedAt != NONE_CREATED_AT) {
            if (createdAtTime < preCreatedAt) {
                return;
            }
        }
        preCreatedAt = createdAtTime;

        String writerProfile = pushInfo.getWriterThumb();
        Notification notification;
        if (writerProfile != null) {
            Bitmap bitmap = null;
            try {
                bitmap = Ion.with(context)
                        .load(JandiConstantsForFlavors.SERVICE_ROOT_URL + writerProfile)
                        .asBitmap()
                        .get();
            } catch (Exception e) {
            }

            notification = generateNotification(context, pushInfo, bitmap);
        } else {
            notification = generateNotification(context, pushInfo);
        }

        sendNotification(context, notification);
    }

    void sendNotification(Context context, Notification notification) {
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notification != null) {
            nm.notify(JandiConstants.NOTIFICATION_ID, notification);
        }
    }
}

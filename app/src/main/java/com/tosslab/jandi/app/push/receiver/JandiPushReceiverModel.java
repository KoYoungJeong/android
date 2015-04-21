package com.tosslab.jandi.app.push.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.koushikdutta.ion.Ion;
import com.parse.ParseInstallation;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.client.JandiEntityClient;
import com.tosslab.jandi.app.network.client.JandiEntityClient_;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.spring.JacksonMapper;
import com.tosslab.jandi.app.push.PushInterfaceActivity_;
import com.tosslab.jandi.app.push.to.PushTO;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 4. 10..
 */
@EBean
public class JandiPushReceiverModel {

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

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        pendingIntent.cancel();

        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public int getEntityType(PushTO.MessagePush messagePush) {
        String entityType = messagePush.getChatType();

        if (entityType.equals("channel")) {
            return JandiConstants.TYPE_PUBLIC_TOPIC;
        } else if (entityType.equals("privateGroup")) {
            return JandiConstants.TYPE_PRIVATE_TOPIC;
        } else if (entityType.equals("user")) {
            return JandiConstants.TYPE_DIRECT_MESSAGE;
        } else {
            return -1;
        }
    }

    @Background
    public void updateEntityAndBadge(Context context) {

        try {
            JandiEntityClient jandiEntityClient = JandiEntityClient_.getInstance_(context);
            ResLeftSideMenu resLeftSideMenu = jandiEntityClient.getTotalEntitiesInfo();
            JandiEntityDatabaseManager.getInstance(context).upsertLeftSideMenu(resLeftSideMenu);
            int totalUnreadCount = BadgeUtils.getTotalUnreadCount(resLeftSideMenu);
            BadgeUtils.setBadge(context, totalUnreadCount);
            JandiPreference.setBadgeCount(context, totalUnreadCount);
            EntityManager.getInstance(context).refreshEntity(resLeftSideMenu);
        } catch (JandiNetworkException e) {
            e.printStackTrace();
        }
    }

    public PushTO parsingPushTO(Bundle extras) {

        if (extras == null || !extras.containsKey(JandiBroadcastReceiver.JSON_KEY_DATA)) {
            return null;
        }

        try {
            String jsonData = extras.getString(JandiBroadcastReceiver.JSON_KEY_DATA);
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

    private Notification generateNotification(Context context, PushTO.MessagePush messagePush) {
        return generateNotification(context, messagePush, null);
    }

    private Notification generateNotification(Context context, PushTO.MessagePush messagePush, Bitmap writerProfile) {
        String message = messagePush.getAlert();
        String chatName = messagePush.getChatName();
        String writerName = messagePush.getWriterName();

        int chatId = messagePush.getChatId();
        int chatType = getEntityType(messagePush);

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
            sound = Notification.DEFAULT_SOUND;
        }

        int vibrate = 0;

        if (JandiPreference.isAlarmVibrate(context)) {
            vibrate = Notification.DEFAULT_VIBRATE;
        }

        builder.setDefaults(led | sound | vibrate);
        builder.setSmallIcon(R.drawable.jandi_icon_push_notification);
        builder.setPriority(Notification.PRIORITY_MAX);
        builder.setAutoCancel(true);

        // 노티를 터치할 경우엔 자동 삭제되나, 노티를 삭제하지 않고 앱으로 진입했을 때, 해당 채팅 방에 들어갈 때만
        // 이 노티가 삭제되도록...
        JandiPreference.setChatIdFromPush(context, chatId);

        // 노티를 터치할 경우 실행 intent 설정
        PendingIntent pendingIntent = generatePendingIntent(context, chatId, chatType, messagePush.getTeamId());
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
        if (JandiConstants.PARSE_ACTIVATION_OFF.equals(
                ParseInstallation
                        .getCurrentInstallation()
                        .getString(JandiConstants.PARSE_ACTIVATION))) {
            return false;
        }

        return true;
    }

    @Background
    public void sendNotificationWithProfile(final Context context, final PushTO.MessagePush messagePush) {
        // 현재 디바이스 설정이 push off 라면 무시
        String writerProfile = messagePush.getWriterThumb();
        if (writerProfile != null) {
            Bitmap bitmap = null;
            try {
                bitmap = Ion.with(context)
                        .load(JandiConstantsForFlavors.SERVICE_ROOT_URL + writerProfile)
                        .asBitmap()
                        .get();
            } catch (Exception e) {
            }

            Notification notification;
            if (writerProfile != null) {
                notification = generateNotification(context, messagePush, bitmap);
            } else {
                notification = generateNotification(context, messagePush);
            }

            sendNotification(context, notification);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void sendNotification(Context context, Notification notification) {
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notification != null) {
            nm.notify(JandiConstants.NOTIFICATION_ID, notification);
        }
    }
}
package com.tosslab.jandi.app.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.push.MessagePushEvent;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.push.monitor.PushMonitor;
import com.tosslab.jandi.app.push.to.PushTO;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 14. 12. 3..
 */
public class JandiBroadcastReceiver extends BroadcastReceiver {
    private static final String JSON_KEY_DATA = "com.parse.Data";

    private static final String JSON_VALUE_TYPE_PUSH = "push";
    private static final String JSON_VALUE_TYPE_SUBSCRIBE = "subscribe";
    private static final String JSON_VALUE_TYPE_UNSUBSCRIBE = "unsubscribe";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (TextUtils.isEmpty(JandiPreference.getRefreshToken(context))) {
            // 이전에 JANDI 를 설치하고 삭제한 경우, 해당 디바이스 ID 가 남아있어 push 가 전송될 수 있다.
            // 새로 설치하고 아직 sign-in 을 하지 않은 경우 이전 사용자에 대한 push 가 전송됨으로 이를 무시한다.
            return;
        }

        Bundle extras = intent.getExtras();
        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            String jsonData = extras.getString(JSON_KEY_DATA);
            try {
                ObjectMapper mapper = new ObjectMapper();
                PushTO pushTO = mapper.readValue(jsonData, PushTO.class);
                if (pushTO == null) {
                    return;
                }
                String type = pushTO.getType();
                PushTO.PushInfo pushTOInfo = pushTO.getInfo();

                // writerId 가 본인 ID 면 작성자가 본인인 노티이기 때문에 무시한다.
                if (type.equals(JSON_VALUE_TYPE_PUSH)) {
                    PushTO.MessagePush messagePush = (PushTO.MessagePush) pushTOInfo;
                    // is From My Message???
                    if (isMyEntityId(context, messagePush.getWriterId())) {
                        return;
                    }

                    // I'm seeing same Entity???
                    if (!PushMonitor.getInstance().hasEntityId(messagePush.getChatId())) {
                        sendNotificationWithProfile(context, messagePush);
                    }

                    // Update count of badge
                    int count = recalculateBadgeCount(context);
                    JandiPreference.setBadgeCount(context, count);
                    BadgeUtils.setBadge(context, count);

                    EventBus eventBus = EventBus.getDefault();
                    if (eventBus.hasSubscriberForEvent(MessagePushEvent.class)) {
                        eventBus.post(new MessagePushEvent(messagePush.getChatId(), messagePush.getChatType()));
                    }
                } else if (type.equals(JSON_VALUE_TYPE_SUBSCRIBE)) {
                    PushTO.SubscribePush subscribePush = (PushTO.SubscribePush) pushTOInfo;
                    subscribeTopic(subscribePush.getChatId());
                } else if (type.equals(JSON_VALUE_TYPE_UNSUBSCRIBE)) {
                    PushTO.UnSubscribePush unSubscribePush = (PushTO.UnSubscribePush) pushTOInfo;
                    unsubscribeTopic(unSubscribePush.getChatId());
                } else {
                    // DO NOTHING
                }

            } catch (IOException e) {
                // Push 관련 parsing 이 실패하면 push notification 을 실행하지 않는다.
                Log.e("PUSH", "Push parsing error", e);
            }
        }
        return;
    }

    private boolean isMyEntityId(Context context, int writerId) {
        List<ResAccountInfo.UserTeam> userTeams = JandiAccountDatabaseManager.getInstance(context).getUserTeams();

        for (ResAccountInfo.UserTeam userTeam : userTeams) {
            if (userTeam.getMemberId() == writerId) {
                return true;
            }
        }
        return false;
    }

    private void sendNotificationWithProfile(final Context context, final PushTO.MessagePush messagePush) {
        // 현재 JANDI client 가 chatting 중이라면 해당 채팅방에 대한 push 는 무시한다.
        int activatedChatId = JandiPreference.getActivatedChatId(context);
        int chatIdFromPush = messagePush.getChatId();
        if (activatedChatId == chatIdFromPush) {
            return;
        }

        // 현재 디바이스 설정이 push off 라면 무시
        if (JandiConstants.PARSE_ACTIVATION_OFF.equals(
                ParseInstallation
                        .getCurrentInstallation()
                        .getString(JandiConstants.PARSE_ACTIVATION))) {
            return;
        }

        String writerProfile = messagePush.getWriterThumb();
        Log.d("Profile Url", JandiConstantsForFlavors.SERVICE_ROOT_URL + writerProfile);
        if (writerProfile != null) {
            Glide.with(context)
                    .load(JandiConstantsForFlavors.SERVICE_ROOT_URL + writerProfile)
                    .into(new SimpleTarget<GlideDrawable>(200, 200) {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                            if (resource != null) {
                                Bitmap bitmap = ((GlideBitmapDrawable) resource).getBitmap();
                                sendNotification(context, messagePush, bitmap);
                            } else {
                                sendNotification(context, messagePush, null);
                            }
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            sendNotification(context, messagePush, null);
                        }
                    });
        }
    }

    private void sendNotification(final Context context, final PushTO.MessagePush messagePush, Bitmap writerProfile) {
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = generateNotification(context, messagePush, writerProfile);
        if (notification != null) {
            nm.notify(JandiConstants.NOTIFICATION_ID, notification);
        }
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
        builder.setDefaults(Notification.DEFAULT_ALL);
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

    private int getEntityType(PushTO.MessagePush messagePush) {
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

    private PendingIntent generatePendingIntent(Context context, int chatId, int chatType, int teamId) {
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

    private void subscribeTopic(String chatId) {
        ParsePush.subscribeInBackground(chatId);

    }

    private void unsubscribeTopic(String chatId) {
        ParsePush.unsubscribeInBackground(chatId);

    }

    int recalculateBadgeCount(Context context) {
        int badgeCount = JandiPreference.getBadgeCount(context);
        badgeCount++;
        return badgeCount;
    }
}

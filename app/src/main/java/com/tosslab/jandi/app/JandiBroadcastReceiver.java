package com.tosslab.jandi.app;

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
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.tosslab.jandi.app.utils.BadgeUtils;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * Created by justinygchoi on 14. 12. 3..
 */
public class JandiBroadcastReceiver extends BroadcastReceiver {
    private static final String JSON_KEY_DATA  = "com.parse.Data";
    private static final String JSON_KEY_TYPE     = "type";
    private static final String JSON_KEY_INFO       = "info";
    private static final String JSON_KEY_INFO_MESSAGE    = "alert";
    private static final String JSON_KEY_INFO_CHAT_ID    = "chatId";
    private static final String JSON_KEY_INFO_CHAT_NAME  = "chatName";
    private static final String JSON_KEY_INFO_CHAT_TYPE  = "chatType";
    private static final String JSON_KEY_INFO_WRITER_ID     = "writerId";
    private static final String JSON_KEY_INFO_WRITER_THUMB  = "writerThumb";

    private static final String JSON_VALUE_TYPE_PUSH        = "push";
    private static final String JSON_VALUE_TYPE_SUBSCRIBE   = "subscribe";
    private static final String JSON_VALUE_TYPE_UNSUBSCRIBE = "unsubscribe";

    private static final int MAX_LENGTH_SMALL_NOTIFICATION  = 20;

    @Override
    public void onReceive(Context context, Intent intent) {
        int myEntityId = JandiPreference.getMyEntityId(context);
        if (myEntityId == JandiPreference.NOT_SET_YET) {
            // 이전에 JANDI 를 설치하고 삭제한 경우, 해당 디바이스 ID 가 남아있어 push 가 전송될 수 있다.
            // 새로 설치하고 아직 sign-in 을 하지 않은 경우 이전 사용자에 대한 push 가 전송됨으로 이를 무시한다.
            return;
        }

        Bundle extras = intent.getExtras();
        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            String jsonData = extras.getString(JSON_KEY_DATA);
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode dataObj = mapper.readTree(jsonData);
                if (dataObj == null) {
                    return;
                }
                String type = getJsonTypeValue(dataObj);
                JsonNode infoObj = dataObj.get(JSON_KEY_INFO);

                // writerId 가 본인 ID 면 작성자가 본인인 노티이기 때문에 무시한다.
                int writerId = getJsonNodeIntValue(infoObj, JSON_KEY_INFO_WRITER_ID);
                if (writerId == myEntityId) {
                    return;
                }

                if (type.equals(JSON_VALUE_TYPE_PUSH)) {
                    sendNotificationWithProfile(context, infoObj);
                    // Update count of badge
                    BadgeUtils.setBadge(context, recalculateBadgeCount(context));
                } else if (type.equals(JSON_VALUE_TYPE_SUBSCRIBE)) {
                    subscribeTopic(infoObj);
                } else if (type.equals(JSON_VALUE_TYPE_UNSUBSCRIBE)) {
                    unsubscribeTopic(infoObj);
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

    private void sendNotificationWithProfile(final Context context, final JsonNode infoObj) {
        // 현재 JANDI client 가 chatting 중이라면 해당 채팅방에 대한 push 는 무시한다.
        int activatedChatId = JandiPreference.getActivatedChatId(context);
        int chatIdFromPush = getJsonNodeIntValue(infoObj, JSON_KEY_INFO_CHAT_ID);
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

        String writerProfile = getJsonNodeStringValue(infoObj, JSON_KEY_INFO_WRITER_THUMB);
        Log.d("Profile Url", JandiConstantsForFlavors.SERVICE_ROOT_URL + writerProfile);
        if (writerProfile != null) {
            Glide.with(context)
                    .load(JandiConstantsForFlavors.SERVICE_ROOT_URL + writerProfile)
                    .into(new SimpleTarget<GlideDrawable>(200, 200) {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                            if (resource != null) {
                                Bitmap bitmap = ((GlideBitmapDrawable)resource).getBitmap();
                                sendNotification(context, infoObj, bitmap);
                            } else {
                                sendNotification(context, infoObj, null);
                            }
                        }

                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            sendNotification(context, infoObj, null);
                        }
                    });
        }
    }

    private void sendNotification(final Context context, final JsonNode infoObj, Bitmap writerProfile) {
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = generateNotification(context, infoObj, writerProfile);
        if (notification != null) {
            nm.notify(JandiConstants.NOTIFICATION_ID, notification);
        }
    }

    private Notification generateNotification(Context context, JsonNode infoObj, Bitmap writerProfile) {
        String message = getJsonNodeStringValue(infoObj, JSON_KEY_INFO_MESSAGE);
        String chatName = getJsonNodeStringValue(infoObj, JSON_KEY_INFO_CHAT_NAME);

        int chatId = getJsonNodeIntValue(infoObj, JSON_KEY_INFO_CHAT_ID);
        int chatType = retrieveEntityTypeFromJsonNode(infoObj, JSON_KEY_INFO_CHAT_TYPE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(chatName);
        builder.setContentText(message);
        // 메시지 길이에 따른 노티 크기 설정
        if (message.length() > MAX_LENGTH_SMALL_NOTIFICATION) {
            builder.setStyle(getBigTextStyle(chatName, message, context.getString(R.string.app_name)));
        }
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setSmallIcon(R.drawable.jandi_icon_push_notification);
        builder.setPriority(Notification.PRIORITY_MAX);
        builder.setAutoCancel(true);

        // 노티를 터치할 경우엔 자동 삭제되나, 노티를 삭제하지 않고 앱으로 진입했을 때, 해당 채팅 방에 들어갈 때만
        // 이 노티가 삭제되도록...
        JandiPreference.setChatIdFromPush(context, chatId);

        // 노티를 터치할 경우 실행 intent 설정
        PendingIntent pendingIntent = generatePendingIntent(context, chatId, chatType);
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

    private int retrieveEntityTypeFromJsonNode(JsonNode dataObj, String key) {
        String entityType = getJsonNodeStringValue(dataObj, key);

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

    private PendingIntent generatePendingIntent(Context context, int chatId, int chatType) {
        Intent intent = new Intent(context, com.tosslab.jandi.app.ui.MessageListActivity_.class);
        if (chatType >= 0 && chatId >= 0) {
            intent.putExtra(JandiConstants.EXTRA_ENTITY_ID, chatId);
            intent.putExtra(JandiConstants.EXTRA_ENTITY_TYPE, chatType);
            intent.putExtra(JandiConstants.EXTRA_IS_FROM_PUSH, true);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private void subscribeTopic(JsonNode infoObj) {
        int chatId = getJsonNodeIntValue(infoObj, JSON_KEY_INFO_CHAT_ID);
        if (chatId > 0) {
            ParsePush.subscribeInBackground(JandiConstants.PUSH_CHANNEL_PREFIX + chatId);
        }
    }

    private void unsubscribeTopic(JsonNode infoObj) {
        int chatId = getJsonNodeIntValue(infoObj, JSON_KEY_INFO_CHAT_ID);
        if (chatId > 0) {
            ParsePush.unsubscribeInBackground(JandiConstants.PUSH_CHANNEL_PREFIX + chatId);
        }
    }

    private String getJsonTypeValue(JsonNode node) {
        return getJsonNodeStringValue(node, JSON_KEY_TYPE);
    }

    private String getJsonNodeStringValue(JsonNode node, String key) {
        JsonNode jsonNode = getJsonNodeValue(node, key);
        return (jsonNode != null) ? jsonNode.asText() : "";
    }

    private int getJsonNodeIntValue(JsonNode node, String key) {
        JsonNode jsonNode = getJsonNodeValue(node, key);
        return (jsonNode != null) ? jsonNode.asInt() : -1;
    }

    private JsonNode getJsonNodeValue(JsonNode node, String key) {
        if (node != null && node.get(key) != null) {
            return node.get(key);
        }
        return null;
    }

    int recalculateBadgeCount(Context context) {
        int badgeCount = JandiPreference.getBadgeCount(context);
        badgeCount++;
        JandiPreference.setBadgeCount(context, badgeCount);
        return badgeCount;
    }
}

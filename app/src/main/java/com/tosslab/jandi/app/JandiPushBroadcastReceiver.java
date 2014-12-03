package com.tosslab.jandi.app;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.parse.ParsePush;
import com.parse.ParsePushBroadcastReceiver;
import com.squareup.picasso.Picasso;
import com.tosslab.jandi.app.ui.MessageListActivity_;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * Created by justinygchoi on 14. 11. 27..
 */
public class JandiPushBroadcastReceiver extends ParsePushBroadcastReceiver {
    private static final String JSON_KEY_DATA  = "com.parse.Data";
    private static final String JSON_KEY_ACTION     = "action";
    private static final String JSON_KEY_INFO       = "info";
    private static final String JSON_KEY_INFO_MESSAGE       = "alert";
    private static final String JSON_KEY_INFO_CHAT_ID    = "chatId";
    private static final String JSON_KEY_INFO_CHAT_NAME  = "chatName";
    private static final String JSON_KEY_INFO_CHAT_TYPE  = "chatType";
    private static final String JSON_KEY_INFO_WRITER_ID     = "writerId";
    private static final String JSON_KEY_INFO_WRITER_THUMB  = "writerThumb";

    private static final String JSON_VALUE_ACTION_PUSH          = "push";
    private static final String JSON_VALUE_ACTION_SUBSCRIBE     = "subscribe";
    private static final String JSON_VALUE_ACTION_UNSUBSCRIBE   = "unsubscribe";

    private static final int MAX_LENGTH_SMALL_NOTIFICATION  = 20;

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            String jsonData = extras.getString(JSON_KEY_DATA);
            Log.d("HIHIHIHIHI", jsonData);
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode dataObj = mapper.readTree(jsonData);
                if (dataObj == null) {
                    return null;
                }
                JsonNode infoObj = dataObj.get(JSON_KEY_INFO);
                String action = getJsonActionValue(dataObj);
                if (action.equals(JSON_VALUE_ACTION_PUSH)) {
                    return generateNotification(context, infoObj);
                } else if (action.equals(JSON_VALUE_ACTION_SUBSCRIBE)) {
                    subscribeTopic(infoObj);
                } else if (action.equals(JSON_VALUE_ACTION_UNSUBSCRIBE)) {
                    unsubscribeTopic(infoObj);
                } else {
                    // DO NOTHING
                }

            } catch (IOException e) {
                // Push 관련 parsing 이 실패하면 push notification 을 실행하지 않는다.
                Log.e("PUSH", "Push parsing error", e);
            }
        }
        return null;
    }

//    @Override
//    protected void onPushReceive(Context context, Intent intent) {
//        sendRefreshEntities(context);
//    }
//
//    private void sendRefreshEntities(Context context) {
//        Intent intent = new Intent();
//        intent.setAction(JandiConstants.PUSH_REFRESH_ACTION);
//        context.sendBroadcast(intent);
//    }

    private Notification generateNotification(Context context, JsonNode infoObj) {
        int writerId = getJsonNodeIntValue(infoObj, JSON_KEY_INFO_WRITER_ID);
        // TODO writerId 가 본인 ID 면 작성자가 본인인 노티이기 때문에 무시한다.

        String message = getJsonNodeStringValue(infoObj, JSON_KEY_INFO_MESSAGE);
        String chatName = getJsonNodeStringValue(infoObj, JSON_KEY_INFO_CHAT_NAME);

        int chatId = getJsonNodeIntValue(infoObj, JSON_KEY_INFO_CHAT_ID);
        int chatType = retrieveEntityTypeFromJsonNode(infoObj, JSON_KEY_INFO_CHAT_TYPE);

        String writerProfile = getJsonNodeStringValue(infoObj, JSON_KEY_INFO_WRITER_THUMB);

        Notification.Builder builder = new Notification.Builder(context);
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

        // 노티를 터치할 경우 실행 intent 설정
        PendingIntent pendingIntent = genPendingIntent(context, chatId, chatType);
        builder.setContentIntent(pendingIntent);

        // 사용자 프로필 사진 설정
        // TODO in Background Thread
//        try {
//            Bitmap bitmapProfile = getBitmapWriterProfile(context, writerProfile);
//            builder.setLargeIcon(bitmapProfile);
//        } catch (IOException e) {
//            // DO NOTHING
//        }

        return builder.build();
    }

    private Notification.BigTextStyle getBigTextStyle(String title, String message, String summary) {
        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
        bigTextStyle.setBigContentTitle(title);
        bigTextStyle.bigText(message);
        bigTextStyle.setSummaryText(summary);
        return bigTextStyle;
    }

    private Bitmap getBitmapWriterProfile(Context context, String overLayUrl) throws IOException {
        if (overLayUrl != null) {
            return Picasso.with(context).load(overLayUrl).get();
        }
        return null;
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

    private PendingIntent genPendingIntent(Context context, int chatId, int chatType) {
        Intent intent = new Intent(context, MessageListActivity_.class);
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

    private String getJsonActionValue(JsonNode node) {
        return getJsonNodeStringValue(node, JSON_KEY_ACTION);
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
}

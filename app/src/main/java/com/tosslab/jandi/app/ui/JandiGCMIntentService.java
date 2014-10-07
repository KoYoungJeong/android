package com.tosslab.jandi.app.ui;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.squareup.picasso.Picasso;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Set;

/**
 * Created by justinygchoi on 2014. 7. 9..
 */
public class JandiGCMIntentService extends IntentService {
    private final Logger log = Logger.getLogger(JandiGCMIntentService.class);
    static final String TAG = "JandiCGMIntentService";

    public JandiGCMIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("ERROR", "Send error: " + extras.toString(), -1, -1, null);
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("ERROR", "Deleted messages on server: " + extras.toString(), -1, -1, null);
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                log.info("Received: " + extras.toString());
                try {
                    // 총 메시지
//                    int messageCount = extras.getInt("messageCount", 0);

                    String messages = extras.getString("messages");
                    ObjectMapper mapper = new ObjectMapper();

                    JsonNode messagesObj = mapper.readTree(messages);
                    int entityType = retrieveEntityType(messagesObj);
                    int entityId = retrieveEntityId(messagesObj);
                    String lastTitle = retrieveLastTitle(messagesObj);
                    String lastMessage = retrieveLastMessage(messagesObj);
                    String profileUrl = retrieveProfileUrl(messagesObj);

                    JandiPreference.setEntityId(getApplicationContext(), entityId);
                    // Post notification of received message.
                    sendNotification(lastTitle, lastMessage, entityType, entityId, profileUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Update count of badge
                updateBadge(1);
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        JandiGCMBroadcastReceiver.completeWakefulIntent(intent);
    }

    private String retrieveLastMessage(JsonNode messagesNode) {
        // Message text
        JsonNode messageObj = messagesNode.get("message");
        JsonNode contentObj = messageObj.get("content");
        String contentType = messageObj.get("contentType").asText();
        if (contentType.equals("text")) {
            return contentObj.get("body").asText();
        } else if (contentType.equals("file")) {
            return contentObj.get("title").asText() + " is uploaded.";
        } else if (contentType.equals("comment")) {
            return contentObj.get("body").asText();
        } else {
            return "";
        }
    }

    private int retrieveEntityType(JsonNode messagesNode) {
        JsonNode toEntity = messagesNode.get("toEntity");
        String entityType = toEntity.get("type").asText();
        if (entityType.equals("channel")) {
            return JandiConstants.TYPE_CHANNEL;
        } else if (entityType.equals("privateGroup")) {
            return JandiConstants.TYPE_PRIVATE_GROUP;
        } else if (entityType.equals("user")) {
            return JandiConstants.TYPE_DIRECT_MESSAGE;
        } else {
            return -1;
        }
    }

    private int retrieveEntityId(JsonNode messagesNode) {
        int entityType = retrieveEntityType(messagesNode);
        if (entityType == JandiConstants.TYPE_DIRECT_MESSAGE) {
            JsonNode toEntity = messagesNode.get("fromEntity");
            return toEntity.get("id").asInt();
        } else {
            JsonNode toEntity = messagesNode.get("toEntity");
            return toEntity.get("id").asInt();
        }
    }

    private String retrieveLastTitle(JsonNode messagesNode) {
        int entityType = retrieveEntityType(messagesNode);
        if (entityType == JandiConstants.TYPE_DIRECT_MESSAGE) {
            JsonNode fromEntity = messagesNode.get("fromEntity");
            return fromEntity.get("name").asText();
        } else {
            JsonNode toEntity = messagesNode.get("toEntity");
            return toEntity.get("name").asText();
        }
    }

    private String retrieveProfileUrl(JsonNode messagesNode) {
        JsonNode messageObj = messagesNode.get("message");
        JsonNode writerObj = messageObj.get("writer");
        JsonNode profileObj = writerObj.get("u_photoThumbnailUrl");
        if (profileObj != null) {
            return (profileObj.get("mediumThumbnailUrl") != null)
                    ? JandiConstants.SERVICE_ROOT_URL + profileObj.get("mediumThumbnailUrl").asText()
                    : null;
        }
        return null;
    }

    private void updateBadge(int badgeCount) {
        if (badgeCount >= 0) {
            Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
            // 패키지 네임과 클래스 네임 설정
            intent.putExtra("badge_count_package_name", getApplication().getPackageName());
            intent.putExtra("badge_count_class_name", "com.tosslab.jandi.app.ui.MainTabActivity");
            // 업데이트 카운트
            intent.putExtra("badge_count", badgeCount);
            sendBroadcast(intent);
        }
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String title, String body, int entityType, int entityId, String overLayUrl) {
        NotificationManager nm =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(getApplicationContext(), MessageListActivity_.class);
        if (entityType >= 0 && entityId >= 0) {
            intent.putExtra(JandiConstants.EXTRA_ENTITY_ID, entityId);
            intent.putExtra(JandiConstants.EXTRA_ENTITY_TYPE, entityType);
            intent.putExtra(JandiConstants.EXTRA_IS_FROM_PUSH, true);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext()
                , 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title);
        bigTextStyle.bigText(body);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(body);
        mBuilder.setStyle(bigTextStyle);
        mBuilder.setDefaults(Notification.DEFAULT_ALL);
        mBuilder.setSmallIcon(R.drawable.jandi_actionb_logo);

        try {
            if (overLayUrl != null) {
                Bitmap bitmap = Picasso.with(getApplicationContext()).load(overLayUrl).get();
                if (bitmap != null) {
                    log.debug("setLargeIcon");
                    mBuilder.setLargeIcon(bitmap);
                }
            }
        } catch (IOException e) {

        }


//        mBuilder.setNumber(3);

        // 텍스트 또는 이미지가 첨부되어있는 푸시일 경우 아래 코드를 써주면 Notification이 펼쳐진 상태로 나오게 됩니다.
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        mBuilder.setContentIntent(contentIntent);
        nm.notify(JandiConstants.NOTIFICATION_ID, mBuilder.build());
    }
}

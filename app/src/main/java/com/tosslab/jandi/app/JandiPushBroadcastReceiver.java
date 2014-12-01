package com.tosslab.jandi.app;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * Created by justinygchoi on 14. 11. 27..
 */
public class JandiPushBroadcastReceiver extends ParsePushBroadcastReceiver {
    private static final String JSON_KEY_DATA  = "com.parse.Data";
    private static final String JSON_KEY_TITLE  = "title";
    private static final String JSON_KEY_MESSAGE  = "alert";

    private static final int MAX_LENGTH_SMALL_NOTIFICATION  = 20;
    @Override
    protected Notification getNotification(Context context, Intent intent) {
        Bundle extras = intent.getExtras();

        String title = "";
        String message = "";
        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            String jsonData = extras.getString(JSON_KEY_DATA);

            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode dataObj = mapper.readTree(jsonData);
                Log.d("Hello", dataObj.toString());
                title = getJsonNodeValue(dataObj, JSON_KEY_TITLE);
                message = getJsonNodeValue(dataObj, JSON_KEY_MESSAGE);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return generateNotification(context, title, message);
    }

    private Notification generateNotification(Context context, String title, String message) {
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle(title);
        builder.setContentText(message);
        if (message.length() > MAX_LENGTH_SMALL_NOTIFICATION) {
            builder.setStyle(getBigTextStyle(title, message, context.getString(R.string.app_name)));
        }
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setSmallIcon(R.drawable.jandi_icon_push_notification);
        builder.setNumber(5);
        // 텍스트 또는 이미지가 첨부되어있는 푸시일 경우 아래 코드를 써주면 Notification이 펼쳐진 상태로 나오게 됩니다.
        builder.setPriority(Notification.PRIORITY_MAX);
//        builder.setContentIntent(contentIntent);
        return builder.build();
    }

    private Notification.BigTextStyle getBigTextStyle(String title, String message, String summary) {
        Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
        bigTextStyle.setBigContentTitle(title);
        bigTextStyle.bigText(message);
        bigTextStyle.setSummaryText(summary);
        return bigTextStyle;
    }

    private String getJsonNodeValue(JsonNode dataObj, String key) {
        if (dataObj != null && dataObj.get(key) != null) {
            return dataObj.get(key).asText();
        }
        return "";
    }
}

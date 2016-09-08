package com.tosslab.jandi.app.push.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.domain.PushHistory;
import com.tosslab.jandi.app.local.orm.repositories.PushHistoryRepository;
import com.tosslab.jandi.app.push.PushInterfaceActivity;
import com.tosslab.jandi.app.push.to.BaseMessagePushInfo;
import com.tosslab.jandi.app.push.to.CommentPushInfo;
import com.tosslab.jandi.app.push.to.FilePushInfo;
import com.tosslab.jandi.app.push.to.MessagePushInfo;
import com.tosslab.jandi.app.push.to.PushRoomType;
import com.tosslab.jandi.app.spannable.SpannableLookUp;
import com.tosslab.jandi.app.ui.settings.Settings;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

public class JandiPushReceiverModel {
    public static final String TAG = JandiPushReceiverModel.class.getSimpleName();
    private static final int PENDING_INTENT_REQUEST_CODE = 2012;

    AudioManager audioManager;

    NotificationManager notificationManager;

    public JandiPushReceiverModel(AudioManager audioManager, NotificationManager notificationManager) {
        this.audioManager = audioManager;
        this.notificationManager = notificationManager;
    }

    public PendingIntent generatePendingIntent(Context context, long roomId, int chatType, long teamId, String roomType) {

        Intent intent = PushInterfaceActivity.getIntent(context, roomId, chatType, true, teamId, roomType);

        // 노티피케이션은 해제 됐지만 PendingIntent 가 살아있는 경우가 있어 cancel 을 호출해줌.
        PendingIntent.getActivity(context,
                PENDING_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                .cancel();

        return PendingIntent.getActivity(context,
                PENDING_INTENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public int getEntityType(String roomType) {
        if (TextUtils.equals(roomType, PushRoomType.CHANNEL.getName())) {
            return JandiConstants.TYPE_PUBLIC_TOPIC;
        } else if (TextUtils.equals(roomType, PushRoomType.PRIVATE_GROUP.getName())) {
            return JandiConstants.TYPE_PRIVATE_TOPIC;
        } else if (TextUtils.equals(roomType, PushRoomType.CHAT.getName())) {
            return JandiConstants.TYPE_DIRECT_MESSAGE;
        } else {
            return -1;
        }
    }

    public String getPlainMarkdownContent(Context context, BaseMessagePushInfo messagePushInfo) {

        String originMessage = "";
        if (isStickerMessage(messagePushInfo)) {
            originMessage = "(Sticker)";
        } else if (messagePushInfo instanceof MessagePushInfo) {
            originMessage = ((MessagePushInfo) messagePushInfo).getMessageContent().getBody();
        } else if (messagePushInfo instanceof CommentPushInfo) {
            originMessage = ((CommentPushInfo) messagePushInfo).getMessageContent().getBody();
        } else if (messagePushInfo instanceof FilePushInfo) {
            return ((FilePushInfo) messagePushInfo).getMessageContent().title;
        } else {
            return originMessage;
        }
        SpannableStringBuilder contentWrapper = new SpannableStringBuilder(originMessage);
        SpannableLookUp.text(contentWrapper)
                .markdown(true)
                .lookUp(context);
        return contentWrapper.toString();
    }

    private boolean isStickerMessage(BaseMessagePushInfo messagePushInfo) {
        return "sticker".equals(messagePushInfo.getMessageType())
                || "comment_sticker".equals(messagePushInfo.getMessageType());
    }

    public boolean isMentionToMe(String mentioned) {
        return BaseMessagePushInfo.MENTION_TO_ME.equals(mentioned)
                || BaseMessagePushInfo.MENTION_TO_ALL.equals(mentioned);
    }

    private NotificationCompat.Builder getNotification(Context context,
                                                       String notificationTitle,
                                                       String roomName,
                                                       String message, Bitmap writerProfile,
                                                       int badgeCount, PendingIntent pendingIntent) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(notificationTitle)
                .setSmallIcon(R.drawable.icon_push_notification)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setSubText(roomName)
                .setContentTitle(notificationTitle)
                .setContentText(message)
                .setNumber(badgeCount)
                .setStyle(getBigTextStyle(notificationTitle, message, roomName))
                .setContentIntent(pendingIntent);

        if (writerProfile != null) {    // 작성자의 프로필 사진
            builder.setLargeIcon(writerProfile);
        }

        return builder;
    }

    private void setUpNotificationEffect(NotificationCompat.Builder notificationBuilder, Context context, boolean isMentionMessage, int roomTypeInt) {
        int led = 0;

        if (JandiPreference.isAlarmLED(context)) {
            led = Notification.DEFAULT_LIGHTS;
        }

        int sound = 0;

        if (JandiPreference.isAlarmSound(context)) {
            int soundEffecSetting = -1;
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                if (isMentionMessage) {
                    soundEffecSetting = preferences.getInt(Settings.SETTING_PUSH_ALARM_SOUND_MENTION, 0);
                } else if (roomTypeInt == JandiConstants.TYPE_DIRECT_MESSAGE) {
                    soundEffecSetting = preferences.getInt(Settings.SETTING_PUSH_ALARM_SOUND_DM, 0);
                } else {
                    soundEffecSetting = preferences.getInt(Settings.SETTING_PUSH_ALARM_SOUND_TOPIC, 0);
                }
            }

            if (soundEffecSetting == 0) {
                sound = Notification.DEFAULT_SOUND;
            } else if (soundEffecSetting > 0) {
                String[] fileNames = context.getResources().getStringArray(R.array.jandi_notification_array_file);

                if (soundEffecSetting < fileNames.length) {
                    int rawId = context.getResources().getIdentifier(fileNames[soundEffecSetting - 1], "raw", context.getPackageName());
                    StringBuilder builder = new StringBuilder();
                    builder.append("android.resource://")
                            .append(context.getPackageName())
                            .append("/")
                            .append(rawId);
                    notificationBuilder.setSound(Uri.parse(builder.toString()));
                    sound = 0;
                } else {
                    sound = Notification.DEFAULT_SOUND;
                }
            } else {
                sound = 0;
            }

        }

        int vibrate = 0;

        if (JandiPreference.isAlarmVibrate(context)) {
            vibrate = Notification.DEFAULT_VIBRATE;
        }

        notificationBuilder.setDefaults(led | sound | vibrate);
    }

    private NotificationCompat.BigTextStyle getBigTextStyle(String title, String message, String summary) {
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
        bigTextStyle.setBigContentTitle(title)
                .bigText(message)
                .setSummaryText(summary);
        return bigTextStyle;
    }

    public void showNotification(Context context, BaseMessagePushInfo baseMessagePushInfo, boolean isMentionMessage) {

        String writerName = baseMessagePushInfo.getWriterName();

        long teamId = baseMessagePushInfo.getTeamId();
        long roomId = baseMessagePushInfo.getRoomId();
        String writerThumb = baseMessagePushInfo.getWriterThumb();
        Bitmap profileImage = null;
        if (!TextUtils.isEmpty(writerThumb)) {
            try {
                profileImage = ImageUtil.getBitmap(context, writerThumb);
            } catch (Exception e) {
                LogUtil.e("Failed Notification Image", e);
            }
        }

        String notificationTitle = writerName;
        if (isMentionMessage) {
            notificationTitle =
                    context.getString(R.string.jandi_mention_push_message, writerName);
        }

        int badgeCount = baseMessagePushInfo.getBadgeCount();

        String roomType = baseMessagePushInfo.getRoomType();
        int roomTypeInt = getEntityType(roomType);
        String roomName = getRoomName(context, baseMessagePushInfo, roomTypeInt);

        String message = getPlainMarkdownContent(context, baseMessagePushInfo);
        String outMessage = getOutMessage(roomTypeInt, message);

        NotificationCompat.Builder notificationBuilder =
                getNotification(context, notificationTitle,
                        roomName, outMessage, profileImage, badgeCount,
                        generatePendingIntent(context, roomId, roomTypeInt, teamId, roomType));

        setUpNotificationEffect(notificationBuilder, context, isMentionMessage, roomTypeInt);

        // 노티를 터치할 경우엔 자동 삭제되나, 노티를 삭제하지 않고 앱으로 진입했을 때,
        // 해당 채팅 방에 들어갈 때만 이 노티가 삭제되도록...
        JandiPreference.setChatIdFromPush(context, roomId);
        sendNotification(context, notificationBuilder.build());
    }

    private String getRoomName(Context context, BaseMessagePushInfo baseMessagePushInfo, int roomTypeInt) {
        String roomName;
        if (roomTypeInt == JandiConstants.TYPE_DIRECT_MESSAGE) {
            roomName = context.getString(R.string.jandi_tab_direct_message);
        } else {
            roomName = baseMessagePushInfo.getRoomName();
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

    void sendNotification(Context context, Notification notification) {
        if (notification != null) {
            notificationManager.cancel(JandiConstants.NOTIFICATION_ID);
            notificationManager.notify(JandiConstants.NOTIFICATION_ID, notification);
        }
    }

    public void removeNotificationIfNeed(long roomId) {
        PushHistory latestPushHistory = PushHistoryRepository.getRepository().getLatestPushHistory();
        if (latestPushHistory.getRoomId() == roomId) {
            notificationManager.cancel(JandiConstants.NOTIFICATION_ID);
        }
    }

    public void removeNotificationAll() {
        notificationManager.cancel(JandiConstants.NOTIFICATION_ID);
    }
}

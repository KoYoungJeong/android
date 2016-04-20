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

import com.koushikdutta.ion.Ion;
import com.parse.ParseInstallation;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.client.main.LeftSideApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.json.JacksonMapper;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.push.PushInterfaceActivity_;
import com.tosslab.jandi.app.push.monitor.PushMonitor;
import com.tosslab.jandi.app.push.to.PushInfo;
import com.tosslab.jandi.app.push.to.PushRoomType;
import com.tosslab.jandi.app.spannable.SpannableLookUp;
import com.tosslab.jandi.app.ui.settings.Settings;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.parse.ParseUpdateUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;


/**
 * Created by Steve SeongUg Jung on 15. 4. 10..
 */
@EBean
public class JandiPushReceiverModel {
    public static final String TAG = JandiPushReceiverModel.class.getSimpleName();
    private static final int PENDING_INTENT_REQUEST_CODE = 2012;
    @SystemService
    AudioManager audioManager;

    @Inject
    Lazy<LeftSideApi> leftSideApi;

    @AfterInject
    void initObject() {
        DaggerApiClientComponent.create().inject(this);
    }

    public PendingIntent generatePendingIntent(Context context, long chatId, int chatType, long teamId, String roomType) {

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

    public boolean isTopicPushOn(ResLeftSideMenu leftSideMenu, long roomId) {
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

    public boolean isPushForMyAccountId(PushInfo pushInfo) {
        return pushInfo.getAccountId() == AccountRepository.getRepository().getAccountInfo().getId();
    }

    public PushInfo parsingPushTO(String content) {
        if (TextUtils.isEmpty(content)) {
            LogUtil.e(TAG, "extras has not data.");
            return null;
        }

        try {
            ObjectMapper mapper = JacksonMapper.getInstance().getObjectMapper();
            return mapper.readValue(content, PushInfo.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void convertPlainMarkdownContent(Context context, PushInfo pushInfo) {
        String content = pushInfo.getMessageContent();
        SpannableStringBuilder contentWrapper = new SpannableStringBuilder(content);
        SpannableLookUp.text(contentWrapper)
                .markdown(true)
                .lookUp(context);
        pushInfo.setMessageContent(contentWrapper.toString());
    }

    public ResLeftSideMenu getLeftSideMenuFromDB(long teamId) {
        return LeftSideMenuRepository.getRepository().findLeftSideMenuByTeamId(teamId);
    }

    public ResLeftSideMenu getLeftSideMenuFromServer(long teamId) {
        ResLeftSideMenu leftSideMenu = null;
        try {
            leftSideMenu = leftSideApi.get().getInfosForSideMenu(teamId);
        } catch (RetrofitException e) {
        }
        return leftSideMenu;
    }

    public void upsertLeftSideMenu(ResLeftSideMenu leftSideMenu) {
        LeftSideMenuRepository.getRepository().upsertLeftSideMenu(leftSideMenu);
    }

    public boolean isMyEntityId(long writerId) {
        List<ResAccountInfo.UserTeam> userTeams = AccountRepository.getRepository().getAccountTeams();

        for (ResAccountInfo.UserTeam userTeam : userTeams) {
            if (userTeam.getMemberId() == writerId) {
                return true;
            }
        }
        return false;
    }

    public boolean isMentionToMe(List<PushInfo.Mention> mentions, ResLeftSideMenu leftSideMenu) {
        boolean isMentionToMe = false;
        if (mentions == null || mentions.isEmpty()) {
            return false;
        }

        long myTeamMemberId = leftSideMenu.user.id;
        List<ResLeftSideMenu.Entity> joinEntities = leftSideMenu.joinEntities;

        logJoinEntities(joinEntities);

        for (PushInfo.Mention mention : mentions) {
            long entityId = mention.getId();
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

    private boolean amIJoined(List<ResLeftSideMenu.Entity> joinEntities, long mentionedEntityId) {
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

    public void showNotification(Context context, PushInfo pushInfo, boolean isMentionMessage) {

        String writerName = pushInfo.getWriterName();

        long teamId = pushInfo.getTeamId();
        long roomId = pushInfo.getRoomId();
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

        int badgeCount = pushInfo.getBadgeCount();

        String roomType = pushInfo.getRoomType();
        int roomTypeInt = getEntityType(roomType);
        String roomName = getRoomName(context, pushInfo, roomTypeInt);

        String message = pushInfo.getMessageContent();
        String outMessage = getOutMessage(roomTypeInt, message);


        NotificationCompat.Builder notificationBuilder =
                getNotification(context, notificationTitle,
                        roomName, outMessage, profileImage, badgeCount,
                        generatePendingIntent(context, roomId, roomTypeInt, teamId, roomType));

        setUpNotificationEffect(notificationBuilder, context, isMentionMessage, roomTypeInt);

        PushMonitor.getInstance().setLastNotificationBuilder(notificationBuilder);


        // 노티를 터치할 경우엔 자동 삭제되나, 노티를 삭제하지 않고 앱으로 진입했을 때,
        // 해당 채팅 방에 들어갈 때만 이 노티가 삭제되도록...
        JandiPreference.setChatIdFromPush(context, roomId);

        sendNotification(context, notificationBuilder.build());
    }

    private String getRoomName(Context context, PushInfo pushInfo, int roomTypeInt) {
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

    void sendNotification(Context context, Notification notification) {
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notification != null) {
            nm.cancel(JandiConstants.NOTIFICATION_ID);
            nm.notify(JandiConstants.NOTIFICATION_ID, notification);
        }
    }

}

package com.tosslab.jandi.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.tosslab.jandi.app.JandiApplication;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by justinygchoi on 2014. 7. 8..
 */
public class JandiPreference {
    public static final long NOT_SET_YET = -1;
    public static final String PREF_SETTING_ORIENTATION = "setting_orientation";
    public static final String PREF_VALUE_PUSH_PREVIEW_ALL_MESSAGE = "0";
    public static final String PREF_VALUE_PUSH_PREVIEW_PUBLIC_ONLY = "1";
    public static final String PREF_VALUE_PUSH_NO_PREVIEW = "2";
    // SharedPreference Key 값
    private static final String PREF_NAME = "JandiPref";
    private static final String PREF_TOKEN = "token";
    private static final String PREF_CHAT_ID = "chatId";
    private static final String PREF_CHAT_ID_FROM_PUSH = "pushEntity_long";
    private static final String PREF_BADGE_COUNT = "badgeCount";
    private static final String PREF_MY_ENTITY_ID = "myEntityId";
    private static final String PREF_REFRESH_TOKEN = "refresh_token";
    private static final String PREF_ACCESS_TOKEN = "access_token";
    private static final String PREF_ACCESS_TOKEN_TYPE = "access_token_type";
    private static final String PREF_REFRESH_TOKEN_TIME = "refresh_token_time";
    private static final String PREF_FIRST_LOGIN = "first_login";
    private static final String PREF_ALARM_SOUND = "setting_push_alarm_sound";
    private static final String PREF_ALARM_VIBRATE = "setting_push_alarm_vibration";
    private static final String PREF_ALARM_LED = "setting_push_alarm_led";
    private static final String PREF_INVITE_POPUP = "invite_popup";
    private static final String PREF_KEYBOARD_HEIGHT_PORTRAIT = "keyboard_height_portrait";
    private static final String PREF_KEYBOARD_HEIGHT_LANDSCAPE = "keyboard_height_landscape";
    private static final String PREF_COACH_MARK_TOPIC = "coach_mark_topic";
    private static final String PREF_COACH_MARK_TOPIC_LIST = "coach_mark_topic_list";
    private static final String PREF_COACH_MARK_MORE = "coach_mark_more";
    private static final String PREF_COACH_MARK_FILE_LIST = "coach_mark_file_list";
    private static final String PREF_COACH_MARK_DIRECT_MESSAGE_LIST = "coach_mark_direct_messege_list";
    private static final String PREF_LAST_NETWORK_CONNECT = "last_network_connect_state";
    private static final String PREF_PASSCODE = "passcode";
    private static final String PREF_USE_FINGERPRRINT = "fingerprint";
    // PARSE
    private static final String PREF_VERSION_POPUP_LAST_TIME = "version_popup_last_time";
    private static final String PREF_PUSH_PREVIEW_INFO = "setting_push_preview";
    private static final String PREF_SOCKET_CONNECTED_LAST_TIME = "socket_connected_last_time";

    private static final String PREF_LAST_EXECUTED_TIME = "last_executed_time";
    private static final String PREF_LAST_TOPIC_ORDER_TYPE = "last_topic_order_type";

    private static final String PREF_SOCKET_RECONNECT_DELAY = "socket_reconnect_delay";
    private static final String PREF_LAST_SELECTED_TAB = "last_selected_tab";
    private static final String PREF_VERSION_CODE_STAMP = "version_code_stamp";
    private static final String PREF_EMAIL_AUTH_SEND_TIME = "email_auth_send_time";

    private static final String PREF_NAVIGATION_POSITION = "navigation_position";
    private static final String PREF_LATEST_FCM_TOKEN_UPDATE = "latest_fcm_token_update";
    private static final String PREF_LAST_SELECTED_OF_MYPAGE = "last_selected_of_mypage";
    private static final String PREF_LAST_SELECTED_OF_TEAM = "last_selected_of_team";

    private static final String PREF_CALL_PREVIEW_Y = "call_preview_coordinate_y";
    private static final String PREF_CALL_POPUP = "call_preview_popup";
    private static final String PREF_CALL_PERMISSION_POPUP = "call_preview_permission_popup";
    private static final String PREF_CALL_PERMISSION_POPUP_TODAY = "call_preview_permission_popup_today";

    private static final String PREF_PUSH_LAST_SENT_AT = "push_last_sent_at";

    private static final String PREF_DEVICE_HOME_BADGE_REFRESH_AT = "device_home_badge_refresh_at";

    private static final String PREF_FOLDER_CLOSED_SET = "push_folder_closed_set";

    private static final String PREF_EXCEED_POPUP_TIME_RECORD = "exceed_popup_time_record";
    private static final String PREF_PROPERTY_DEVICE_ID = "device_id";

    public static boolean isAleadyShowCoachMarkTopic(Context context) {
        if (!getSharedPreferences().getBoolean(PREF_COACH_MARK_TOPIC, false)) {
            getSharedPreferences().edit()
                    .putBoolean(PREF_COACH_MARK_TOPIC, true).commit();
            return false;
        }
        return true;
    }

    public static boolean isAleadyShowCoachMarkTopicList(Context context) {
        if (!getSharedPreferences().getBoolean(PREF_COACH_MARK_TOPIC_LIST, false)) {
            getSharedPreferences().edit()
                    .putBoolean(PREF_COACH_MARK_TOPIC_LIST, true).commit();
            return false;
        }
        return true;
    }

    public static boolean isAleadyShowCoachMarkFileList(Context context) {
        if (!getSharedPreferences().getBoolean(PREF_COACH_MARK_FILE_LIST, false)) {
            getSharedPreferences().edit()
                    .putBoolean(PREF_COACH_MARK_FILE_LIST, true).commit();
            return false;
        }
        return true;
    }

    public static boolean isAleadyShowCoachMarkMore(Context context) {
        if (!getSharedPreferences().getBoolean(PREF_COACH_MARK_MORE, false)) {
            getSharedPreferences().edit()
                    .putBoolean(PREF_COACH_MARK_MORE, true).commit();
            return false;
        }
        return true;
    }


    public static boolean isAleadyShowCoachMarkDirectMessageList(Context context) {
        if (!getSharedPreferences().getBoolean(PREF_COACH_MARK_DIRECT_MESSAGE_LIST, false)) {
            getSharedPreferences().edit()
                    .putBoolean(PREF_COACH_MARK_DIRECT_MESSAGE_LIST, true).commit();
            return false;
        }
        return true;
    }

    public static long getChatIdFromPush(Context context) {
        SharedPreferences pref = getSharedPreferences();
        return pref.getLong(PREF_CHAT_ID_FROM_PUSH, NOT_SET_YET);
    }

    public static void setChatIdFromPush(Context context, long chatId) {
        SharedPreferences pref = getSharedPreferences();
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(PREF_CHAT_ID_FROM_PUSH, chatId);
        editor.commit();
    }


    public static void setMyEntityId(Context context, long myEntityId) {
        SharedPreferences pref = getSharedPreferences();
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(PREF_MY_ENTITY_ID, myEntityId);
        editor.commit();
    }

    // JANDI Access Token
    public static String getMyToken(Context context) {
        SharedPreferences pref = getSharedPreferences();
        return pref.getString(PREF_TOKEN, "");
    }

    public static void clearMyToken(Context context) {
        SharedPreferences pref = getSharedPreferences();
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(PREF_TOKEN, "");
        editor.commit();
    }

    @Deprecated
    public static String getAccessTokenType(Context context) {
        SharedPreferences pref = getSharedPreferences();
        return pref.getString(PREF_ACCESS_TOKEN_TYPE, "");
    }

    @Deprecated
    public static String getAccessToken(Context context) {
        SharedPreferences pref = getSharedPreferences();
        return pref.getString(PREF_ACCESS_TOKEN, "");
    }

    @Deprecated
    public static String getRefreshToken(Context context) {
        SharedPreferences pref = getSharedPreferences();
        return pref.getString(PREF_REFRESH_TOKEN, "");
    }

    public static void removeTokenInfo(Context context) {
        SharedPreferences pref = getSharedPreferences();
        pref.edit()
                .remove(PREF_ACCESS_TOKEN)
                .remove(PREF_ACCESS_TOKEN_TYPE)
                .remove(PREF_REFRESH_TOKEN)
                .commit();
    }

    public static void signOut(Context context) {
        SharedPreferences pref = getSharedPreferences();
        pref.edit().clear().commit();

        isAleadyShowCoachMarkTopic(context);
        isAleadyShowCoachMarkTopicList(context);
        isAleadyShowCoachMarkFileList(context);
        isAleadyShowCoachMarkMore(context);
        isAleadyShowCoachMarkDirectMessageList(context);

        setFirstLogin(context);

        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .clear()
                .commit();
    }

    public static void setFirstLogin(Context context) {
        SharedPreferences pref = getSharedPreferences();
        pref.edit().putBoolean(PREF_FIRST_LOGIN, true).commit();
    }

    public static boolean isFirstLogin(Context context) {
        SharedPreferences pref = getSharedPreferences();
        return pref.getBoolean(PREF_FIRST_LOGIN, false);
    }

    @NonNull
    private static SharedPreferences getSharedPreferences() {
        return JandiApplication.getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isAlarmLED(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_ALARM_LED, true);
    }

    public static boolean isAlarmVibrate(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_ALARM_VIBRATE, true);
    }

    public static boolean isAlarmSound(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PREF_ALARM_SOUND, true);
    }

    public static boolean isInvitePopup(Context context) {
        return getSharedPreferences().getBoolean(PREF_INVITE_POPUP, true);
    }

    public static void setInvitePopup(Context context) {
        getSharedPreferences().edit().putBoolean(PREF_INVITE_POPUP, false).commit();
    }

    public static void setKeyboardHeight(Context context, int keyboardHeight) {

        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getSharedPreferences().edit().putInt(PREF_KEYBOARD_HEIGHT_LANDSCAPE, keyboardHeight).commit();
        } else {
            getSharedPreferences().edit().putInt(PREF_KEYBOARD_HEIGHT_PORTRAIT, keyboardHeight).commit();
        }

    }

    public static int getKeyboardHeight(Context context) {

        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return getSharedPreferences().getInt(PREF_KEYBOARD_HEIGHT_LANDSCAPE, -1);
        } else {
            return getSharedPreferences().getInt(PREF_KEYBOARD_HEIGHT_PORTRAIT, -1);
        }
    }

    public static String getPassCode(Context context) {
        return getSharedPreferences().getString(PREF_PASSCODE, "");
    }

    public static void setPassCode(Context context, String passCode) {
        SharedPreferences pref = getSharedPreferences();
        pref.edit().putString(PREF_PASSCODE, passCode).commit();
    }

    public static void removePassCode(Context context) {
        SharedPreferences pref = getSharedPreferences();
        pref.edit().remove(PREF_PASSCODE).commit();
    }

    public static boolean isUseFingerprint() {
        return getSharedPreferences().getBoolean(PREF_USE_FINGERPRRINT, true);
    }

    public static void setUseFingerprint(boolean useFingerprint) {
        SharedPreferences pref = getSharedPreferences();
        pref.edit().putBoolean(PREF_USE_FINGERPRRINT, useFingerprint).commit();
    }

    /**
     * 마지막 네트워크 접속 상태 값
     *
     * @param context
     * @param state   0 : disconnected, 1 : connected
     */
    public static void setLastNetworkConnect(Context context, int state) {
        getSharedPreferences().edit().putInt(PREF_LAST_NETWORK_CONNECT, state).commit();
    }

    /**
     * 마지막 네트워크 접속 상태 값
     *
     * @param context
     * @return 0 : disconnected, 1 : conntected, -1 : not setting
     */
    public static int getLastNetworkConnect(Context context) {
        return getSharedPreferences().getInt(PREF_LAST_NETWORK_CONNECT, -1);
    }

    public static String getOrientation(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString
                (PREF_SETTING_ORIENTATION, "0");
    }

    public static void setVersionPopupLastTimeToCurrentTime(long time) {
        getSharedPreferences().
                edit().putLong(PREF_VERSION_POPUP_LAST_TIME, time).commit();
    }

    public static long getVersionPopupLastTime() {
        return getSharedPreferences()
                .getLong(PREF_VERSION_POPUP_LAST_TIME, 0);
    }

    public static String getPushPreviewInfo() {
        return PreferenceManager.getDefaultSharedPreferences(JandiApplication.getContext())
                .getString(PREF_PUSH_PREVIEW_INFO, "0");
    }

    public static boolean isClearedLink() {
        return getSharedPreferences().getBoolean("cleared_link", false);
    }

    public static void setClearedLink() {
        getSharedPreferences().edit()
                .putBoolean("cleared_link", true)
                .commit();
    }

    public static long getSocketConnectedLastTime() {
        return getSharedPreferences().getLong(PREF_SOCKET_CONNECTED_LAST_TIME, -1);
    }

    public static void setSocketConnectedLastTime(long ts) {
        if (getSocketConnectedLastTime() < ts || ts == -1) {
            getSharedPreferences().edit()
                    .putLong(PREF_SOCKET_CONNECTED_LAST_TIME, ts)
                    .commit();
        }
    }

    public static long getLastExecutedTime() {
        return getSharedPreferences().getLong(PREF_LAST_EXECUTED_TIME, 0);
    }

    public static void setLastExecutedTime(long time) {
        getSharedPreferences()
                .edit()
                .putLong(PREF_LAST_EXECUTED_TIME, time)
                .apply();
    }

    /**
     * @return 0 = folder, 1 = updated
     */
    public static int getLastTopicOrderType() {
        return getSharedPreferences()
                .getInt(PREF_LAST_TOPIC_ORDER_TYPE, 0);

    }

    /**
     * @param type 0 = folder, 1 = updated
     */
    public static void setLastTopicOrderType(int type) {
        getSharedPreferences()
                .edit()
                .putInt(PREF_LAST_TOPIC_ORDER_TYPE, type)
                .apply();
    }

    public static long getSocketReconnectDelay() {
        return getSharedPreferences()
                .getLong(PREF_SOCKET_RECONNECT_DELAY, 0l);
    }

    public static void setSocketReconnectDelay(long delay) {
        getSharedPreferences()
                .edit()
                .putLong(PREF_SOCKET_RECONNECT_DELAY, delay)
                .apply();
    }

    public static int getLastSelectedTab() {
        return getSharedPreferences()
                .getInt(PREF_LAST_SELECTED_TAB, 0);
    }

    public static void setLastSelectedTab(int position) {
        getSharedPreferences()
                .edit()
                .putInt(PREF_LAST_SELECTED_TAB, position)
                .apply();
    }

    public static boolean isPutVersionCodeStamp() {
        // 버전 154 이하 사용자의 링크를 초기화 하기 위함
        return getSharedPreferences()
                .getInt(PREF_VERSION_CODE_STAMP, 192) >= com.tosslab.jandi.app.BuildConfig.VERSION_CODE;
    }

    public static void putVersionCodeStamp() {
        getSharedPreferences()
                .edit()
                .putInt(PREF_VERSION_CODE_STAMP, com.tosslab.jandi.app.BuildConfig.VERSION_CODE)
                .apply();
    }

    public static void setEmailAuthSendTime() {
        getSharedPreferences()
                .edit()
                .putLong(PREF_EMAIL_AUTH_SEND_TIME, System.currentTimeMillis())
                .apply();
    }

    public static long getEmailAuthSendTime() {
        return getSharedPreferences()
                .getLong(PREF_EMAIL_AUTH_SEND_TIME, -1);
    }

    public static int getNavigationPosition() {
        return getSharedPreferences()
                .getInt(PREF_NAVIGATION_POSITION, -1);
    }

    public static void setNavigationPosition(int gravity) {
        getSharedPreferences()
                .edit()
                .putInt(PREF_NAVIGATION_POSITION, gravity)
                .commit();
    }

    public static long getLatestFcmTokenUpdate() {
        return getSharedPreferences().getLong(PREF_LATEST_FCM_TOKEN_UPDATE, 0);
    }

    public static void setLatestFcmTokenUpdate(long time) {
        getSharedPreferences().edit()
                .putLong(PREF_LATEST_FCM_TOKEN_UPDATE, time)
                .commit();
    }

    public static int getLastSelectedTabOfMyPage() {
        return getSharedPreferences().getInt(PREF_LAST_SELECTED_OF_MYPAGE, 0);
    }

    public static void setLastSelectedTabOfMyPage(int position) {
        getSharedPreferences().edit().putInt(PREF_LAST_SELECTED_OF_MYPAGE, position)
                .commit();
    }

    public static int getLastSelectedTabOfTeam() {
        return getSharedPreferences().getInt(PREF_LAST_SELECTED_OF_TEAM, 0);
    }

    public static void setLastSelectedTabOfTeam(int position) {
        getSharedPreferences().edit().putInt(PREF_LAST_SELECTED_OF_TEAM, position)
                .commit();
    }

    public static int getCallPreviewCoordinateY() {
        return getSharedPreferences().getInt(PREF_CALL_PREVIEW_Y, 0);
    }

    public static void setCallPreviewCoordinateY(int callPreviewY) {
        getSharedPreferences().edit().putInt(PREF_CALL_PREVIEW_Y, callPreviewY).commit();
    }

    public static boolean isShowCallPermissionPopup() {
        return getSharedPreferences().getBoolean(PREF_CALL_PERMISSION_POPUP, true);
    }

    public static void setShowCallPermissionPopup() {
        getSharedPreferences().edit().putBoolean(PREF_CALL_PERMISSION_POPUP, false).commit();
    }

    public static boolean isShowCallPermissionToday() {
        return (getSharedPreferences().getInt(PREF_CALL_PERMISSION_POPUP_TODAY, 0) !=
                Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
    }

    public static void setShowCallPermissionToday() {
        getSharedPreferences()
                .edit()
                .putInt(PREF_CALL_PERMISSION_POPUP_TODAY, Calendar.getInstance().get(Calendar.DAY_OF_YEAR))
                .commit();
    }

    public static boolean isShowCallPopup() {
        return getSharedPreferences().getBoolean(PREF_CALL_POPUP, true);
    }

    public static void setShowCallPopup(boolean value) {
        getSharedPreferences().edit().putBoolean(PREF_CALL_POPUP, value).commit();
    }

    public static long getPushLastSentAt() {
        return getSharedPreferences().getLong(PREF_PUSH_LAST_SENT_AT, 0);
    }

    public static void setPushLastSentAt(long time) {
        getSharedPreferences().edit().putLong(PREF_PUSH_LAST_SENT_AT, time).commit();
    }

    public static long getDeviceHomeBadgeRefreshAt() {
        return getSharedPreferences().getLong(PREF_DEVICE_HOME_BADGE_REFRESH_AT, 0);
    }

    public static void setDeviceHomeBadgeRefreshAt(long time) {
        getSharedPreferences().edit().putLong(PREF_DEVICE_HOME_BADGE_REFRESH_AT, time).commit();
    }

    public static Set<String> getFolderClosedStatus() {
        return getSharedPreferences().getStringSet(PREF_FOLDER_CLOSED_SET, new HashSet<String>());
    }

    public static void setFolderClosedStatus(Set<String> folderClosedStatusSet) {
        getSharedPreferences().edit().putStringSet(PREF_FOLDER_CLOSED_SET, folderClosedStatusSet).commit();
    }

    public static String getDeviceId() {
        return getSharedPreferences().getString(PREF_PROPERTY_DEVICE_ID, "");
    }

    public static void setDeviceId(String deviceId) {
        getSharedPreferences().edit().putString(PREF_PROPERTY_DEVICE_ID, deviceId).commit();
    }

    public static boolean isExceedPopupWithin3Days() {
        int previousShowExceedPopupTime = getSharedPreferences().getInt(PREF_EXCEED_POPUP_TIME_RECORD, -1);
        if (previousShowExceedPopupTime == -1) {
            return false;
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            int currentMilliSecond = calendar.get(Calendar.MILLISECOND);
            if (previousShowExceedPopupTime - currentMilliSecond < 3 * 24 * 60 * 60 * 1000) {
                return true;
            }
        }
        return false;
    }

    public static void setExceedPopupNotShowRecordTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int currentMilliSecond = calendar.get(Calendar.MILLISECOND);
        getSharedPreferences().edit().putInt(PREF_EXCEED_POPUP_TIME_RECORD, currentMilliSecond).commit();
    }

}
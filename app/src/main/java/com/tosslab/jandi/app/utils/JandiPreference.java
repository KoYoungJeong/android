package com.tosslab.jandi.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by justinygchoi on 2014. 7. 8..
 */
public class JandiPreference {
    public static final int NOT_SET_YET = -1;
    // SharedPreference Key 값
    private static final String PREF_NAME = "JandiPref";
    private static final String PREF_TOKEN = "token";
    private static final String PREF_CHAT_ID = "chatId";
    private static final String PREF_CHAT_ID_FROM_PUSH = "pushEntity";
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
    private static final String PREF_KEYBOARD_HEIGHT = "keyboard_height";
    private static final String PREF_COACH_MARK_TOPIC = "coach_mark_topic";
    private static final String PREF_COACH_MARK_TOPIC_LIST = "coach_mark_topic_list";
    private static final String PREF_COACH_MARK_MORE = "coach_mark_more";
    private static final String PREF_COACH_MARK_FILE_LIST = "coach_mark_file_list";
    private static final String PREF_COACH_MARK_DIRECT_MESSAGE_LIST = "coach_mark_direct_messege_list";

    public static boolean isAleadyShowCoachMarkTopic(Context context) {
        if (!getSharedPreferences(context).getBoolean(PREF_COACH_MARK_TOPIC, false)) {
            getSharedPreferences(context).edit()
                    .putBoolean(PREF_COACH_MARK_TOPIC, true).commit();
            return false;
        }
        return true;
    }

    public static boolean isAleadyShowCoachMarkTopicList(Context context) {
        if (!getSharedPreferences(context).getBoolean(PREF_COACH_MARK_TOPIC_LIST, false)) {
            getSharedPreferences(context).edit()
                    .putBoolean(PREF_COACH_MARK_TOPIC_LIST, true).commit();
            return false;
        }
        return true;
    }

    public static boolean isAleadyShowCoachMarkFileList(Context context) {
        if (!getSharedPreferences(context).getBoolean(PREF_COACH_MARK_FILE_LIST, false)) {
            getSharedPreferences(context).edit()
                    .putBoolean(PREF_COACH_MARK_FILE_LIST, true).commit();
            return false;
        }
        return true;
    }

    public static boolean isAleadyShowCoachMarkMore(Context context) {
        if (!getSharedPreferences(context).getBoolean(PREF_COACH_MARK_MORE, false)) {
            getSharedPreferences(context).edit()
                    .putBoolean(PREF_COACH_MARK_MORE, true).commit();
            return false;
        }
        return true;
    }


    public static boolean isAleadyShowCoachMarkDirectMessageList(Context context) {
        if (!getSharedPreferences(context).getBoolean(PREF_COACH_MARK_DIRECT_MESSAGE_LIST, false)) {
            getSharedPreferences(context).edit()
                    .putBoolean(PREF_COACH_MARK_DIRECT_MESSAGE_LIST, true).commit();
            return false;
        }
        return true;
    }

    public static int getChatIdFromPush(Context context) {
        SharedPreferences pref = getSharedPreferences(context);
        return pref.getInt(PREF_CHAT_ID_FROM_PUSH, NOT_SET_YET);
    }

    public static void setChatIdFromPush(Context context, int chatId) {
        SharedPreferences pref = getSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(PREF_CHAT_ID_FROM_PUSH, chatId);
        editor.commit();
    }

    public static int getActivatedChatId(Context context) {
        SharedPreferences pref = getSharedPreferences(context);
        return pref.getInt(PREF_CHAT_ID, NOT_SET_YET);
    }

    public static void setActivatedChatId(Context context, int chatId) {
        SharedPreferences pref = getSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(PREF_CHAT_ID, chatId);
        editor.commit();
    }

    // Badge Count
    public static int getBadgeCount(Context context) {
        SharedPreferences pref = getSharedPreferences(context);
        return pref.getInt(PREF_BADGE_COUNT, 0);
    }

    public static void setBadgeCount(Context context, int badgeCount) {
        SharedPreferences pref = getSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(PREF_BADGE_COUNT, badgeCount);
        editor.commit();
    }

    public static int getMyEntityId(Context context) {
        SharedPreferences pref = getSharedPreferences(context);
        return pref.getInt(PREF_MY_ENTITY_ID, NOT_SET_YET);
    }

    public static void setMyEntityId(Context context, int myEntityId) {
        SharedPreferences pref = getSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(PREF_MY_ENTITY_ID, myEntityId);
        editor.commit();
    }

    // JANDI Access Token
    public static String getMyToken(Context context) {
        SharedPreferences pref = getSharedPreferences(context);
        return pref.getString(PREF_TOKEN, "");
    }

    public static void clearMyToken(Context context) {
        SharedPreferences pref = getSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(PREF_TOKEN, "");
        editor.commit();
    }

    public static String getAccessTokenType(Context context) {
        SharedPreferences pref = getSharedPreferences(context);
        return pref.getString(PREF_ACCESS_TOKEN_TYPE, "");
    }

    public static void setAccessTokenType(Context context, String accessTokenType) {
        SharedPreferences pref = getSharedPreferences(context);
        pref.edit().putString(PREF_ACCESS_TOKEN_TYPE, accessTokenType).commit();
    }

    public static String getAccessToken(Context context) {
        SharedPreferences pref = getSharedPreferences(context);
        return pref.getString(PREF_ACCESS_TOKEN, "");
    }

    public static void setAccessToken(Context context, String accessToken) {
        SharedPreferences pref = getSharedPreferences(context);
        pref.edit().putString(PREF_ACCESS_TOKEN, accessToken).commit();
    }

    public static String getRefreshToken(Context context) {
        SharedPreferences pref = getSharedPreferences(context);
        return pref.getString(PREF_REFRESH_TOKEN, "");
    }

    public static void setRefreshToken(Context context, String refreshToken) {
        SharedPreferences pref = getSharedPreferences(context);
        pref.edit().putString(PREF_REFRESH_TOKEN, refreshToken).commit();
    }

    public static void signOut(Context context) {
        SharedPreferences pref = getSharedPreferences(context);
        pref.edit().clear().commit();

        isAleadyShowCoachMarkTopic(context);
        isAleadyShowCoachMarkTopicList(context);
        isAleadyShowCoachMarkFileList(context);
        isAleadyShowCoachMarkMore(context);
        isAleadyShowCoachMarkDirectMessageList(context);

        setFirstLogin(context);
    }

    public static void setFirstLogin(Context context) {
        SharedPreferences pref = getSharedPreferences(context);
        pref.edit().putBoolean(PREF_FIRST_LOGIN, true).commit();
    }

    public static boolean isFirstLogin(Context context) {
        SharedPreferences pref = getSharedPreferences(context);
        return pref.getBoolean(PREF_FIRST_LOGIN, false);
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
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
        return getSharedPreferences(context).getBoolean(PREF_INVITE_POPUP, true);
    }

    public static void setInvitePopup(Context context) {
        getSharedPreferences(context).edit().putBoolean(PREF_INVITE_POPUP, false).commit();
    }

    public static void setKeyboardHeight(Context context, int keyboardHeight) {
        getSharedPreferences(context).edit().putInt(PREF_KEYBOARD_HEIGHT, keyboardHeight).commit();
    }

    public static int getKeyboardHeight(Context context) {
        return getSharedPreferences(context).getInt(PREF_KEYBOARD_HEIGHT, 0);
    }
}

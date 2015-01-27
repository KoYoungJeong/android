package com.tosslab.jandi.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

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

    public static int getChatIdFromPush(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(PREF_CHAT_ID_FROM_PUSH, NOT_SET_YET);
    }

    public static void setChatIdFromPush(Context context, int chatId) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(PREF_CHAT_ID_FROM_PUSH, chatId);
        editor.commit();
    }

    public static int getActivatedChatId(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(PREF_CHAT_ID, NOT_SET_YET);
    }

    public static void setActivatedChatId(Context context, int chatId) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(PREF_CHAT_ID, chatId);
        editor.commit();
    }

    // Badge Count
    public static int getBadgeCount(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(PREF_BADGE_COUNT, 0);
    }

    public static void setBadgeCount(Context context, int badgeCount) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(PREF_BADGE_COUNT, badgeCount);
        editor.commit();
    }

    public static int getMyEntityId(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(PREF_MY_ENTITY_ID, NOT_SET_YET);
    }

    public static void setMyEntityId(Context context, int myEntityId) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(PREF_MY_ENTITY_ID, myEntityId);
        editor.commit();
    }


    // JANDI Access Token
    public static String getMyToken(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_TOKEN, "");
    }

    public static void clearMyToken(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(PREF_TOKEN, "");
        editor.commit();
    }

    public static String getAccessTokenType(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_ACCESS_TOKEN_TYPE, "");
    }

    public static void setAccessTokenType(Context context, String accessTokenType) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(PREF_ACCESS_TOKEN_TYPE, accessTokenType).commit();
    }

    public static String getAccessToken(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_ACCESS_TOKEN, "");
    }

    public static void setAccessToken(Context context, String accessToken) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(PREF_ACCESS_TOKEN, accessToken).commit();
    }

    public static String getRefreshToken(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(PREF_REFRESH_TOKEN, "");
    }

    public static void setRefreshToken(Context context, String refreshToken) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putString(PREF_REFRESH_TOKEN, refreshToken).commit();
    }

    public static void signOut(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().clear().commit();

        setFirstLogin(context);
    }

    public static void setFirstLogin(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(PREF_FIRST_LOGIN, true).commit();
    }

    public static boolean isFirstLogin(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(PREF_FIRST_LOGIN, false);
    }


}

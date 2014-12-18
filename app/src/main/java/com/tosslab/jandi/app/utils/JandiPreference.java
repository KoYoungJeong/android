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
    private static final String PREF_LOGIN_ID = "loginId";
    private static final String PREF_CHAT_ID = "chatId";
    private static final String PREF_CHAT_ID_FROM_PUSH = "pushEntity";
    private static final String PREF_HAS_READ_TUTORIAL = "hasReadTutorial";
    private static final String PREF_BADGE_COUNT = "badgeCount";
    private static final String PREF_MY_ENTITY_ID = "myEntityId";
    private static final String PREF_REFRESH_TOKEN = "refresh_token";
    private static final String PREF_ACCESS_TOKEN = "access_token";
    private static final String PREF_ACCESS_TOKEN_TYPE = "access_token_type";
    private static final String PREF_REFRESH_TOKEN_TIME = "refresh_token_time";
    private static final String PREF_LAST_SELECTED_TEAM_ID = "last_selected_team_id";

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

    public static void setMyToken(Context context, String token) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(PREF_TOKEN, token);
        editor.commit();
    }

    public static void clearMyToken(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(PREF_TOKEN, "");
        editor.commit();
    }

    // Tutorial
    public static boolean getFlagForTutorial(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(PREF_HAS_READ_TUTORIAL, false);
    }

    public static void setFlagForTutorial(Context context, boolean hasRead) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(PREF_HAS_READ_TUTORIAL, hasRead);
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

    public static void setLastRefreshTokenTime(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putLong(PREF_REFRESH_TOKEN_TIME, System.currentTimeMillis()).commit();
    }

    public static long getLastRefreshTokenTime(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getLong(PREF_REFRESH_TOKEN_TIME, 0);
    }

    public static int getLastSelectedTeamId(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(PREF_LAST_SELECTED_TEAM_ID, -1);
    }

    public static void setLastSelectedTeamId(Context context, int lastSelectedTeamId) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putInt(PREF_LAST_SELECTED_TEAM_ID, lastSelectedTeamId).commit();
    }
}

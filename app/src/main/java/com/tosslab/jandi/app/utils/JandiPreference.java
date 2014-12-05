package com.tosslab.jandi.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.tosslab.jandi.app.JandiConstants;

/**
 * Created by justinygchoi on 2014. 7. 8..
 */
public class JandiPreference {
    public static final int NOT_SET_YET = -1;
    public static int getChatId(Context context) {
        SharedPreferences pref = context.getSharedPreferences(JandiConstants.PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(JandiConstants.PREF_PUSH_ENTITY, NOT_SET_YET);
    }

    public static void setChatId(Context context, int chatId) {
        SharedPreferences pref = context.getSharedPreferences(JandiConstants.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(JandiConstants.PREF_PUSH_ENTITY, chatId);
        editor.commit();
    }

    // Badge Count
    public static int getBadgeCount(Context context) {
        SharedPreferences pref = context.getSharedPreferences(JandiConstants.PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(JandiConstants.PREF_BADGE_COUNT, 0);
    }

    public static void setBadgeCount(Context context, int badgeCount) {
        SharedPreferences pref = context.getSharedPreferences(JandiConstants.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(JandiConstants.PREF_BADGE_COUNT, badgeCount);
        editor.commit();
    }

    public static int getMyEntityId(Context context) {
        SharedPreferences pref = context.getSharedPreferences(JandiConstants.PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(JandiConstants.PREF_MY_ENTITY_ID, NOT_SET_YET);
    }

    public static void setMyEntityId(Context context, int myEntityId) {
        SharedPreferences pref = context.getSharedPreferences(JandiConstants.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(JandiConstants.PREF_MY_ENTITY_ID, myEntityId);
        editor.commit();
    }


    // JANDI Access Token
    public static String getMyToken(Context context) {
        SharedPreferences pref = context.getSharedPreferences(JandiConstants.PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(JandiConstants.PREF_TOKEN, "");
    }

    public static void setMyToken(Context context, String token) {
        SharedPreferences pref = context.getSharedPreferences(JandiConstants.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(JandiConstants.PREF_TOKEN, token);
        editor.commit();
    }

    public static void clearMyToken(Context context) {
        SharedPreferences pref = context.getSharedPreferences(JandiConstants.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(JandiConstants.PREF_TOKEN, "");
        editor.commit();
    }

    // Tutorial
    public static boolean getFlagForTutorial(Context context) {
        SharedPreferences pref = context.getSharedPreferences(JandiConstants.PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(JandiConstants.PREF_HAS_READ_TUTORIAL, false);
    }
    public static void setFlagForTutorial(Context context, boolean hasRead) {
        SharedPreferences pref = context.getSharedPreferences(JandiConstants.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(JandiConstants.PREF_HAS_READ_TUTORIAL, hasRead);
        editor.commit();
    }
}

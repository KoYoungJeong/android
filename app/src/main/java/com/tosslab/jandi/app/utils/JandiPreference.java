package com.tosslab.jandi.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.tosslab.jandi.app.JandiConstants;

/**
 * Created by justinygchoi on 2014. 7. 8..
 */
public class JandiPreference {
    public static int getEntityId(Context context) {
        SharedPreferences pref = context.getSharedPreferences(JandiConstants.PREF_NAME, Context.MODE_PRIVATE);
        return pref.getInt(JandiConstants.PREF_PUSH_ENTITY, -1);
    }

    public static void setEntityId(Context context, int entityId) {
        SharedPreferences pref = context.getSharedPreferences(JandiConstants.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(JandiConstants.PREF_PUSH_ENTITY, entityId);
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

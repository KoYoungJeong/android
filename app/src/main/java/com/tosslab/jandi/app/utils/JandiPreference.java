package com.tosslab.jandi.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.tosslab.jandi.app.JandiConstants;

/**
 * Created by justinygchoi on 2014. 7. 8..
 */
public class JandiPreference {
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

    public static String getMyId(Context context) {
        SharedPreferences pref = context.getSharedPreferences(JandiConstants.PREF_NAME, Context.MODE_PRIVATE);
        return pref.getString(JandiConstants.PREF_LOGIN_ID, "");
    }

    public static void setMyId(Context context, String token) {
        SharedPreferences pref = context.getSharedPreferences(JandiConstants.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(JandiConstants.PREF_LOGIN_ID, token);
        editor.commit();
    }

    public static void clearMyId(Context context) {
        SharedPreferences pref = context.getSharedPreferences(JandiConstants.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(JandiConstants.PREF_LOGIN_ID, "");
        editor.commit();
    }
}

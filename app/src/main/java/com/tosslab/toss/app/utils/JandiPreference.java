package com.tosslab.toss.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.tosslab.toss.app.JandiConstants;

/**
 * Created by justinygchoi on 2014. 7. 8..
 */
public class JandiPreference {
    public static String getMyToken(Context context) {
        SharedPreferences pref = context.getSharedPreferences(JandiConstants.PREF_NAME, 0);
        return pref.getString(JandiConstants.PREF_TOKEN, "");
    }

    public static void setMyToken(Context context, String token) {
        SharedPreferences pref = context.getSharedPreferences(JandiConstants.PREF_NAME, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(JandiConstants.PREF_TOKEN, token);
        editor.commit();
    }
}

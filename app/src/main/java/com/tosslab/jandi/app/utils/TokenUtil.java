package com.tosslab.jandi.app.utils;

import android.content.Context;

import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.spring.JandiV2HttpAuthentication;

/**
 * Created by Steve SeongUg Jung on 14. 12. 17..
 */
public class TokenUtil {

    public static void saveTokenInfoByPassword(Context context, ResAccessToken accessToken) {
        JandiPreference.setAccessToken(context, accessToken.getAccessToken());
        JandiPreference.setAccessTokenType(context, accessToken.getTokenType());
        JandiPreference.setRefreshToken(context, accessToken.getRefreshToken());
    }

    public static void saveTokenInfoByRefresh(Context context, ResAccessToken accessToken) {
        JandiPreference.setAccessToken(context, accessToken.getAccessToken());
        JandiPreference.setAccessTokenType(context, accessToken.getTokenType());
    }

    public static void clearTokenInfo(Context context) {
        JandiPreference.setAccessToken(context, "");
        JandiPreference.setAccessTokenType(context, "");
        JandiPreference.setRefreshToken(context, "");
    }

    public static JandiV2HttpAuthentication getRequestAuthentication(Context context) {
        return new JandiV2HttpAuthentication(JandiPreference.getAccessTokenType(context), JandiPreference.getAccessToken(context));
    }
}

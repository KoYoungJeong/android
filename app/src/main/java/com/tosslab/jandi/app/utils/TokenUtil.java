package com.tosslab.jandi.app.utils;

import android.content.Context;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.network.models.ResAccessToken;

/**
 * Created by Steve SeongUg Jung on 14. 12. 17..
 */
public class TokenUtil {

    public static void saveTokenInfoByPassword(Context context,
                                               String accessToken, String refreshToken,
                                               String tokenType) {
        if (!TextUtils.isEmpty(accessToken)) {
            JandiPreference.setAccessToken(context, accessToken);
        }

        if (!TextUtils.isEmpty(refreshToken)) {
            JandiPreference.setRefreshToken(context, refreshToken);
        }

        if (!TextUtils.isEmpty(tokenType)) {
            JandiPreference.setAccessTokenType(context, tokenType);
        }
    }


    public static void saveTokenInfoByPassword(ResAccessToken accessToken) {
        saveTokenInfoByRefresh(accessToken);
    }

    public static void saveTokenInfoByRefresh(ResAccessToken accessToken) {
        if (!TextUtils.isEmpty(accessToken.getAccessToken())) {
            JandiPreference.setAccessToken(JandiApplication.getContext(), accessToken.getAccessToken());
        }

        if (!TextUtils.isEmpty(accessToken.getRefreshToken())) {
            JandiPreference.setRefreshToken(JandiApplication.getContext(), accessToken.getRefreshToken());
        }

        if (!TextUtils.isEmpty(accessToken.getTokenType())) {
            JandiPreference.setAccessTokenType(JandiApplication.getContext(), accessToken.getTokenType());
        }
    }

    public static void clearTokenInfo() {
        JandiPreference.setAccessToken(JandiApplication.getContext(), "");
        JandiPreference.setAccessTokenType(JandiApplication.getContext(), "");
        JandiPreference.setRefreshToken(JandiApplication.getContext(), "");
    }

    public static Pair<String, String> getRequestAuthentication() {
        return new Pair<>(JandiPreference.getAccessTokenType(JandiApplication.getContext()),
                JandiPreference.getAccessToken(JandiApplication.getContext()));
    }

}

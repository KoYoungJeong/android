package com.tosslab.jandi.app.utils;

import com.tosslab.jandi.app.local.orm.repositories.AccessTokenRepository;
import com.tosslab.jandi.app.network.models.ResAccessToken;

/**
 * Created by Steve SeongUg Jung on 14. 12. 17..
 */
public class TokenUtil {


    public static boolean saveTokenInfoByPassword(ResAccessToken accessToken) {
        return saveTokenInfoByRefresh(accessToken);
    }

    public static boolean saveTokenInfoByRefresh(ResAccessToken accessToken) {
        return AccessTokenRepository.getRepository().upsertAccessToken(accessToken);
    }

    public static void clearTokenInfo() {
        AccessTokenRepository.getRepository().deleteAccessToken();
    }

    public static String getRequestAuthentication() {
        ResAccessToken accessToken = AccessTokenRepository.getRepository().getAccessToken();
        return String.format("%s %s", accessToken.getTokenType(), accessToken.getAccessToken());
    }

}

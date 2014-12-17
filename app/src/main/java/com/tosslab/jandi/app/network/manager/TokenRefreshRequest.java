package com.tosslab.jandi.app.network.manager;

import android.content.Context;

import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.JandiRestClient_;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.utils.JandiPreference;

/**
 * Created by Steve SeongUg Jung on 14. 12. 16..
 */
public class TokenRefreshRequest implements Request<ResAccessToken> {

    private final Context context;
    private final String refreshToken;

    public TokenRefreshRequest(Context context, String refreshToken) {
        this.context = context;
        this.refreshToken = refreshToken;
    }

    @Override
    public ResAccessToken request() {
        JandiRestClient jandiRestClient_ = new JandiRestClient_(context);

        ResAccessToken accessToken = jandiRestClient_.getAccessToken(ReqAccessToken.createRefreshReqToken(refreshToken));

        // save token info
        JandiPreference.setRefreshToken(context, accessToken.getRefreshToken());
        JandiPreference.setAccessToken(context, accessToken.getAccessToken());
        JandiPreference.setLastRefreshTokenTime(context);

        return accessToken;
    }
}

package com.tosslab.jandi.app.network.manager;

import android.content.Context;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.JandiRestV2Client;
import com.tosslab.jandi.app.network.client.RestAdapterFactory;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.utils.TokenUtil;

import retrofit.RestAdapter;

/**
 * Created by Steve SeongUg Jung on 14. 12. 16..
 */
@Deprecated
public class TokenRefreshRequest implements Request<ResAccessToken> {

    private final Context context;
    private final String refreshToken;

    RestAdapter restAdapter;

    public TokenRefreshRequest(Context context, String refreshToken) {
        this.context = context;
        this.refreshToken = refreshToken;
    }

    @Override
    public ResAccessToken request() {
        JandiRestV2Client jandiRestClient = RestAdapterFactory.getRestAdapter(JandiConstants.REST_TYPE_BASIC).create(JandiRestV2Client.class);
        ResAccessToken accessToken = jandiRestClient.getAccessToken(ReqAccessToken.createRefreshReqToken(refreshToken));
        // save token info
        TokenUtil.saveTokenInfoByRefresh(accessToken);
        return accessToken;
    }
}

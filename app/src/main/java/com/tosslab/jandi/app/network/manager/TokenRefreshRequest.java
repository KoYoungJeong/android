package com.tosslab.jandi.app.network.manager;

/**
 * Created by Steve SeongUg Jung on 14. 12. 16..
 */

//@Deprecated
//public class TokenRefreshRequest implements Request<ResAccessToken> {
//
//    private final Context context;
//    private final String refreshToken;
//
//    RestAdapter restAdapter;
//
//    public TokenRefreshRequest(Context context, String refreshToken) {
//        this.context = context;
//        this.refreshToken = refreshToken;
//    }
//
//    @Override
//    public ResAccessToken request() {
//        JandiRestV2Client jandiRestClient = RestAdapterBuilder.newInstance(JandiRestV2Client.class).create();
//        ResAccessToken accessToken = jandiRestClient.getAccessToken(ReqAccessToken.createRefreshReqToken(refreshToken));
//        // save token info
//        TokenUtil.saveTokenInfoByRefresh(accessToken);
//        return accessToken;
//    }
//}

package com.tosslab.jandi.app.network.manager;

import android.content.Context;

import com.tosslab.jandi.app.network.client.JandiRestClient;
import com.tosslab.jandi.app.network.client.JandiRestClient_;
import com.tosslab.jandi.app.network.client.JandiRestV2Client;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.spring.JandiV3HttpMessageConverter;
import com.tosslab.jandi.app.ui.intro.model.JacksonConverter;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.codehaus.jackson.map.ObjectMapper;

import retrofit.RestAdapter;

/**
 * Created by Steve SeongUg Jung on 14. 12. 16..
 */
public class TokenRefreshRequest implements Request<ResAccessToken> {

    private final Context context;
    private final String refreshToken;

    RestAdapter restAdapter;

    public TokenRefreshRequest(Context context, String refreshToken) {
        this.context = context;
        this.refreshToken = refreshToken;

        JacksonConverter converter = new JacksonConverter(new ObjectMapper());

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setRequestInterceptor(request -> {
                    request.addHeader("Accept", JandiV3HttpMessageConverter.APPLICATION_VERSION_FULL_NAME);
                })
                .setConverter(converter)
                .setEndpoint("http://i2.jandi.io:8888/inner-api")
                .build();


    }

    @Override
    public ResAccessToken request() {
//        JandiRestClient jandiRestClient_ = new JandiRestClient_(context);
//        ResAccessToken accessToken = jandiRestClient_.getAccessToken(ReqAccessToken.createRefreshReqToken(refreshToken));
        ResAccessToken accessToken = restAdapter.create(JandiRestV2Client.class).getAccessToken(ReqAccessToken.createRefreshReqToken(refreshToken));
        // save token info
        TokenUtil.saveTokenInfoByRefresh(accessToken);

        return accessToken;
    }
}

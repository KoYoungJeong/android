package com.tosslab.jandi.app.network.client.main;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ReqTargetToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.network.models.ResCommon;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HTTP;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public class LoginApi extends ApiTemplate<LoginApi.Api> {

    @Inject
    public LoginApi(RetrofitBuilder retrofitBuilder) {
        super(LoginApi.Api.class, retrofitBuilder);
    }

    public ResAccessToken getAccessToken(ReqAccessToken login) throws RetrofitException {
        return call(() -> getApi().getAccessToken(login));
    }

    public ResCommon deleteToken(String refreshToken, String deviceId) throws RetrofitException {
        return call(() -> getApi().deleteToken(new ReqTargetToken(refreshToken, deviceId)));
    }

    interface Api {
        // 로그인
        @POST("token")
        @Headers({"Content-Type : application/json",
                "Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3})
        Call<ResAccessToken> getAccessToken(@Body ReqAccessToken login);

        @HTTP(path = "token", hasBody = true, method = "DELETE")
        @Headers({"Content-Type : application/json",
                "Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3})
        Call<ResCommon> deleteToken(@Body ReqTargetToken token);

    }
}

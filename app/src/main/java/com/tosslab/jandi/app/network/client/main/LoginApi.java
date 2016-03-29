package com.tosslab.jandi.app.network.client.main;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqAccessToken;
import com.tosslab.jandi.app.network.models.ResAccessToken;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public class LoginApi extends ApiTemplate<LoginApi.Api> {

    public LoginApi() {
        super(LoginApi.Api.class);
    }

    public ResAccessToken getAccessToken(ReqAccessToken login) throws RetrofitException {
        return call(() -> getApi().getAccessToken(login));
    }


    interface Api {
        // 로그인
        @POST("/token")
        @Headers({"Content-Type : application/json",
                "Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT})
        Call<ResAccessToken> getAccessToken(@Body ReqAccessToken login);


    }
}

package com.tosslab.jandi.app.network.client.account.password;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ResCommon;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public class AccountPasswordApi extends ApiTemplate<AccountPasswordApi.Api> {

    @Inject
    public AccountPasswordApi(RetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public ResCommon resetPassword(ReqAccountEmail reqAccountEmail) throws RetrofitException {
        return call(() -> getApi().resetPassword(reqAccountEmail));
    }


    interface Api {

        //TOKEN NOT NEDDED
        @POST("accounts/password/resetToken")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> resetPassword(@Body ReqAccountEmail reqAccountEmail);

    }
}

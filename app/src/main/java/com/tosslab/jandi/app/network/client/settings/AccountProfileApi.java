package com.tosslab.jandi.app.network.client.settings;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ResAccountInfo;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public class AccountProfileApi extends ApiTemplate<AccountProfileApi.Api> {
    @Inject
    public AccountProfileApi(RetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public ResAccountInfo changeName(ReqProfileName reqProfileName) throws RetrofitException {
        return call(() -> getApi().changeName(reqProfileName));
    }

    public ResAccountInfo changePrimaryEmail(ReqAccountEmail reqAccountEmail) throws RetrofitException {
        return call(() -> getApi().changePrimaryEmail(reqAccountEmail));
    }

    interface Api {

        @POST("settings/name")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResAccountInfo> changeName(@Body ReqProfileName reqProfileName);

        @PUT("settings/primaryEmail")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResAccountInfo> changePrimaryEmail(@Body ReqAccountEmail reqAccountEmail);

    }
}

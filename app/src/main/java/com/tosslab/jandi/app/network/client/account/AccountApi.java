package com.tosslab.jandi.app.network.client.account;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqProfileName;
import com.tosslab.jandi.app.network.models.ReqUpdatePrimaryEmailInfo;
import com.tosslab.jandi.app.network.models.ResAccountInfo;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PUT;

public class AccountApi extends ApiTemplate<AccountApi.Api> {

    @Inject
    public AccountApi(RetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public ResAccountInfo getAccountInfo() throws RetrofitException {
        return call(() -> getApi().getAccountInfo());
    }

    public ResAccountInfo updatePrimaryEmail(ReqUpdatePrimaryEmailInfo updatePrimaryEmailInfo) throws RetrofitException {
        return call(() -> getApi().updatePrimaryEmail(updatePrimaryEmailInfo));
    }

    public ResAccountInfo updateName(ReqProfileName profileName) throws RetrofitException {
        return call(() -> getApi().updateName(profileName));
    }

    interface Api {
        @GET("account")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResAccountInfo> getAccountInfo();

        @PUT("account")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResAccountInfo> updatePrimaryEmail(@Body ReqUpdatePrimaryEmailInfo updatePrimaryEmailInfo);

        @PUT("account")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResAccountInfo> updateName(@Body ReqProfileName profileName);
    }
}

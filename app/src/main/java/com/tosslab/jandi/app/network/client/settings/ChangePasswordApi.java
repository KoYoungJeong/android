package com.tosslab.jandi.app.network.client.settings;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqChangePassword;
import com.tosslab.jandi.app.network.models.ResCommon;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by tee on 2017. 4. 13..
 */

public class ChangePasswordApi extends ApiTemplate<ChangePasswordApi.Api> {

    @Inject
    public ChangePasswordApi(RetrofitBuilder retrofitBuilder) {
        super(ChangePasswordApi.Api.class, retrofitBuilder);
    }

    public ResCommon changePassword(String oldPassword, String newPassword) throws RetrofitException {
        return call(() -> getApi().changePassword(new ReqChangePassword(oldPassword, newPassword)));
    }

    interface Api {
        @POST("settings/password")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> changePassword(@Body ReqChangePassword reqChangePassword);

    }
}

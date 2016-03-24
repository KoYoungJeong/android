package com.tosslab.jandi.app.network.client.account.password;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqChangePassword;
import com.tosslab.jandi.app.network.models.ResCommon;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public class AccountPasswordApi extends ApiTemplate<AccountPasswordApi.Api> {

    public AccountPasswordApi() {
        super(Api.class);
    }

    public ResCommon resetPassword(ReqAccountEmail reqAccountEmail) throws RetrofitException {
        return call(() -> getApi().resetPassword(reqAccountEmail));
    }

    public ResCommon changePassword(ReqChangePassword reqConfirmEmail) throws RetrofitException {
        return call(() -> getApi().changePassword(reqConfirmEmail));
    }


    interface Api {

        //TOKEN NOT NEDDED
        @POST("/accounts/password/resetToken")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> resetPassword(@Body ReqAccountEmail reqAccountEmail);

        @PUT("/accounts/password")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> changePassword(@Body ReqChangePassword reqConfirmEmail);

    }
}

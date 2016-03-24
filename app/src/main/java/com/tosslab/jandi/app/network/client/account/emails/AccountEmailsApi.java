package com.tosslab.jandi.app.network.client.account.emails;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqAccountEmail;
import com.tosslab.jandi.app.network.models.ReqConfirmEmail;
import com.tosslab.jandi.app.network.models.ResAccountInfo;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HTTP;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public class AccountEmailsApi extends ApiTemplate<AccountEmailsApi.Api> {

    public AccountEmailsApi() {
        super(AccountEmailsApi.Api.class);
    }

    public ResAccountInfo requestAddEmail(ReqAccountEmail reqAccountEmail) throws RetrofitException {
        return call(() -> getApi().requestAddEmail(reqAccountEmail));
    }

    public ResAccountInfo confirmEmail(ReqConfirmEmail reqConfirmEmail) throws RetrofitException {
        return call(() -> getApi().confirmEmail(reqConfirmEmail));
    }

    public ResAccountInfo deleteEmail(ReqAccountEmail reqConfirmEmail) throws RetrofitException {
        return call(() -> getApi().deleteEmail(reqConfirmEmail));
    }


    interface Api {

        @POST("/account/emails")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResAccountInfo> requestAddEmail(@Body ReqAccountEmail reqAccountEmail);

        @PUT("/emails/confirm")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResAccountInfo> confirmEmail(@Body ReqConfirmEmail reqConfirmEmail);

        @HTTP(path = "/account/emails", hasBody = true, method = "DELETE")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResAccountInfo> deleteEmail(@Body ReqAccountEmail reqConfirmEmail);

    }
}

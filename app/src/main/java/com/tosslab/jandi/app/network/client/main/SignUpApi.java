package com.tosslab.jandi.app.network.client.main;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqAccountActivate;
import com.tosslab.jandi.app.network.models.ReqAccountVerification;
import com.tosslab.jandi.app.network.models.ReqSignUpInfo;
import com.tosslab.jandi.app.network.models.ResAccountActivate;
import com.tosslab.jandi.app.network.models.ResCommon;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public class SignUpApi extends ApiTemplate<SignUpApi.Api> {

    @Inject
    public SignUpApi(RetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public ResCommon signUpAccount(ReqSignUpInfo signUpInfo) throws RetrofitException {
        return call(() -> getApi().signUpAccount(signUpInfo));
    }

    public ResAccountActivate activateAccount(ReqAccountActivate reqAccountActivate) throws RetrofitException {
        return call(() -> getApi().activateAccount(reqAccountActivate));
    }

    public ResCommon accountVerification(ReqAccountVerification reqAccountVerification) throws RetrofitException {
        return call(() -> getApi().accountVerification(reqAccountVerification));
    }


    interface Api {

        @POST("accounts")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V4)
        Call<ResCommon> signUpAccount(@Body ReqSignUpInfo signUpInfo);

        @POST("accounts/activate")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<ResAccountActivate> activateAccount(@Body ReqAccountActivate reqAccountActivate);

        @POST("accounts/verification")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> accountVerification(@Body ReqAccountVerification reqAccountVerification);
    }
}

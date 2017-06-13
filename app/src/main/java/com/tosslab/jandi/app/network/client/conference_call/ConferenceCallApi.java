package com.tosslab.jandi.app.network.client.conference_call;

import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.GooroomeeRetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqGooroomeeOtp;
import com.tosslab.jandi.app.network.models.ResGooroomeeOtp;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by tee on 2017. 6. 7..
 */

public class ConferenceCallApi extends ApiTemplate<ConferenceCallApi.Api> {

    @Inject
    public ConferenceCallApi(GooroomeeRetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public ResGooroomeeOtp getGooroomeOtp(ReqGooroomeeOtp reqGooroomeeOtp) throws RetrofitException {
        return call(() -> getApi().getGooroomeOtp(reqGooroomeeOtp));
    }

    interface Api {
        @POST("api/v1/room/user/otp")
        @Headers("Content-Type: application/x-www-form-urlencoded; charset=utf-8")
        Call<ResGooroomeeOtp> getGooroomeOtp(@Body ReqGooroomeeOtp reqGooroomeOtp);
    }

}

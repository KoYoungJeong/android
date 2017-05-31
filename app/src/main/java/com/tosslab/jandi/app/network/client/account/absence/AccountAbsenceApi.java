package com.tosslab.jandi.app.network.client.account.absence;

import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.BaseRetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqAbsenceInfo;
import com.tosslab.jandi.app.network.models.ResCommon;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PUT;

/**
 * Created by tee on 2017. 5. 29..
 */

public class AccountAbsenceApi extends ApiTemplate<AccountAbsenceApi.Api> {

    @Inject
    public AccountAbsenceApi(BaseRetrofitBuilder retrofitBuilder) {
        super(AccountAbsenceApi.Api.class, retrofitBuilder);
    }

    public ResCommon updateAbsenceInfo(ReqAbsenceInfo reqAbsenceInfo) throws RetrofitException {
        return call(() -> getApi().updateAbsenceInfo(reqAbsenceInfo));
    }

    interface Api {
        @PUT("account-api/v1/absence")
        Call<ResCommon> updateAbsenceInfo(@Body ReqAbsenceInfo reqAbsenceInfo);
    }

}

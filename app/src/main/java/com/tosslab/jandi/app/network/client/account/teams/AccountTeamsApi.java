package com.tosslab.jandi.app.network.client.account.teams;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.ResCommon;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Headers;
import retrofit2.http.Path;

/**
 * Created by tee on 2017. 1. 10..
 */

public class AccountTeamsApi extends ApiTemplate<AccountTeamsApi.Api> {

    @Inject
    public AccountTeamsApi(InnerApiRetrofitBuilder retrofitBuilder) {
        super(AccountTeamsApi.Api.class, retrofitBuilder);
    }

    public ResCommon requestLeaveTeam(long teamId) throws RetrofitException {
        return call(() -> getApi().leaveTeam(teamId));
    }

    interface Api {
        @DELETE("account/teams/{teamId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> leaveTeam(@Path("teamId") long teamId);
    }

}
package com.tosslab.jandi.app.network.client.member;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.member.MemberInfo;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public class MemberApi extends ApiTemplate<MemberApi.Api> {
    @Inject
    public MemberApi(InnerApiRetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }


    public MemberInfo getMemberInfo(long teamId, long memberId) throws RetrofitException {
        return call(() -> getApi().getMemberInfo(teamId, memberId));
    }

    interface Api {


        @GET("teams/{teamId}/members/{memberId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<MemberInfo> getMemberInfo(@Path("teamId") long teamId, @Path("memberId") long memberId);


    }
}

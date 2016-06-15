package com.tosslab.jandi.app.network.client.profile;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResAvatarsInfo;
import com.tosslab.jandi.app.network.models.start.Human;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public class ProfileApi extends ApiTemplate<ProfileApi.Api> {
    public ProfileApi(RetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public Human updateMemberProfile(long teamId, long memberId, ReqUpdateProfile reqUpdateProfile) throws RetrofitException {
        return call(() -> getApi().updateMemberProfile(teamId, memberId, reqUpdateProfile));
    }

    public ResAvatarsInfo getAvartarsInfo() throws RetrofitException {
        return call(() -> getApi().getAvartarsInfo());
    }

    public Human getMemberProfile(long teamId, long memberId) throws RetrofitException {
        return call(() -> getApi().getMemberProfile(teamId, memberId));
    }


    interface Api {

        @GET("teams/{teamId}/members/{memberId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<Human> getMemberProfile(@Path("teamId") long teamId,
                                     @Path("memberId") long memberId);

        @PUT("teams/{teamId}/members/{memberId}/profile")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<Human> updateMemberProfile(@Path("teamId") long teamId,
                                        @Path("memberId") long memberId,
                                        @Body ReqUpdateProfile reqUpdateProfile);

        @GET("avatars")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResAvatarsInfo> getAvartarsInfo();

    }
}

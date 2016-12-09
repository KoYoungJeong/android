package com.tosslab.jandi.app.network.client.rooms;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqCreateAnnouncement;
import com.tosslab.jandi.app.network.models.ReqUpdateAnnouncementStatus;
import com.tosslab.jandi.app.network.models.ResCommon;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HTTP;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public class AnnounceApi extends ApiTemplate<AnnounceApi.Api> {
    @Inject
    public AnnounceApi(RetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public ResCommon createAnnouncement(long teamId, long topicId, ReqCreateAnnouncement reqCreateAnnouncement) throws RetrofitException {
        return call(() -> getApi().createAnnouncement(teamId, topicId, reqCreateAnnouncement));
    }

    public ResCommon updateAnnouncementStatus(long teamId, long memberId, ReqUpdateAnnouncementStatus reqUpdateAnnouncementStatus) throws RetrofitException {
        return call(() -> getApi().updateAnnouncementStatus(teamId, memberId, reqUpdateAnnouncementStatus));
    }

    public ResCommon deleteAnnouncement(long teamId, long topicId) throws RetrofitException {
        return call(() -> getApi().deleteAnnouncement(teamId, topicId));
    }


    interface Api {
        @POST("teams/{teamId}/topics/{topicId}/announcement")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> createAnnouncement(@Path("teamId") long teamId, @Path("topicId") long topicId, @Body ReqCreateAnnouncement reqCreateAnnouncement);

        @PUT("teams/{teamId}/members/{memberId}/announcement")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> updateAnnouncementStatus(@Path("teamId") long teamId, @Path("memberId") long memberId, @Body ReqUpdateAnnouncementStatus reqUpdateAnnouncementStatus);

        @HTTP(path = "teams/{teamId}/topics/{topicId}/announcement", hasBody = true, method = "DELETE")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> deleteAnnouncement(@Path("teamId") long teamId, @Path("topicId") long topicId);

    }
}

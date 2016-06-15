package com.tosslab.jandi.app.network.client.rooms;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqMember;
import com.tosslab.jandi.app.network.models.ReqOwner;
import com.tosslab.jandi.app.network.models.ReqUpdateTopicPushSubscribe;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.start.Topic;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public class RoomsApi extends ApiTemplate<RoomsApi.Api> {
    public RoomsApi(RetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public Topic getTopic(long teamId, long topicId) throws RetrofitException {
        return call(() -> getApi().getTopic(teamId, topicId));
    }

    public ResCommon updateTopicPushSubscribe(long teamId, long topicId, ReqUpdateTopicPushSubscribe reqUpdateTopicPushSubscribe) throws RetrofitException {
        return call(() -> getApi().updateTopicPushSubscribe(teamId, topicId, reqUpdateTopicPushSubscribe));
    }

    public ResCommon kickUserFromTopic(long teamId, long topicId, ReqMember member) throws RetrofitException {
        return call(() -> getApi().kickUserFromTopic(teamId, topicId, member));
    }

    public ResCommon assignToTopicOwner(long teamId, long topicId, ReqOwner owner) throws RetrofitException {
        return call(() -> getApi().assignToTopicOwner(teamId, topicId, owner));
    }


    interface Api {


        @GET("teams/{teamId}/rooms/{roomId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<Topic> getTopic(@Path("teamId") long teamId, @Path("roomId") long topicId);

        @PUT("teams/{teamId}/rooms/{roomId}/subscribe")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> updateTopicPushSubscribe(@Path("teamId") long teamId, @Path("roomId") long topicId, @Body ReqUpdateTopicPushSubscribe reqUpdateTopicPushSubscribe);

        @PUT("teams/{teamId}/topics/{topicId}/kickout")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> kickUserFromTopic(@Path("teamId") long teamId, @Path("topicId") long topicId, @Body ReqMember member);

        @PUT("teams/{teamId}/topics/{topicId}/admin")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> assignToTopicOwner(@Path("teamId") long teamId, @Path("topicId") long topicId, @Body ReqOwner owner);
    }
}

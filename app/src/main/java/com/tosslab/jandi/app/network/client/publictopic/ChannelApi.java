package com.tosslab.jandi.app.network.client.publictopic;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqDeleteTopic;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ReqModifyTopicAutoJoin;
import com.tosslab.jandi.app.network.models.ReqModifyTopicDescription;
import com.tosslab.jandi.app.network.models.ReqModifyTopicName;
import com.tosslab.jandi.app.network.models.ResCommon;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HTTP;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class ChannelApi extends ApiTemplate<ChannelApi.Api> {
    public ChannelApi() {
        super(Api.class);
    }

    // 채널 생성
    ResCommon createChannel(long teamId, ReqCreateTopic channel) throws RetrofitException {
        return call(() -> getApi().createChannel(teamId, channel));
    }

    ResCommon modifyPublicTopicName(long teamId,
                                    ReqModifyTopicName topicName,
                                    long channelId) throws RetrofitException {
        return call(() -> getApi().modifyPublicTopicName(teamId, topicName, channelId));
    }

    ResCommon modifyPublicTopicDescription(long teamId,
                                           ReqModifyTopicDescription description,
                                           long channelId) throws RetrofitException {
        return call(() -> getApi().modifyPublicTopicDescription(teamId, description, channelId));
    }

    ResCommon modifyPublicTopicAutoJoin(long teamId,
                                        ReqModifyTopicAutoJoin topicAutoJoin,
                                        long channelId) throws RetrofitException {
        return call(() -> getApi().modifyPublicTopicAutoJoin(teamId, topicAutoJoin, channelId));
    }

    // 채널 삭제
    ResCommon deleteTopic(long channelId, ReqDeleteTopic reqDeleteTopic) throws RetrofitException {
        return call(() -> getApi().deleteTopic(channelId, reqDeleteTopic));
    }

    // 채널 Join
    ResCommon joinTopic(long channelId, ReqDeleteTopic reqDeleteTopic) throws RetrofitException {
        return call(() -> getApi().joinTopic(channelId, reqDeleteTopic));
    }

    // 채널 leave
    ResCommon leaveTopic(long channelId, ReqDeleteTopic reqDeleteTopic) throws RetrofitException {
        return call(() -> getApi().leaveTopic(channelId, reqDeleteTopic));
    }

    // 채널 invite
    ResCommon invitePublicTopic(long channelId, ReqInviteTopicUsers reqInviteTopicUsers) throws RetrofitException {
        return call(() -> getApi().invitePublicTopic(channelId, reqInviteTopicUsers));
    }


    interface Api {

        // 채널 생성
        @POST("/channel")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<ResCommon> createChannel(@Query("teamId") long teamId, @Body ReqCreateTopic channel);

        @PUT("/channels/{channelId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<ResCommon> modifyPublicTopicName(@Query("teamId") long teamId,
                                              @Body ReqModifyTopicName topicName,
                                              @Path("channelId") long channelId);

        @PUT("/channels/{channelId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<ResCommon> modifyPublicTopicDescription(@Query("teamId") long teamId,
                                                     @Body ReqModifyTopicDescription description,
                                                     @Path("channelId") long channelId);

        @PUT("/channels/{channelId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<ResCommon> modifyPublicTopicAutoJoin(@Query("teamId") long teamId,
                                                  @Body ReqModifyTopicAutoJoin topicAutoJoin,
                                                  @Path("channelId") long channelId);

        // 채널 삭제
        @HTTP(path = "/channels/{channelId}", method = "DELETE", hasBody = true)
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> deleteTopic(@Path("channelId") long channelId, @Body ReqDeleteTopic reqDeleteTopic);

        // 채널 Join
        @PUT("/channels/{channelId}/join")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> joinTopic(@Path("channelId") long channelId, @Body ReqDeleteTopic reqDeleteTopic);

        // 채널 leave
        @PUT("/channels/{channelId}/leave")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> leaveTopic(@Path("channelId") long channelId, @Body ReqDeleteTopic reqDeleteTopic);

        // 채널 invite
        @PUT("/channels/{channelId}/invite")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> invitePublicTopic(@Path("channelId") long channelId, @Body ReqInviteTopicUsers reqInviteTopicUsers);
    }
}

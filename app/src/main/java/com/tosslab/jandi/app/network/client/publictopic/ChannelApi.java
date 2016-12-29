package com.tosslab.jandi.app.network.client.publictopic;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqDeleteTopic;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ReqModifyTopicAutoJoin;
import com.tosslab.jandi.app.network.models.ReqModifyTopicDescription;
import com.tosslab.jandi.app.network.models.ReqModifyTopicName;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.start.Topic;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HTTP;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class ChannelApi extends ApiTemplate<ChannelApi.Api> {
    @Inject
    public ChannelApi(RetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    // 채널 생성
    public Topic createChannel(long teamId, ReqCreateTopic channel) throws RetrofitException {
        return call(() -> getApi().createChannel(teamId, channel));
    }

    public ResCommon modifyPublicTopicName(long teamId,
                                           ReqModifyTopicName topicName,
                                           long channelId) throws RetrofitException {
        return call(() -> getApi().modifyPublicTopicName(channelId, teamId, topicName));
    }

    public ResCommon modifyPublicTopicDescription(long teamId,
                                                  ReqModifyTopicDescription description,
                                                  long channelId) throws RetrofitException {
        return call(() -> getApi().modifyPublicTopicDescription(channelId, teamId, description));
    }

    public ResCommon modifyPublicTopicAutoJoin(long teamId,
                                               ReqModifyTopicAutoJoin topicAutoJoin,
                                               long channelId) throws RetrofitException {
        return call(() -> getApi().modifyPublicTopicAutoJoin(channelId, teamId, topicAutoJoin));
    }

    // 채널 삭제
    public ResCommon deleteTopic(long channelId, ReqDeleteTopic reqDeleteTopic) throws RetrofitException {
        return call(() -> getApi().deleteTopic(channelId, reqDeleteTopic));
    }

    // 채널 Join
    public ResCommon joinTopic(long channelId, ReqDeleteTopic reqDeleteTopic) throws RetrofitException {
        return call(() -> getApi().joinTopic(channelId, reqDeleteTopic));
    }

    // 채널 leave
    public ResCommon leaveTopic(long channelId, ReqDeleteTopic reqDeleteTopic) throws RetrofitException {
        return call(() -> getApi().leaveTopic(channelId, reqDeleteTopic));
    }

    // 채널 invite
    public ResCommon invitePublicTopic(long channelId, ReqInviteTopicUsers reqInviteTopicUsers) throws RetrofitException {
        return call(() -> getApi().invitePublicTopic(channelId, reqInviteTopicUsers));
    }

    public ResCommon modifyReadOnly(long teamId, long topicId, boolean readOnly) throws RetrofitException {
        Map<String, Object> map = new HashMap<>();
        map.put("isAnnouncement", readOnly);
        return call(() -> getApi().modifyReadOnly(topicId, teamId, map));
    }


    interface Api {

        // 채널 생성
        @POST("channel")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<Topic> createChannel(@Query("teamId") long teamId, @Body ReqCreateTopic channel);

        @PUT("channels/{channelId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<ResCommon> modifyPublicTopicName(@Path("channelId") long channelId, @Query("teamId") long teamId,
                                              @Body ReqModifyTopicName topicName);

        @PUT("channels/{channelId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<ResCommon> modifyPublicTopicDescription(@Path("channelId") long channelId, @Query("teamId") long teamId,
                                                     @Body ReqModifyTopicDescription description);

        @PUT("channels/{channelId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<ResCommon> modifyPublicTopicAutoJoin(@Path("channelId") long channelId, @Query("teamId") long teamId,
                                                  @Body ReqModifyTopicAutoJoin topicAutoJoin);

        @PUT("channels/{channelId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<ResCommon> modifyReadOnly(@Path("channelId") long topicId, @Query("teamId") long teamId, @Body Map<String, Object> map);

        // 채널 삭제
        @HTTP(path = "channels/{channelId}", method = "DELETE", hasBody = true)
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> deleteTopic(@Path("channelId") long channelId, @Body ReqDeleteTopic reqDeleteTopic);

        // 채널 Join
        @PUT("channels/{channelId}/join")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> joinTopic(@Path("channelId") long channelId, @Body ReqDeleteTopic reqDeleteTopic);

        // 채널 leave
        @PUT("channels/{channelId}/leave")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> leaveTopic(@Path("channelId") long channelId, @Body ReqDeleteTopic reqDeleteTopic);

        // 채널 invite
        @PUT("channels/{channelId}/invite")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> invitePublicTopic(@Path("channelId") long channelId, @Body ReqInviteTopicUsers reqInviteTopicUsers);
    }
}

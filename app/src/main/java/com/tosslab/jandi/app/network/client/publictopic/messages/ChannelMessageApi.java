package com.tosslab.jandi.app.network.client.publictopic.messages;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
import com.tosslab.jandi.app.network.models.ReqSendMessages;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class ChannelMessageApi extends ApiTemplate<ChannelMessageApi.Api> {
    public ChannelMessageApi(RetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public ResMessages getPublicTopicMessages(long teamId, long channelId,
                                              long fromId, int count) throws RetrofitException {
        return call(() -> getApi().getPublicTopicMessages(channelId, teamId, fromId, count));
    }

    public ResMessages getPublicTopicMessages(int teamId, int channelId) throws RetrofitException {
        return call(() -> getApi().getPublicTopicMessages(channelId, teamId));
    }

    // 채널의 업데이트 Message 리스트 정보 획득
    public ResUpdateMessages getPublicTopicUpdatedMessages(int teamId, int channelId,
                                                           int currentLinkId) throws RetrofitException {
        return call(() -> getApi().getPublicTopicUpdatedMessages(channelId, currentLinkId, teamId));
    }

    public ResMessages getPublicTopicUpdatedMessagesForMarker(long teamId, long channelId,
                                                              long currentLinkId) throws RetrofitException {
        return call(() -> getApi().getPublicTopicUpdatedMessagesForMarker(channelId, teamId, currentLinkId));
    }

    public ResMessages getPublicTopicUpdatedMessagesForMarker(long teamId, long channelId,
                                                              long currentLinkId, int count) throws RetrofitException {
        return call(() -> getApi().getPublicTopicUpdatedMessagesForMarker(channelId, teamId, currentLinkId, count));
    }


    public ResMessages getPublicTopicMarkerMessages(long teamId, long channelId,
                                                    long currentLinkId, int count) throws RetrofitException {
        return call(() -> getApi().getPublicTopicMarkerMessages(channelId, teamId, currentLinkId, count));
    }

    // 채널에서 Message 수정
    public ResCommon modifyPublicTopicMessage(ReqModifyMessage message, int channelId,
                                              int messageId) throws RetrofitException {
        return call(() -> getApi().modifyPublicTopicMessage(channelId, messageId, message));
    }

    // 채널에서 Message 삭제
    public ResCommon deletePublicTopicMessage(long teamId, long channelId,
                                              long messageId) throws RetrofitException {
        return call(() -> getApi().deletePublicTopicMessage(channelId, messageId, teamId));
    }


    interface Api {

        // 채널에서 Message 리스트 정보 획득
        @GET("channels/{channelId}/messages?type=old")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResMessages> getPublicTopicMessages(@Path("channelId") long channelId, @Query("teamId") long teamId,
                                                 @Query("linkId") long fromId, @Query("count") int count);

        @GET("channels/{channelId}/messages?type=old")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResMessages> getPublicTopicMessages(@Path("channelId") int channelId, @Query("teamId") int teamId);

        // 채널의 업데이트 Message 리스트 정보 획득
        @GET("channels/{channelId}/messages/update/{linkId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<ResUpdateMessages> getPublicTopicUpdatedMessages(@Path("channelId") int channelId, @Path("linkId") int currentLinkId, @Query("teamId") int teamId);

        @GET("channels/{channelId}/messages?type=new")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResMessages> getPublicTopicUpdatedMessagesForMarker(@Path("channelId") long channelId, @Query("teamId") long teamId,
                                                                 @Query("linkId") long currentLinkId);

        @GET("channels/{channelId}/messages?type=new")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResMessages> getPublicTopicUpdatedMessagesForMarker(@Path("channelId") long channelId, @Query("teamId") long teamId,
                                                                 @Query("linkId") long currentLinkId, @Query("count") int count);


        @GET("channels/{channelId}/messages")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResMessages> getPublicTopicMarkerMessages(@Path("channelId") long channelId, @Query("teamId") long teamId,
                                                       @Query("linkId") long currentLinkId, @Query("count") int count);

        // 채널에서 Message 수정
        @PUT("channels/{channelId}/messages/{messageId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> modifyPublicTopicMessage(@Path("channelId") int channelId, @Path("messageId") int messageId, @Body ReqModifyMessage message);

        // 채널에서 Message 삭제
        @HTTP(path = "channels/{channelId}/messages/{messageId}", hasBody = true, method = "DELETE")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> deletePublicTopicMessage(@Path("channelId") long channelId, @Path("messageId") long messageId, @Query("teamId") long teamId);

    }
}

package com.tosslab.jandi.app.network.client.publictopic.messages;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
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
    public ChannelMessageApi() {
        super(Api.class);
    }

    public ResMessages getPublicTopicMessages(long teamId, long channelId,
                                       long fromId, int count) throws RetrofitException {
        return call(() -> getApi().getPublicTopicMessages(teamId, channelId, fromId, count));
    }

    public ResMessages getPublicTopicMessages(int teamId, int channelId) throws RetrofitException {
        return call(() -> getApi().getPublicTopicMessages(teamId, channelId));
    }

    // 채널의 업데이트 Message 리스트 정보 획득
    public ResUpdateMessages getPublicTopicUpdatedMessages(int teamId, int channelId,
                                                    int currentLinkId) throws RetrofitException {
        return call(() -> getApi().getPublicTopicUpdatedMessages(teamId, channelId, currentLinkId));
    }

    public ResMessages getPublicTopicUpdatedMessagesForMarker(long teamId, long channelId,
                                                       long currentLinkId) throws RetrofitException {
        return call(() -> getApi().getPublicTopicUpdatedMessagesForMarker(teamId, channelId, currentLinkId));
    }

    public ResMessages getPublicTopicUpdatedMessagesForMarker(long teamId, long channelId,
                                                       long currentLinkId, int count) throws RetrofitException {
        return call(() -> getApi().getPublicTopicUpdatedMessagesForMarker(teamId, channelId, currentLinkId));
    }


    public ResMessages getPublicTopicMarkerMessages(long teamId, long channelId,
                                             long currentLinkId) throws RetrofitException {
        return call(() -> getApi().getPublicTopicMarkerMessages(teamId, channelId, currentLinkId));
    }

    // 채널에서 Message 생성
    public ResCommon sendPublicTopicMessage(long channelId, long teamId,
                                     ReqSendMessageV3 reqSendMessageV3) throws RetrofitException {
        return call(() -> getApi().sendPublicTopicMessage(channelId, teamId, reqSendMessageV3));
    }

    // 채널에서 Message 수정
    public ResCommon modifyPublicTopicMessage(ReqModifyMessage message, int channelId,
                                       int messageId) throws RetrofitException {
        return call(() -> getApi().modifyPublicTopicMessage(message, channelId, messageId));
    }

    // 채널에서 Message 삭제
    public ResCommon deletePublicTopicMessage(long teamId, long channelId,
                                       long messageId) throws RetrofitException {
        return call(() -> getApi().deletePublicTopicMessage(teamId, channelId, messageId));
    }


    interface Api {

        // 채널에서 Message 리스트 정보 획득
        @GET("/channels/{channelId}/messages?type=old")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResMessages> getPublicTopicMessages(@Query("teamId") long teamId, @Path("channelId") long channelId,
                                                 @Query("linkId") long fromId, @Query("count") int count);

        @GET("/channels/{channelId}/messages?type=old")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResMessages> getPublicTopicMessages(@Query("teamId") int teamId, @Path("channelId") int channelId);

        // 채널의 업데이트 Message 리스트 정보 획득
        @GET("/channels/{channelId}/messages/update/{linkId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<ResUpdateMessages> getPublicTopicUpdatedMessages(@Query("teamId") int teamId, @Path("channelId") int channelId,
                                                              @Path("linkId") int currentLinkId);

        @GET("/channels/{channelId}/messages?type=new")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResMessages> getPublicTopicUpdatedMessagesForMarker(@Query("teamId") long teamId, @Path("channelId") long channelId,
                                                                 @Query("linkId") long currentLinkId);

        @GET("/channels/{channelId}/messages?type=new")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResMessages> getPublicTopicUpdatedMessagesForMarker(@Query("teamId") long teamId, @Path("channelId") long channelId,
                                                                 @Query("linkId") long currentLinkId, @Query("count") int count);


        @GET("/channels/{channelId}/messages")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResMessages> getPublicTopicMarkerMessages(@Query("teamId") long teamId, @Path("channelId") long channelId,
                                                       @Query("linkId") long currentLinkId);

        // 채널에서 Message 생성
        @POST("/channels/{channelId}/message")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<ResCommon> sendPublicTopicMessage(@Path("channelId") long channelId, @Query("teamId") long teamId,
                                               @Body ReqSendMessageV3 reqSendMessageV3);

        // 채널에서 Message 수정
        @PUT("/channels/{channelId}/messages/{messageId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> modifyPublicTopicMessage(@Body ReqModifyMessage message, @Path("channelId") int channelId,
                                                 @Path("messageId") int messageId);

        // 채널에서 Message 삭제
        @HTTP(path = "/channels/{channelId}/messages/{messageId}", hasBody = true, method = "DELETE")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> deletePublicTopicMessage(@Query("teamId") long teamId, @Path("channelId") long channelId,
                                                 @Path("messageId") long messageId);

    }
}

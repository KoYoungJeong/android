package com.tosslab.jandi.app.network.client.publictopic.messages;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class ChannelMessageApi extends ApiTemplate<ChannelMessageApi.Api> {
    @Inject
    public ChannelMessageApi(InnerApiRetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public ResMessages getPublicTopicMessages(long teamId, long channelId,
                                              long fromId, int count) throws RetrofitException {
        return call(() -> getApi().getPublicTopicMessages(channelId, teamId, fromId, count));
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

        // 채널에서 Message 삭제
        @HTTP(path = "channels/{channelId}/messages/{messageId}", hasBody = true, method = "DELETE")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> deletePublicTopicMessage(@Path("channelId") long channelId, @Path("messageId") long messageId, @Query("teamId") long teamId);

    }
}

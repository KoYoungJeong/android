package com.tosslab.jandi.app.network.client.privatetopic.messages;

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

public class GroupMessageApi extends ApiTemplate<GroupMessageApi.Api> {
    @Inject
    public GroupMessageApi(InnerApiRetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public ResMessages getGroupMessages(long teamId, long groupId,
                                        long fromId, int count) throws RetrofitException {
        return call(() -> getApi().getGroupMessages(groupId, teamId, fromId, count));
    }

    public ResMessages getGroupMessagesUpdatedForMarker(long teamId, long groupId,
                                                        long currentLinkId) throws RetrofitException {
        return call(() -> getApi().getGroupMessagesUpdatedForMarker(groupId, teamId, currentLinkId));
    }

    public ResMessages getGroupMessagesUpdatedForMarker(long teamId, long groupId,
                                                        long currentLinkId, int count) throws RetrofitException {
        return call(() -> getApi().getGroupMessagesUpdatedForMarker(groupId, teamId, currentLinkId, count));
    }

    // Updated 된 Private Group의 리스트 정보 획득
    public ResMessages getGroupMarkerMessages(long teamId, long groupId,
                                              long currentLinkId, int count) throws RetrofitException {
        return call(() -> getApi().getGroupMarkerMessages(groupId, teamId, currentLinkId, count));
    }

    // Private Group Message 삭제
    public ResCommon deletePrivateGroupMessage(long teamId, long groupId,
                                               long messageId) throws RetrofitException {
        return call(() -> getApi().deletePrivateGroupMessage(groupId, messageId, teamId));
    }


    interface Api {

        // Private Group의 Message 리스트 정보 획득
        @GET("privateGroups/{groupId}/messages?type=old")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResMessages> getGroupMessages(@Path("groupId") long groupId, @Query("teamId") long teamId,
                                           @Query("linkId") long fromId, @Query("count") int count);

        @GET("privateGroups/{groupId}/messages?type=new")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResMessages> getGroupMessagesUpdatedForMarker(@Path("groupId") long groupId, @Query("teamId") long teamId,
                                                           @Query("linkId") long currentLinkId);

        @GET("privateGroups/{groupId}/messages?type=new")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResMessages> getGroupMessagesUpdatedForMarker(@Path("groupId") long groupId, @Query("teamId") long teamId,
                                                           @Query("linkId") long currentLinkId, @Query("count") int count);

        // Updated 된 Private Group의 리스트 정보 획득
        @GET("privateGroups/{groupId}/messages")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResMessages> getGroupMarkerMessages(@Path("groupId") long groupId, @Query("teamId") long teamId,
                                                 @Query("linkId") long currentLinkId, @Query("count") int count);

        // Private Group Message 삭제
        @HTTP(path = "privateGroups/{groupId}/messages/{messageId}", hasBody = true, method = "DELETE")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> deletePrivateGroupMessage(@Path("groupId") long groupId, @Path("messageId") long messageId, @Query("teamId") long teamId);

    }
}

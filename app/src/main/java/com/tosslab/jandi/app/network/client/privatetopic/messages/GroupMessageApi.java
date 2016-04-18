package com.tosslab.jandi.app.network.client.privatetopic.messages;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
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

public class GroupMessageApi extends ApiTemplate<GroupMessageApi.Api> {
    public GroupMessageApi(RetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public ResMessages getGroupMessages(long teamId, long groupId,
                                        long fromId, int count) throws RetrofitException {
        return call(() -> getApi().getGroupMessages(groupId, teamId, fromId, count));
    }

    public ResMessages getGroupMessages(int teamId, int groupId) throws RetrofitException {
        return call(() -> getApi().getGroupMessages(groupId, teamId));
    }

    public ResUpdateMessages getGroupMessagesUpdated(int teamId,
                                                     int groupId, int lastLinkId) throws RetrofitException {
        return call(() -> getApi().getGroupMessagesUpdated(groupId, lastLinkId, teamId));
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
                                              long currentLinkId) throws RetrofitException {
        return call(() -> getApi().getGroupMarkerMessages(groupId, teamId, currentLinkId));
    }

    // Private Group에서의 Message 생성
    public ResCommon sendGroupMessage(long privateGroupId, long teamId,
                                      ReqSendMessageV3 reqSendMessageV3) throws RetrofitException {
        return call(() -> getApi().sendGroupMessage(privateGroupId, teamId, reqSendMessageV3));
    }

    // Private Group Message 수정
    public ResCommon modifyPrivateGroupMessage(ReqModifyMessage message,
                                               int groupId, int messageId) throws RetrofitException {
        return call(() -> getApi().modifyPrivateGroupMessage(groupId, messageId, message));
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

        @GET("privateGroups/{groupId}/messages?type=old")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResMessages> getGroupMessages(@Path("groupId") int groupId, @Query("teamId") int teamId);

        @GET("privateGroups/{groupId}/messages/update/{lastLinkId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<ResUpdateMessages> getGroupMessagesUpdated(@Path("groupId") int groupId, @Path("lastLinkId") int lastLinkId, @Query("teamId") int teamId);

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
                                                 @Query("linkId") long currentLinkId);

        // Private Group에서의 Message 생성
        @POST("privateGroups/{privateGroupId}/message")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<ResCommon> sendGroupMessage(@Path("privateGroupId") long privateGroupId, @Query("teamId") long teamId,
                                         @Body ReqSendMessageV3 reqSendMessageV3);

        // Private Group Message 수정
        @PUT("privateGroups/{groupId}/messages/{messageId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> modifyPrivateGroupMessage(@Path("groupId") int groupId, @Path("messageId") int messageId, @Body ReqModifyMessage message);

        // Private Group Message 삭제
        @HTTP(path = "privateGroups/{groupId}/messages/{messageId}", hasBody = true, method = "DELETE")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> deletePrivateGroupMessage(@Path("groupId") long groupId, @Path("messageId") long messageId, @Query("teamId") long teamId);

    }
}

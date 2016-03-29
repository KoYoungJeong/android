package com.tosslab.jandi.app.network.client.privatetopic.messages;

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

public class GroupMessageApi extends ApiTemplate<GroupMessageApi.Api> {
    public GroupMessageApi() {
        super(Api.class);
    }

    public ResMessages getGroupMessages(long teamId, long groupId,
                                        long fromId, int count) throws RetrofitException {
        return call(() -> getApi().getGroupMessages(teamId, groupId, fromId, count));
    }

    public ResMessages getGroupMessages(int teamId, int groupId) throws RetrofitException {
        return call(() -> getApi().getGroupMessages(teamId, groupId));
    }

    public ResUpdateMessages getGroupMessagesUpdated(int teamId,
                                                     int groupId, int lastLinkId) throws RetrofitException {
        return call(() -> getApi().getGroupMessagesUpdated(teamId, groupId, lastLinkId));
    }

    public ResMessages getGroupMessagesUpdatedForMarker(long teamId, long groupId,
                                                        long currentLinkId) throws RetrofitException {
        return call(() -> getApi().getGroupMessagesUpdatedForMarker(teamId, groupId, currentLinkId));
    }

    public ResMessages getGroupMessagesUpdatedForMarker(long teamId, long groupId,
                                                        long currentLinkId, int count) throws RetrofitException {
        return call(() -> getApi().getGroupMessagesUpdatedForMarker(teamId, groupId, currentLinkId, count));
    }

    // Updated 된 Private Group의 리스트 정보 획득
    public ResMessages getGroupMarkerMessages(long teamId, long groupId,
                                              long currentLinkId) throws RetrofitException {
        return call(() -> getApi().getGroupMarkerMessages(teamId, groupId, currentLinkId));
    }

    // Private Group에서의 Message 생성
    public ResCommon sendGroupMessage(long privateGroupId, long teamId,
                                      ReqSendMessageV3 reqSendMessageV3) throws RetrofitException {
        return call(() -> getApi().sendGroupMessage(privateGroupId, teamId, reqSendMessageV3));
    }

    // Private Group Message 수정
    public ResCommon modifyPrivateGroupMessage(ReqModifyMessage message,
                                               int groupId, int messageId) throws RetrofitException {
        return call(() -> getApi().modifyPrivateGroupMessage(message, groupId, messageId));
    }

    // Private Group Message 삭제
    public ResCommon deletePrivateGroupMessage(long teamId, long groupId,
                                               long messageId) throws RetrofitException {
        return call(() -> getApi().deletePrivateGroupMessage(teamId, groupId, messageId));
    }


    interface Api {

        // Private Group의 Message 리스트 정보 획득
        @GET("/privateGroups/{groupId}/messages?type=old")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResMessages> getGroupMessages(@Query("teamId") long teamId, @Path("groupId") long groupId,
                                           @Query("linkId") long fromId, @Query("count") int count);

        @GET("/privateGroups/{groupId}/messages?type=old")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResMessages> getGroupMessages(@Query("teamId") int teamId, @Path("groupId") int groupId);

        @GET("/privateGroups/{groupId}/messages/update/{lastLinkId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<ResUpdateMessages> getGroupMessagesUpdated(@Query("teamId") int teamId,
                                                        @Path("groupId") int groupId, @Path("lastLinkId") int lastLinkId);

        @GET("/privateGroups/{groupId}/messages?type=new")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResMessages> getGroupMessagesUpdatedForMarker(@Query("teamId") long teamId, @Path("groupId") long groupId,
                                                           @Query("linkId") long currentLinkId);

        @GET("/privateGroups/{groupId}/messages?type=new")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResMessages> getGroupMessagesUpdatedForMarker(@Query("teamId") long teamId, @Path("groupId") long groupId,
                                                           @Query("linkId") long currentLinkId, @Query("count") int count);

        // Updated 된 Private Group의 리스트 정보 획득
        @GET("/privateGroups/{groupId}/messages")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResMessages> getGroupMarkerMessages(@Query("teamId") long teamId, @Path("groupId") long groupId,
                                                 @Query("linkId") long currentLinkId);

        // Private Group에서의 Message 생성
        @POST("/privateGroups/{privateGroupId}/message")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<ResCommon> sendGroupMessage(@Path("privateGroupId") long privateGroupId, @Query("teamId") long teamId,
                                         @Body ReqSendMessageV3 reqSendMessageV3);

        // Private Group Message 수정
        @PUT("/privateGroups/{groupId}/messages/{messageId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> modifyPrivateGroupMessage(@Body ReqModifyMessage message,
                                                  @Path("groupId") int groupId, @Path("messageId") int messageId);

        // Private Group Message 삭제
        @HTTP(path = "/privateGroups/{groupId}/messages/{messageId}", hasBody = true, method = "DELETE")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> deletePrivateGroupMessage(@Query("teamId") long teamId, @Path("groupId") long groupId,
                                                  @Path("messageId") long messageId);

    }
}

package com.tosslab.jandi.app.network.client.privatetopic;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.AuthorizedHeader;
import com.tosslab.jandi.app.network.manager.restapiclient.annotation.DELETEWithBody;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ReqTeam;
import com.tosslab.jandi.app.network.models.ResCommon;

import retrofit.http.Body;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by tee on 15. 6. 16..
 */
@AuthorizedHeader
public interface GroupApiV2Client {

    // Private Group 생성
    @POST("/privateGroup")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
    ResCommon createPrivateGroup(@Query("teamId") int teamId, @Body ReqCreateTopic group);

    // Private Group 수정
    @PUT("/privateGroups/{groupId}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
    ResCommon modifyGroup(@Query("teamId") int teamId,
                          @Body ReqCreateTopic channel,
                          @Path("groupId") int groupId);

    // Private Group 삭제
    @DELETEWithBody("/privateGroups/{groupId}")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon deleteGroup(@Query("teamId") int teamId, @Path("groupId") int groupId);

    // Private Group Leave
    @PUT("/privateGroups/{groupId}/leave")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon leaveGroup(@Path("groupId") int groupId, @Body ReqTeam team);

    // Private Group invite
    @PUT("/privateGroups/{groupId}/invite")
    @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
    ResCommon inviteGroup(@Path("groupId") int groupId, @Body ReqInviteTopicUsers inviteUsers);

    //    // Private Group invite
//    @Put("/privateGroups/{groupId}/invite")
//    @RequiresAuthentication
//    ResCommon inviteGroup(int groupId, ReqInviteTopicUsers inviteUsers);

}

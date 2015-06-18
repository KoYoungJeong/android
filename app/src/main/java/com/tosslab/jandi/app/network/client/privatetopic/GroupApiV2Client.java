package com.tosslab.jandi.app.network.client.privatetopic;

import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ReqTeam;
import com.tosslab.jandi.app.network.models.ResCommon;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by tee on 15. 6. 16..
 */

public interface GroupApiV2Client {

    // Private Group 생성
    @POST("/privateGroup")
    ResCommon createPrivateGroup(@Body ReqCreateTopic group);

    // Private Group 수정
    @PUT("/privateGroups/{groupId}")
    ResCommon modifyGroup(@Body ReqCreateTopic channel,@Path("groupId") int groupId);

    // Private Group 삭제
    @DELETE("/privateGroups/{groupId}")
    ResCommon deleteGroup(@Query("teamId") int teamId, @Path("groupId") int groupId);

    // Private Group Leave
    @PUT("/privateGroups/{groupId}/leave")
    ResCommon leaveGroup( @Path("groupId") int groupId,@Body ReqTeam team);

    // Private Group invite
    @PUT("/privateGroups/{groupId}/invite")
    ResCommon inviteGroup(int groupId, ReqInviteTopicUsers inviteUsers);

}

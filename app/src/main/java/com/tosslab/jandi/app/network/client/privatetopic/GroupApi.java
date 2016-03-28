package com.tosslab.jandi.app.network.client.privatetopic;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ReqModifyTopicDescription;
import com.tosslab.jandi.app.network.models.ReqModifyTopicName;
import com.tosslab.jandi.app.network.models.ReqTeam;
import com.tosslab.jandi.app.network.models.ResCommon;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HTTP;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class GroupApi extends ApiTemplate<GroupApi.Api> {
    public GroupApi() {
        super(Api.class);
    }

    ResCommon createPrivateGroup(long teamId, ReqCreateTopic group) throws RetrofitException {
        return call(() -> getApi().createPrivateGroup(teamId, group));
    }

    // Private Group 수정
    ResCommon modifyGroupName(long teamId,
                              ReqModifyTopicName channel,
                              long groupId) throws RetrofitException {
        return call(() -> getApi().modifyGroupName(teamId, channel, groupId));
    }

    ResCommon modifyGroupDescription(long teamId,
                                     ReqModifyTopicDescription description,
                                     long groupId) throws RetrofitException {
        return call(() -> getApi().modifyGroupDescription(teamId, description, groupId));
    }

    // Private Group 삭제
    ResCommon deleteGroup(long teamId, long groupId) throws RetrofitException {
        return call(() -> getApi().deleteGroup(teamId, groupId));
    }

    // Private Group Leave
    ResCommon leaveGroup(long groupId, ReqTeam team) throws RetrofitException {
        return call(() -> getApi().leaveGroup(groupId, team));
    }

    // Private Group invite
    ResCommon inviteGroup(long groupId, ReqInviteTopicUsers inviteUsers) throws RetrofitException {
        return call(() -> getApi().inviteGroup(groupId, inviteUsers));
    }

    interface Api {

        // Private Group 생성
        @POST("/privateGroup")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<ResCommon> createPrivateGroup(@Query("teamId") long teamId, @Body ReqCreateTopic group);

        // Private Group 수정
        @PUT("/privateGroups/{groupId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<ResCommon> modifyGroupName(@Query("teamId") long teamId,
                                        @Body ReqModifyTopicName channel,
                                        @Path("groupId") long groupId);

        @PUT("/privateGroups/{groupId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<ResCommon> modifyGroupDescription(@Query("teamId") long teamId,
                                               @Body ReqModifyTopicDescription description,
                                               @Path("groupId") long groupId);

        // Private Group 삭제
        @HTTP(path = "/privateGroups/{groupId}", hasBody = true, method = "DELETE")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> deleteGroup(@Query("teamId") long teamId, @Path("groupId") long groupId);

        // Private Group Leave
        @PUT("/privateGroups/{groupId}/leave")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> leaveGroup(@Path("groupId") long groupId, @Body ReqTeam team);

        // Private Group invite
        @PUT("/privateGroups/{groupId}/invite")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> inviteGroup(@Path("groupId") long groupId, @Body ReqInviteTopicUsers inviteUsers);

    }
}

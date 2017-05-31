package com.tosslab.jandi.app.network.client.privatetopic;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.ApiTemplate;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.InnerApiRetrofitBuilder;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ReqModifyTopicDescription;
import com.tosslab.jandi.app.network.models.ReqModifyTopicName;
import com.tosslab.jandi.app.network.models.ReqTeam;
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

public class GroupApi extends ApiTemplate<GroupApi.Api> {
    @Inject
    public GroupApi(InnerApiRetrofitBuilder retrofitBuilder) {
        super(Api.class, retrofitBuilder);
    }

    public Topic createPrivateGroup(long teamId, ReqCreateTopic group) throws RetrofitException {
        return call(() -> getApi().createPrivateGroup(teamId, group));
    }

    // Private Group 수정
    public ResCommon modifyGroupName(long teamId,
                              ReqModifyTopicName channel,
                              long groupId) throws RetrofitException {
        return call(() -> getApi().modifyGroupName(groupId, teamId, channel));
    }

    public ResCommon modifyGroupDescription(long teamId,
                                     ReqModifyTopicDescription description,
                                     long groupId) throws RetrofitException {
        return call(() -> getApi().modifyGroupDescription(groupId, teamId, description));
    }

    // Private Group 삭제
    public ResCommon deleteGroup(long teamId, long groupId) throws RetrofitException {
        return call(() -> getApi().deleteGroup(groupId, teamId));
    }

    // Private Group Leave
    public ResCommon leaveGroup(long groupId, ReqTeam team) throws RetrofitException {
        return call(() -> getApi().leaveGroup(groupId, team));
    }

    // Private Group invite
    public ResCommon inviteGroup(long groupId, ReqInviteTopicUsers inviteUsers) throws RetrofitException {
        return call(() -> getApi().inviteGroup(groupId, inviteUsers));
    }

    public ResCommon modifyReadOnly(long teamId, long topicId, boolean readOnly) throws RetrofitException {
        Map<String, Object> map = new HashMap<>();
        map.put("isAnnouncement", readOnly);
        return call(() -> getApi().modifyReadOnly(topicId, teamId, map));
    }

    interface Api {

        // Private Group 생성
        @POST("privateGroup")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<Topic> createPrivateGroup(@Query("teamId") long teamId, @Body ReqCreateTopic group);

        // Private Group 수정
        @PUT("privateGroups/{groupId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<ResCommon> modifyGroupName(@Path("groupId") long groupId, @Query("teamId") long teamId,
                                        @Body ReqModifyTopicName channel);

        @PUT("privateGroups/{groupId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<ResCommon> modifyGroupDescription(@Path("groupId") long groupId, @Query("teamId") long teamId,
                                               @Body ReqModifyTopicDescription description);

        @PUT("privateGroups/{groupId}")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_V3)
        Call<ResCommon> modifyReadOnly(@Path("groupId") long topicId,
                                       @Query("teamId") long teamId,
                                       @Body Map<String, Object> map);

        // Private Group 삭제
        @HTTP(path = "privateGroups/{groupId}", hasBody = true, method = "DELETE")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> deleteGroup(@Path("groupId") long groupId, @Query("teamId") long teamId);

        // Private Group Leave
        @PUT("privateGroups/{groupId}/leave")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> leaveGroup(@Path("groupId") long groupId, @Body ReqTeam team);

        // Private Group invite
        @PUT("privateGroups/{groupId}/invite")
        @Headers("Accept:" + JandiConstants.HTTP_ACCEPT_HEADER_DEFAULT)
        Call<ResCommon> inviteGroup(@Path("groupId") long groupId, @Body ReqInviteTopicUsers inviteUsers);
    }
}

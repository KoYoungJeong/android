package com.tosslab.jandi.app.network.client.privatetopic;

import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ReqTeam;
import com.tosslab.jandi.app.network.models.ResCommon;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IGroupApiAuth {

    ResCommon createPrivateGroupByGroupApi(ReqCreateTopic group) throws RetrofitError;

    ResCommon modifyGroupByGroupApi(ReqCreateTopic channel, int groupId) throws RetrofitError;

    ResCommon deleteGroupByGroupApi(int teamId, int groupId) throws RetrofitError;

    ResCommon leaveGroupByGroupApi(int groupId, ReqTeam team) throws RetrofitError;

    ResCommon inviteGroupByGroupApi(int groupId, ReqInviteTopicUsers inviteUsers) throws RetrofitError;

}

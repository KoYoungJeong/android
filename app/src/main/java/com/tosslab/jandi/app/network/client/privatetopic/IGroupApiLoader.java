package com.tosslab.jandi.app.network.client.privatetopic;

import com.tosslab.jandi.app.network.manager.apiexecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ReqTeam;
import com.tosslab.jandi.app.network.models.ResCommon;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IGroupApiLoader {

    IExecutor<ResCommon> loadCreatePrivateGroupByGroupApi(ReqCreateTopic group);

    IExecutor<ResCommon> loadModifyGroupByGroupApi(ReqCreateTopic channel, int groupId);

    IExecutor<ResCommon> loadDeleteGroupByGroupApi(int teamId, int groupId);

    IExecutor<ResCommon> loadLeaveGroupByGroupApi(int groupId, ReqTeam team);

    IExecutor<ResCommon> loadInviteGroupByGroupApi(int groupId, ReqInviteTopicUsers inviteUsers);
}

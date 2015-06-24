package com.tosslab.jandi.app.network.client.privatetopic;

import com.tosslab.jandi.app.network.manager.ApiExecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ReqTeam;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IGroupApiLoader {

    IExecutor loadCreatePrivateGroupByGroupApi(ReqCreateTopic group);

    IExecutor loadModifyGroupByGroupApi(ReqCreateTopic channel, int groupId);

    IExecutor loadDeleteGroupByGroupApi(int teamId, int groupId);

    IExecutor loadLeaveGroupByGroupApi(int groupId, ReqTeam team);

    IExecutor loadInviteGroupByGroupApi(int groupId, ReqInviteTopicUsers inviteUsers);
}

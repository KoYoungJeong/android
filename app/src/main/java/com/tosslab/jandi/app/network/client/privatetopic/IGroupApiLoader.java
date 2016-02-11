package com.tosslab.jandi.app.network.client.privatetopic;

import com.tosslab.jandi.app.network.manager.apiexecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ReqModifyTopicDescription;
import com.tosslab.jandi.app.network.models.ReqModifyTopicName;
import com.tosslab.jandi.app.network.models.ReqTeam;
import com.tosslab.jandi.app.network.models.ResCommon;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IGroupApiLoader {

    IExecutor<ResCommon> loadCreatePrivateGroupByGroupApi(ReqCreateTopic group);

    IExecutor<ResCommon> loadModifyGroupByGroupApi(ReqModifyTopicName channel, long groupId);

    IExecutor<ResCommon> loadModifyGroupDescriptionByGroupApi(ReqModifyTopicDescription entityInfo, long entityId);

    IExecutor<ResCommon> loadDeleteGroupByGroupApi(long teamId, long groupId);

    IExecutor<ResCommon> loadLeaveGroupByGroupApi(long groupId, ReqTeam team);

    IExecutor<ResCommon> loadInviteGroupByGroupApi(long groupId, ReqInviteTopicUsers inviteUsers);
}

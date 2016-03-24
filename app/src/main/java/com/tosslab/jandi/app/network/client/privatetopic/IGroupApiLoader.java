package com.tosslab.jandi.app.network.client.privatetopic;

import com.tosslab.jandi.app.network.manager.apiexecutor.Executor;
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

    Executor<ResCommon> loadCreatePrivateGroupByGroupApi(ReqCreateTopic group);

    Executor<ResCommon> loadModifyGroupByGroupApi(ReqModifyTopicName channel, long groupId);

    Executor<ResCommon> loadModifyGroupDescriptionByGroupApi(ReqModifyTopicDescription entityInfo, long entityId);

    Executor<ResCommon> loadDeleteGroupByGroupApi(long teamId, long groupId);

    Executor<ResCommon> loadLeaveGroupByGroupApi(long groupId, ReqTeam team);

    Executor<ResCommon> loadInviteGroupByGroupApi(long groupId, ReqInviteTopicUsers inviteUsers);
}

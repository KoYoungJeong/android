package com.tosslab.jandi.app.network.client.privatetopic;

import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ReqModifyTopicDescription;
import com.tosslab.jandi.app.network.models.ReqModifyTopicName;
import com.tosslab.jandi.app.network.models.ReqTeam;
import com.tosslab.jandi.app.network.models.ResCommon;



/**
 * Created by tee on 15. 6. 23..
 */
public interface IGroupApiAuth {

    ResCommon createPrivateGroupByGroupApi(ReqCreateTopic group) throws IOException;

    ResCommon modifyGroupNameByGroupApi(ReqModifyTopicName channel, long groupId) throws IOException;

    ResCommon modifyGroupDescriptionByGroupApi(ReqModifyTopicDescription description, long entityId);

    ResCommon deleteGroupByGroupApi(long teamId, long groupId) throws IOException;

    ResCommon leaveGroupByGroupApi(long groupId, ReqTeam team) throws IOException;

    ResCommon inviteGroupByGroupApi(long groupId, ReqInviteTopicUsers inviteUsers) throws IOException;

}

package com.tosslab.jandi.app.network.client.publictopic;

import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqDeleteTopic;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ReqModifyTopicAutoJoin;
import com.tosslab.jandi.app.network.models.ReqModifyTopicDescription;
import com.tosslab.jandi.app.network.models.ReqModifyTopicName;
import com.tosslab.jandi.app.network.models.ResCommon;



/**
 * Created by tee on 15. 6. 23..
 */
public interface IChannelApiAuth {

    ResCommon createChannelByChannelApi(ReqCreateTopic channel) throws IOException;

    ResCommon modifyPublicTopicNameByChannelApi(ReqModifyTopicName channel, long channelId) throws IOException;
    ResCommon modifyPublicTopicDescriptionByChannelApi(ReqModifyTopicDescription description, long channelId) throws IOException;
    ResCommon modifyPublicTopicAutoJoinByChannelApi(ReqModifyTopicAutoJoin topicAutoJoin, long channelId);

    ResCommon deleteTopicByChannelApi(long channelId, ReqDeleteTopic reqDeleteTopic) throws IOException;

    ResCommon joinTopicByChannelApi(long channelId, ReqDeleteTopic reqDeleteTopic) throws IOException;

    ResCommon leaveTopicByChannelApi(long channelId, ReqDeleteTopic reqDeleteTopic) throws IOException;

    ResCommon invitePublicTopicByChannelApi(long channelId, ReqInviteTopicUsers reqInviteTopicUsers) throws IOException;

}

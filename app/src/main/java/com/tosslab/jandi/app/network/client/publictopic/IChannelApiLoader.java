package com.tosslab.jandi.app.network.client.publictopic;

import com.tosslab.jandi.app.network.manager.apiexecutor.Executor;
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
public interface IChannelApiLoader {

    Executor<ResCommon> loadCreateChannelByChannelApi(ReqCreateTopic channel);

    Executor<ResCommon> loadModifyPublicTopicNameByChannelApi(ReqModifyTopicName channel, long channelId);
    Executor<ResCommon> loadModifyPublicTopicDescriptionByChannelApi(ReqModifyTopicDescription description, long channelId) throws IOException;
    Executor<ResCommon> loadModifyPublicTopicAutoJoinByChannelApi(ReqModifyTopicAutoJoin topicAutoJoin, long channelId) throws IOException;


    Executor<ResCommon> loadDeleteTopicByChannelApi(long channelId, ReqDeleteTopic reqDeleteTopic);

    Executor<ResCommon> loadJoinTopicByChannelApi(long channelId, ReqDeleteTopic reqDeleteTopic);

    Executor<ResCommon> loadLeaveTopicByChannelApi(long channelId, ReqDeleteTopic reqDeleteTopic);

    Executor<ResCommon> loadInvitePublicTopicByChannelApi(long channelId, ReqInviteTopicUsers reqInviteTopicUsers);

}

package com.tosslab.jandi.app.network.client.publictopic;

import com.tosslab.jandi.app.network.manager.ApiExecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqDeleteTopic;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IChannelApiLoader {

    IExecutor loadCreateChannelByChannelApi(ReqCreateTopic channel);

    IExecutor loadModifyPublicTopicNameByChannelApi(ReqCreateTopic channel, int channelId);

    IExecutor loadDeleteTopicByChannelApi(int channelId, ReqDeleteTopic reqDeleteTopic);

    IExecutor loadJoinTopicByChannelApi(int channelId, ReqDeleteTopic reqDeleteTopic);

    IExecutor loadLeaveTopicByChannelApi(int channelId, ReqDeleteTopic reqDeleteTopic);

    IExecutor loadInvitePublicTopicByChannelApi(int channelId, ReqInviteTopicUsers reqInviteTopicUsers);

}

package com.tosslab.jandi.app.network.client.publictopic;

import com.tosslab.jandi.app.network.manager.apiexecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqDeleteTopic;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ResCommon;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IChannelApiLoader {

    IExecutor<ResCommon> loadCreateChannelByChannelApi(ReqCreateTopic channel);

    IExecutor<ResCommon> loadModifyPublicTopicNameByChannelApi(ReqCreateTopic channel, int channelId);

    IExecutor<ResCommon> loadDeleteTopicByChannelApi(int channelId, ReqDeleteTopic reqDeleteTopic);

    IExecutor<ResCommon> loadJoinTopicByChannelApi(int channelId, ReqDeleteTopic reqDeleteTopic);

    IExecutor<ResCommon> loadLeaveTopicByChannelApi(int channelId, ReqDeleteTopic reqDeleteTopic);

    IExecutor<ResCommon> loadInvitePublicTopicByChannelApi(int channelId, ReqInviteTopicUsers reqInviteTopicUsers);

}

package com.tosslab.jandi.app.network.client.publictopic.messages;

import com.tosslab.jandi.app.network.manager.apiexecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IChannelMessageApiLoader {

    IExecutor<ResMessages> loadGetPublicTopicMessagesByChannelMessageApi(int teamId, int channelId, int fromId, int count);

    IExecutor<ResMessages> loadGetPublicTopicMessagesByChannelMessageApi(int teamId, int channelId);

    IExecutor<ResUpdateMessages> loadGetPublicTopicUpdatedMessagesByChannelMessageApi(int teamId, int channelId, int currentLinkId);

    IExecutor<ResMessages> loadGetPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(int teamId, int channelId, int currentLinkId);

    IExecutor<ResMessages> loadGetPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(int teamId, int channelId, int currentLinkId, int count);

    IExecutor<ResMessages> loadGetPublicTopicMarkerMessagesByChannelMessageApi(int teamId, int channelId, int currentLinkId);

    IExecutor<ResCommon> loadSendPublicTopicMessageByChannelMessageApi(int channelId, int teamId, ReqSendMessageV3 reqSendMessageV3);

    IExecutor<ResCommon> loadModifyPublicTopicMessageByChannelMessageApi(ReqModifyMessage message, int channelId, int messageId);

    IExecutor<ResCommon> loadDeletePublicTopicMessageByChannelMessageApi(int teamId, int channelId, int messageId);

}

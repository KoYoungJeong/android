package com.tosslab.jandi.app.network.client.publictopic.messages;

import com.tosslab.jandi.app.network.manager.ApiExecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessage;
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

    IExecutor<ResMessages> loadGetPublicTopicMarkerMessagesByChannelMessageApi(int teamId, int channelId, int currentLinkId);

    IExecutor<ResCommon> loadSendPublicTopicMessageByChannelMessageApi(ReqSendMessage message, int channelId);

    IExecutor<ResCommon> loadModifyPublicTopicMessageByChannelMessageApi(ReqModifyMessage message, int channelId, int messageId);

    IExecutor<ResCommon> loadDeletePublicTopicMessageByChannelMessageApi(int teamId, int channelId, int messageId);

}

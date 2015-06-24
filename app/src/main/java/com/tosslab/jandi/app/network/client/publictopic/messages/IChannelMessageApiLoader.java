package com.tosslab.jandi.app.network.client.publictopic.messages;

import com.tosslab.jandi.app.network.manager.ApiExecutor.IExecutor;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessage;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IChannelMessageApiLoader {

    IExecutor loadGetPublicTopicMessagesByChannelMessageApi(int teamId, int channelId, int fromId, int count);

    IExecutor loadGetPublicTopicMessagesByChannelMessageApi(int teamId, int channelId);

    IExecutor loadGetPublicTopicUpdatedMessagesByChannelMessageApi(int teamId, int channelId, int currentLinkId);

    IExecutor loadGetPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(int teamId, int channelId, int currentLinkId);

    IExecutor loadGetPublicTopicMarkerMessagesByChannelMessageApi(int teamId, int channelId, int currentLinkId);

    IExecutor loadSendPublicTopicMessageByChannelMessageApi(ReqSendMessage message, int channelId);

    IExecutor loadModifyPublicTopicMessageByChannelMessageApi(ReqModifyMessage message, int channelId, int messageId);

    IExecutor loadDeletePublicTopicMessageByChannelMessageApi(int teamId, int channelId, int messageId);

}

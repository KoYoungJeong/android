package com.tosslab.jandi.app.network.client.publictopic.messages;

import com.tosslab.jandi.app.network.manager.apiexecutor.Executor;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessageV3;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IChannelMessageApiLoader {

    Executor<ResMessages> loadGetPublicTopicMessagesByChannelMessageApi(long teamId, long channelId, long fromId, int count);

    Executor<ResMessages> loadGetPublicTopicMessagesByChannelMessageApi(int teamId, int channelId);

    Executor<ResUpdateMessages> loadGetPublicTopicUpdatedMessagesByChannelMessageApi(int teamId, int channelId, int currentLinkId);

    Executor<ResMessages> loadGetPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(long teamId, long channelId, long currentLinkId);

    Executor<ResMessages> loadGetPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(long teamId, long channelId, long currentLinkId, int count);

    Executor<ResMessages> loadGetPublicTopicMarkerMessagesByChannelMessageApi(long teamId, long channelId, long currentLinkId);

    Executor<ResCommon> loadSendPublicTopicMessageByChannelMessageApi(long channelId, long teamId, ReqSendMessageV3 reqSendMessageV3);

    Executor<ResCommon> loadModifyPublicTopicMessageByChannelMessageApi(ReqModifyMessage message, int channelId, int messageId);

    Executor<ResCommon> loadDeletePublicTopicMessageByChannelMessageApi(long teamId, long channelId, long messageId);

}

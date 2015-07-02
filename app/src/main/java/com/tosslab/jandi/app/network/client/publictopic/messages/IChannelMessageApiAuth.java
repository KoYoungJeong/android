package com.tosslab.jandi.app.network.client.publictopic.messages;

import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessage;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IChannelMessageApiAuth {

    ResMessages getPublicTopicMessagesByChannelMessageApi(int teamId, int channelId, int fromId, int count) throws RetrofitError;

    ResMessages getPublicTopicMessagesByChannelMessageApi(int teamId, int channelId) throws RetrofitError;

    ResUpdateMessages getPublicTopicUpdatedMessagesByChannelMessageApi(int teamId, int channelId, int currentLinkId) throws RetrofitError;

    ResMessages getPublicTopicUpdatedMessagesForMarkerByChannelMessageApi(int teamId, int channelId, int currentLinkId) throws RetrofitError;

    ResMessages getPublicTopicMarkerMessagesByChannelMessageApi(int teamId, int channelId, int currentLinkId) throws RetrofitError;

    ResCommon sendPublicTopicMessageByChannelMessageApi(ReqSendMessage message, int channelId) throws RetrofitError;

    ResCommon modifyPublicTopicMessageByChannelMessageApi(ReqModifyMessage message, int channelId, int messageId) throws RetrofitError;

    ResCommon deletePublicTopicMessageByChannelMessageApi(int teamId, int channelId, int messageId) throws RetrofitError;

}

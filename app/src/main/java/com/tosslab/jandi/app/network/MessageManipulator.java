package com.tosslab.jandi.app.network;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.client.direct.message.DirectMessageApiClient;
import com.tosslab.jandi.app.network.client.privatetopic.messages.GroupMessageApiClient;
import com.tosslab.jandi.app.network.client.publictopic.messages.ChannelMessageApiClient;
import com.tosslab.jandi.app.network.models.ReqSendMessage;
import com.tosslab.jandi.app.network.models.ReqSetMarker;
import com.tosslab.jandi.app.network.models.ReqTeam;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.rest.RestService;
import org.springframework.web.client.RestClientException;

/**
 * Created by justinygchoi on 2014. 6. 5..
 */
@EBean
public class MessageManipulator {
    private static final int NUMBER_OF_MESSAGES = 20;

    @RestService
    DirectMessageApiClient directMessageApiClient;

    @RestService
    GroupMessageApiClient groupMessageApiClient;

    @RestService
    ChannelMessageApiClient channelMessageApiClient;

    @RestService
    JandiRestClient jandiRestClient;

    int mEntityType;
    int mEntityId;

    public void initEntity(String token, int entityType, int entityId) {

        directMessageApiClient.setHeader("Authorization", token);
        groupMessageApiClient.setHeader("Authorization", token);
        channelMessageApiClient.setHeader("Authorization", token);
        jandiRestClient.setHeader("Authorization", token);

        mEntityId = entityId;
        mEntityType = entityType;
    }

    public ResMessages getMessages(int firstItemId) throws RestClientException {
        switch (mEntityType) {
            case JandiConstants.TYPE_PUBLIC_TOPIC:
                return channelMessageApiClient.getPublicTopicMessages(mEntityId, firstItemId, NUMBER_OF_MESSAGES);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return directMessageApiClient.getDirectMessages(mEntityId, firstItemId, NUMBER_OF_MESSAGES);
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                return groupMessageApiClient.getGroupMessages(mEntityId, firstItemId, NUMBER_OF_MESSAGES);
            default:
                return null;

        }
    }

    public ResUpdateMessages updateMessages(int fromCurrentId) throws RestClientException {
        switch (mEntityType) {
            case JandiConstants.TYPE_PUBLIC_TOPIC:
                return channelMessageApiClient.getPublicTopicMessagesUpdated(mEntityId, fromCurrentId);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return directMessageApiClient.getDirectMessagesUpdated(mEntityId, fromCurrentId);
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                return groupMessageApiClient.getGroupMessagesUpdated(mEntityId, fromCurrentId);
            default:
                return null;
        }
    }

    public ResCommon setMarker(int lastLinkId) throws RestClientException {
        String entityType;
        switch (mEntityType) {
            case JandiConstants.TYPE_PUBLIC_TOPIC:
                entityType = ReqSetMarker.CHANNEL;
                break;
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                entityType = ReqSetMarker.USER;
                break;
            case JandiConstants.TYPE_PRIVATE_TOPIC:
            default:
                entityType = ReqSetMarker.PRIVATEGROUP;
                break;
        }
        // TODO Temp Team Id
        ReqSetMarker reqSetMarker = new ReqSetMarker(1, lastLinkId, entityType);
        return jandiRestClient.setMarker(mEntityId, reqSetMarker);
    }

    public ResCommon sendMessage(String message) throws RestClientException {
        ReqSendMessage sendingMessage = new ReqSendMessage();
        sendingMessage.type = "string";
        sendingMessage.content = message;

        switch (mEntityType) {
            case JandiConstants.TYPE_PUBLIC_TOPIC:
                return channelMessageApiClient.sendPublicTopicMessage(sendingMessage, mEntityId);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return directMessageApiClient.sendDirectMessage(sendingMessage, mEntityId);
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                return groupMessageApiClient.sendGroupMessage(sendingMessage, mEntityId);
            default:
                return null;
        }
    }

    public ResCommon deleteMessage(int messageId) throws RestClientException {
        switch (mEntityType) {
            case JandiConstants.TYPE_PUBLIC_TOPIC:
                // TODO Temp Team Id
                return channelMessageApiClient.deletePublicTopicMessage(new ReqTeam(1), mEntityId, messageId);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return directMessageApiClient.deleteDirectMessage(mEntityId, messageId);
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                return groupMessageApiClient.deletePrivateGroupMessage(mEntityId, messageId);
            default:
                return null;
        }
    }
}

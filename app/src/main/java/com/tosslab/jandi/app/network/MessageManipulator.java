package com.tosslab.jandi.app.network;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendMessage;
import com.tosslab.jandi.app.network.models.ReqSetMarker;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;

import org.springframework.web.client.RestClientException;

/**
 * Created by justinygchoi on 2014. 6. 5..
 */
public class MessageManipulator {
    private static final int NUMBER_OF_MESSAGES = 20;
    JandiRestClient mRestClient;
    int mEntityType;
    int mEntityId;

    public MessageManipulator(JandiRestClient jandiRestClient, String token,
                              int entityType, int entityId) {
        mRestClient = jandiRestClient;
        mEntityId = entityId;
        mEntityType = entityType;
        mRestClient.setHeader("Authorization", token);
    }

    public ResMessages getMessages(int firstItemId) throws RestClientException {
        switch (mEntityType) {
            case JandiConstants.TYPE_PUBLIC_TOPIC:
                return mRestClient.getChannelMessages(mEntityId, firstItemId, NUMBER_OF_MESSAGES);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return mRestClient.getDirectMessages(mEntityId, firstItemId, NUMBER_OF_MESSAGES);
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                return mRestClient.getGroupMessages(mEntityId, firstItemId, NUMBER_OF_MESSAGES);
            default:
                return null;

        }
    }

    public ResUpdateMessages updateMessages(int fromCurrentId) throws RestClientException {
        switch (mEntityType) {
            case JandiConstants.TYPE_PUBLIC_TOPIC:
                return mRestClient.getChannelMessagesUpdated(mEntityId, fromCurrentId);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return mRestClient.getDirectMessagesUpdated(mEntityId, fromCurrentId);
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                return mRestClient.getGroupMessagesUpdated(mEntityId, fromCurrentId);
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
        ReqSetMarker reqSetMarker = new ReqSetMarker(lastLinkId, entityType);
        return mRestClient.setMarker(mEntityId, reqSetMarker);
    }

    public ResCommon sendMessage(String message) throws RestClientException {
        ReqSendMessage sendingMessage = new ReqSendMessage();
        sendingMessage.type = "string";
        sendingMessage.content = message;

        switch (mEntityType) {
            case JandiConstants.TYPE_PUBLIC_TOPIC:
                return mRestClient.sendChannelMessage(sendingMessage, mEntityId);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return mRestClient.sendDirectMessage(sendingMessage, mEntityId);
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                return mRestClient.sendGroupMessage(sendingMessage, mEntityId);
            default:
                return null;
        }
    }

    public ResCommon deleteMessage(int messageId) throws RestClientException {
        switch (mEntityType) {
            case JandiConstants.TYPE_PUBLIC_TOPIC:
                return mRestClient.deleteChannelMessage(mEntityId, messageId);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return mRestClient.deleteDirectMessage(mEntityId, messageId);
            case JandiConstants.TYPE_PRIVATE_TOPIC:
                return mRestClient.deletePrivateGroupMessage(mEntityId, messageId);
            default:
                return null;
        }
    }
}

package com.tosslab.jandi.app.network;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendComment;
import com.tosslab.jandi.app.network.models.ReqSendMessage;
import com.tosslab.jandi.app.network.models.ReqSetMarker;
import com.tosslab.jandi.app.network.models.ReqShareMessage;
import com.tosslab.jandi.app.network.models.ReqUnshareMessage;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResFileDetail;
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

    public MessageManipulator(JandiRestClient jandiRestClient, String token) {
        mRestClient = jandiRestClient;
        mRestClient.setHeader("Authorization", token);
    }

    public ResMessages getMessages(int firstItemId) throws RestClientException {
        switch (mEntityType) {
            case JandiConstants.TYPE_CHANNEL:
                return mRestClient.getChannelMessages(mEntityId, firstItemId, NUMBER_OF_MESSAGES);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return mRestClient.getDirectMessages(mEntityId, firstItemId, NUMBER_OF_MESSAGES);
            case JandiConstants.TYPE_PRIVATE_GROUP:
                return mRestClient.getGroupMessages(mEntityId, firstItemId, NUMBER_OF_MESSAGES);
            default:
                return null;

        }
    }

    public ResUpdateMessages updateMessages(int fromCurrentId) throws RestClientException {
        switch (mEntityType) {
            case JandiConstants.TYPE_CHANNEL:
                return mRestClient.getChannelMessagesUpdated(mEntityId, fromCurrentId);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return mRestClient.getDirectMessagesUpdated(mEntityId, fromCurrentId);
            case JandiConstants.TYPE_PRIVATE_GROUP:
                return mRestClient.getGroupMessagesUpdated(mEntityId, fromCurrentId);
            default:
                return null;
        }
    }

    public ResCommon setMarker(int lastLinkId) throws RestClientException {
        String entityType;
        switch (mEntityType) {
            case JandiConstants.TYPE_CHANNEL:
                entityType = ReqSetMarker.CHANNEL;
                break;
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                entityType = ReqSetMarker.USER;
                break;
            case JandiConstants.TYPE_PRIVATE_GROUP:
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
            case JandiConstants.TYPE_CHANNEL:
                return mRestClient.sendChannelMessage(sendingMessage, mEntityId);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return mRestClient.sendDirectMessage(sendingMessage, mEntityId);
            case JandiConstants.TYPE_PRIVATE_GROUP:
                return mRestClient.sendGroupMessage(sendingMessage, mEntityId);
            default:
                return null;
        }
    }

    public ResCommon modifyMessage(int messageId, String message) throws RestClientException {
        ReqModifyMessage reqModifyMessage = new ReqModifyMessage();
        reqModifyMessage.content = message;

        switch (mEntityType) {
            case JandiConstants.TYPE_CHANNEL:
                return mRestClient.modifyChannelMessage(reqModifyMessage, mEntityId, messageId);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return mRestClient.modifyDirectMessage(reqModifyMessage, mEntityId, messageId);
            case JandiConstants.TYPE_PRIVATE_GROUP:
                return mRestClient.modifyPrivateGroupMessage(reqModifyMessage, mEntityId, messageId);
            default:
                return null;
        }
    }

    public ResCommon deleteMessage(int messageId) throws RestClientException {
        switch (mEntityType) {
            case JandiConstants.TYPE_CHANNEL:
                return mRestClient.deleteChannelMessage(mEntityId, messageId);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return mRestClient.deleteDirectMessage(mEntityId, messageId);
            case JandiConstants.TYPE_PRIVATE_GROUP:
                return mRestClient.deletePrivateGroupMessage(mEntityId, messageId);
            default:
                return null;
        }
    }

    public ResFileDetail getMessageDetail(int messageId) throws RestClientException {
        return mRestClient.getFileDetail(messageId);
    }

    public ResCommon sendMessageComment(int messageId, String comment) throws RestClientException {
        ReqSendComment reqSendComment = new ReqSendComment();
        reqSendComment.comment = comment;
        return mRestClient.sendMessageComment(reqSendComment, messageId);

    }

    public ResCommon shareMessage(int messageId, int cdpIdToBeShared) throws RestClientException {
        ReqShareMessage reqShareMessage = new ReqShareMessage();
        reqShareMessage.shareEntity = cdpIdToBeShared;
        return mRestClient.shareMessage(reqShareMessage, messageId);
    }

    public ResCommon unshareMessage(int messageId, int cdpIdToBeunshared) throws RestClientException {
        ReqUnshareMessage reqUnshareMessage = new ReqUnshareMessage(cdpIdToBeunshared);
        return mRestClient.unshareMessage(reqUnshareMessage, messageId);
    }

    public ResCommon modifyMessageComment(int messageId, String comment, int feedbackId) {
        ReqSendComment reqModifyComment = new ReqSendComment();
        reqModifyComment.comment = comment;
        return mRestClient.modifyMessageComment(reqModifyComment, feedbackId, messageId);
    }

    public ResCommon deleteMessageComment(int messageId, int feedbackId) {
        return mRestClient.deleteMessageComment(feedbackId, messageId);
    }

    public ResCommon deleteFile(int fileId) throws RestClientException {
        return mRestClient.deleteFile(fileId);
    }
}

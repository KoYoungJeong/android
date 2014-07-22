package com.tosslab.jandi.app.network;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.models.ReqModifyMessage;
import com.tosslab.jandi.app.network.models.ReqSendComment;
import com.tosslab.jandi.app.network.models.ReqSendMessage;
import com.tosslab.jandi.app.network.models.ReqShareMessage;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResFileDetail;
import com.tosslab.jandi.app.network.models.ResMessages;

import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 6. 5..
 */
public class MessageManipulator {
    private static final int NUMBER_OF_MESSAGES = 10;
    TossRestClient mRestClient;
    int mCdpType;
    int mCdpId;

    public MessageManipulator(TossRestClient tossRestClient, String token, int cdpType, int cdpId) {
        mRestClient = tossRestClient;
        mCdpId = cdpId;
        mCdpType = cdpType;
        mRestClient.setHeader("Authorization", token);
    }

    public MessageManipulator(TossRestClient tossRestClient, String token) {
        mRestClient = tossRestClient;
        mRestClient.setHeader("Authorization", token);
    }

    public ResMessages getMessages(int firstItemId) throws RestClientException {
        switch (mCdpType) {
            case JandiConstants.TYPE_CHANNEL:
                return mRestClient.getChannelMessages(mCdpId, firstItemId, NUMBER_OF_MESSAGES);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return mRestClient.getDirectMessages(mCdpId, firstItemId, NUMBER_OF_MESSAGES);
            case JandiConstants.TYPE_PRIVATE_GROUP:
                return mRestClient.getGroupMessages(mCdpId, firstItemId, NUMBER_OF_MESSAGES);
            default:
                return null;

        }
    }

    public ResMessages updateMessages(int fromCurrentId) throws RestClientException {
        switch (mCdpType) {
            case JandiConstants.TYPE_CHANNEL:
                return mRestClient.getChannelMessagesUpdated(mCdpId, fromCurrentId);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return mRestClient.getDirectMessagesUpdated(mCdpId, fromCurrentId);
            case JandiConstants.TYPE_PRIVATE_GROUP:
                return mRestClient.getGroupMessagesUpdated(mCdpId, fromCurrentId);
            default:
                return null;
        }
    }

    public ResCommon sendMessage(String message) throws RestClientException {
        ReqSendMessage sendingMessage = new ReqSendMessage();
        sendingMessage.type = "string";
        sendingMessage.content = message;

        switch (mCdpType) {
            case JandiConstants.TYPE_CHANNEL:
                return mRestClient.sendChannelMessage(sendingMessage, mCdpId);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return mRestClient.sendDirectMessage(sendingMessage, mCdpId);
            case JandiConstants.TYPE_PRIVATE_GROUP:
                return mRestClient.sendGroupMessage(sendingMessage, mCdpId);
            default:
                return null;
        }
    }

    public ResCommon modifyMessage(int messageId, String message) throws RestClientException {
        ReqModifyMessage reqModifyMessage = new ReqModifyMessage();
        reqModifyMessage.content = message;

        switch (mCdpType) {
            case JandiConstants.TYPE_CHANNEL:
                return mRestClient.modifyChannelMessage(reqModifyMessage, mCdpId, messageId);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return mRestClient.modifyDirectMessage(reqModifyMessage, mCdpId, messageId);
            case JandiConstants.TYPE_PRIVATE_GROUP:
                return mRestClient.modifyPrivateGroupMessage(reqModifyMessage, mCdpId, messageId);
            default:
                return null;
        }
    }

    public ResCommon deleteMessage(int messageId) throws RestClientException {
        switch (mCdpType) {
            case JandiConstants.TYPE_CHANNEL:
                return mRestClient.deleteChannelMessage(mCdpId, messageId);
            case JandiConstants.TYPE_DIRECT_MESSAGE:
                return mRestClient.deleteDirectMessage(mCdpId, messageId);
            case JandiConstants.TYPE_PRIVATE_GROUP:
                return mRestClient.deletePrivateGroupMessage(mCdpId, messageId);
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

    public ResCommon modifyMessageComment(int messageId, String comment, int feedbackId) {
        ReqSendComment reqModifyComment = new ReqSendComment();
        reqModifyComment.comment = comment;
        return mRestClient.modifyMessageComment(reqModifyComment, feedbackId, messageId);
    }

    public ResCommon deleteMessageComment(int messageId, int feedbackId) {
        return mRestClient.deleteMessageComment(feedbackId, messageId);
    }


}

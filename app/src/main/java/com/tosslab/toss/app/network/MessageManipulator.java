package com.tosslab.toss.app.network;

import android.util.Log;

import com.tosslab.toss.app.TossConstants;
import com.tosslab.toss.app.events.SelectCdpItemEvent;
import com.tosslab.toss.app.network.models.ReqModifyMessage;
import com.tosslab.toss.app.network.models.ReqSendComment;
import com.tosslab.toss.app.network.models.ReqSendMessage;
import com.tosslab.toss.app.network.models.ReqShareMessage;
import com.tosslab.toss.app.network.models.ResFileDetail;
import com.tosslab.toss.app.network.models.ResMessages;
import com.tosslab.toss.app.network.models.ResSendMessage;

import org.springframework.web.client.RestClientException;

import java.util.Date;

/**
 * Created by justinygchoi on 2014. 6. 5..
 */
public class MessageManipulator {
    private static final int NUMBER_OF_MESSAGES = 10;
    TossRestClient mRestClient;
    SelectCdpItemEvent mCurrentEvent;
    int mMessageId;

    public MessageManipulator(TossRestClient tossRestClient, SelectCdpItemEvent event, String token) {
        mRestClient = tossRestClient;
        mCurrentEvent = event;
        mRestClient.setHeader("Authorization", token);
    }

    public MessageManipulator(TossRestClient tossRestClient, String token) {
        mRestClient = tossRestClient;
        mRestClient.setHeader("Authorization", token);
    }

    public ResMessages getMessages(int firstItemId) throws RestClientException {
        switch (mCurrentEvent.type) {
            case TossConstants.TYPE_CHANNEL:
                return mRestClient.getChannelMessages(mCurrentEvent.id, firstItemId, NUMBER_OF_MESSAGES);
            case TossConstants.TYPE_DIRECT_MESSAGE:
                return mRestClient.getDirectMessages(mCurrentEvent.id, firstItemId, NUMBER_OF_MESSAGES);
            case TossConstants.TYPE_PRIVATE_GROUP:
                return mRestClient.getGroupMessages(mCurrentEvent.id, firstItemId, NUMBER_OF_MESSAGES);
            default:
                return null;

        }
    }

    public ResMessages updateMessages(Date fromNow) throws RestClientException {
        switch (mCurrentEvent.type) {
            case TossConstants.TYPE_CHANNEL:
                return mRestClient.getChannelMessagesUpdated(mCurrentEvent.id, fromNow.getTime());
            case TossConstants.TYPE_DIRECT_MESSAGE:
                return mRestClient.getDirectMessagesUpdated(mCurrentEvent.id, fromNow.getTime());
            case TossConstants.TYPE_PRIVATE_GROUP:
                return mRestClient.getGroupMessagesUpdated(mCurrentEvent.id, fromNow.getTime());
            default:
                return null;
        }
    }

    public ResSendMessage sendMessage(String message) throws RestClientException {
        ReqSendMessage sendingMessage = new ReqSendMessage();
        sendingMessage.type = "string";
        sendingMessage.content = message;

        switch (mCurrentEvent.type) {
            case TossConstants.TYPE_CHANNEL:
                return mRestClient.sendChannelMessage(sendingMessage, mCurrentEvent.id);
            case TossConstants.TYPE_DIRECT_MESSAGE:
                return mRestClient.sendDirectMessage(sendingMessage, mCurrentEvent.id);
            case TossConstants.TYPE_PRIVATE_GROUP:
                return mRestClient.sendGroupMessage(sendingMessage, mCurrentEvent.id);
            default:
                return null;
        }
    }

    public ResSendMessage modifyMessage(int messageId, String message) throws RestClientException {
        ReqModifyMessage reqModifyMessage = new ReqModifyMessage();
        reqModifyMessage.content = message;

        switch (mCurrentEvent.type) {
            case TossConstants.TYPE_CHANNEL:
                return mRestClient.modifyChannelMessage(reqModifyMessage, mCurrentEvent.id, messageId);
            case TossConstants.TYPE_DIRECT_MESSAGE:
                return mRestClient.modifyDirectMessage(reqModifyMessage, mCurrentEvent.id, messageId);
            case TossConstants.TYPE_PRIVATE_GROUP:
                return mRestClient.modifyPrivateGroupMessage(reqModifyMessage, mCurrentEvent.id, messageId);
            default:
                return null;
        }
    }

    public ResSendMessage deleteMessage(int messageId) throws RestClientException {
        switch (mCurrentEvent.type) {
            case TossConstants.TYPE_CHANNEL:
                return mRestClient.deleteChannelMessage(mCurrentEvent.id, messageId);
            case TossConstants.TYPE_DIRECT_MESSAGE:
                return mRestClient.deleteDirectMessage(mCurrentEvent.id, messageId);
            case TossConstants.TYPE_PRIVATE_GROUP:
                return mRestClient.deletePrivateGroupMessage(mCurrentEvent.id, messageId);
            default:
                return null;
        }
    }

    public ResFileDetail getMessageDetail(int messageId) throws RestClientException {
        return mRestClient.getFileDetail(messageId);
    }

    public ResSendMessage sendMessageComment(int messageId, String comment) throws RestClientException {
        ReqSendComment reqSendComment = new ReqSendComment();
        reqSendComment.comment = comment;
        return mRestClient.sendMessageComment(reqSendComment, messageId);

    }

    public ResSendMessage shareMessage(int messageId, int cdpIdToBeShared) throws RestClientException {
        ReqShareMessage reqShareMessage = new ReqShareMessage();
        reqShareMessage.shareEntity = cdpIdToBeShared;
        return mRestClient.shareMessage(reqShareMessage, messageId);
    }

    public ResSendMessage modifyMessageComment(int messageId, String comment, int feedbackId) {
        ReqSendComment reqModifyComment = new ReqSendComment();
        reqModifyComment.comment = comment;
        return mRestClient.modifyMessageComment(reqModifyComment, feedbackId, messageId);
    }

    public ResSendMessage deleteMessageComment(int messageId, int feedbackId) {
        return mRestClient.deleteMessageComment(feedbackId, messageId);
    }

}

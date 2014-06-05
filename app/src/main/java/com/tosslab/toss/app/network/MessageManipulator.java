package com.tosslab.toss.app.network;

import android.util.Log;

import com.tosslab.toss.app.events.ChooseNaviActionEvent;
import com.tosslab.toss.app.network.entities.ReqModifyCdpMessage;
import com.tosslab.toss.app.network.entities.ReqSendCdpMessage;
import com.tosslab.toss.app.network.entities.ResSendCdpMessage;

import org.springframework.web.client.RestClientException;

/**
 * Created by justinygchoi on 2014. 6. 5..
 */
public class MessageManipulator {
    private static final String TAG = "MessageManipulator";
    TossRestClient mRestClient;
    ChooseNaviActionEvent mCurrentEvent;

    public MessageManipulator(TossRestClient tossRestClient, ChooseNaviActionEvent event, String token) {
        mRestClient = tossRestClient;
        mCurrentEvent = event;
        mRestClient.setHeader("Authorization", token);
    }

    public ResSendCdpMessage sendMessage(String message) throws RestClientException {
        ReqSendCdpMessage sendingMessage = new ReqSendCdpMessage();
        sendingMessage.type = "string";
        sendingMessage.content = message;

        if (mCurrentEvent.type == ChooseNaviActionEvent.TYPE_CHENNEL) {
            return mRestClient.sendChannelMessage(sendingMessage, mCurrentEvent.id);
        } else if (mCurrentEvent.type == ChooseNaviActionEvent.TYPE_PRIVATE_GROUP) {
            return mRestClient.sendGroupMessage(sendingMessage, mCurrentEvent.id);
        } else if (mCurrentEvent.type == ChooseNaviActionEvent.TYPE_DIRECT_MESSAGE) {
            return mRestClient.sendDirectMessage(sendingMessage, mCurrentEvent.userId);
        }
        return null;
    }

    public ResSendCdpMessage modifyMessage(int messageId, String message) throws RestClientException {
        ReqModifyCdpMessage reqModifyCdpMessage = new ReqModifyCdpMessage();
        reqModifyCdpMessage.content = message;

        if (mCurrentEvent.type == ChooseNaviActionEvent.TYPE_CHENNEL) {
            return mRestClient.modifyChannelMessage(reqModifyCdpMessage, mCurrentEvent.id, messageId);
        } else if (mCurrentEvent.type == ChooseNaviActionEvent.TYPE_PRIVATE_GROUP) {
            return mRestClient.modifyPrivateGroupMessage(reqModifyCdpMessage, mCurrentEvent.id, messageId);
        } else if (mCurrentEvent.type == ChooseNaviActionEvent.TYPE_DIRECT_MESSAGE) {
            return mRestClient.modifyDirectMessage(reqModifyCdpMessage, mCurrentEvent.userId, messageId);
        }
        return null;
    }

    public ResSendCdpMessage deleteMessage(int messageId) throws RestClientException {
        if (mCurrentEvent.type == ChooseNaviActionEvent.TYPE_CHENNEL) {
            return mRestClient.deleteChannelMessage(mCurrentEvent.id, messageId);
        } else if (mCurrentEvent.type == ChooseNaviActionEvent.TYPE_PRIVATE_GROUP) {
            return mRestClient.deletePrivateGroupMessage(mCurrentEvent.id, messageId);
        } else if (mCurrentEvent.type == ChooseNaviActionEvent.TYPE_DIRECT_MESSAGE) {
            return mRestClient.deleteDirectMessage(mCurrentEvent.userId, messageId);
        }
        return null;
    }

}

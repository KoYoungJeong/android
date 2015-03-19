package com.tosslab.jandi.app.ui.message.v2.loader;

import android.content.Context;

import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResUpdateMessages;
import com.tosslab.jandi.app.ui.message.to.MessageState;
import com.tosslab.jandi.app.ui.message.v2.MessageListPresenter;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.apache.log4j.Logger;

import rx.Subscription;

/**
 * Created by Steve SeongUg Jung on 15. 3. 17..
 */
public class NormalNewMessageLoader implements NewsMessageLoader {
    private static final Logger logger = Logger.getLogger(NormalNewMessageLoader.class);

    private final Context context;
    MessageListModel messageListModel;
    MessageListPresenter messageListPresenter;
    private MessageState messageState;

    private Subscription messageSubscription;

    public NormalNewMessageLoader(Context context) {
        this.context = context;
    }

    public void setMessageListModel(MessageListModel messageListModel) {
        this.messageListModel = messageListModel;
    }

    public void setMessageListPresenter(MessageListPresenter messageListPresenter) {
        this.messageListPresenter = messageListPresenter;
    }

    public void setMessageState(MessageState messageState) {
        this.messageState = messageState;
    }

    public void setMessageSubscription(Subscription messageSubscription) {
        this.messageSubscription = messageSubscription;
    }

    @Override
    public void load(int linkId) {
        if (linkId <= 0) {
            return;
        }

        messageListModel.stopRefreshTimer();

        try {
            ResUpdateMessages newMessage = messageListModel.getNewMessage(linkId);

            if (newMessage.updateInfo.messages != null && newMessage.updateInfo.messages.size() > 0) {
                int lastItemPosition = messageListPresenter.getLastItemPosition();
                messageListPresenter.addAll(lastItemPosition, newMessage.updateInfo.messages);
                messageState.setLastUpdateLinkId(newMessage.lastLinkId);
                updateMarker();

                ResMessages.Link lastUpdatedMessage = newMessage.updateInfo.messages.get(newMessage.updateInfo.messages.size() - 1);
                if (!messageListModel.isMyMessage(lastUpdatedMessage.fromEntity)) {
                    messageListPresenter.showPreviewIfNotLastItem();
                }
            }


        } catch (JandiNetworkException e) {
            logger.debug(e.getErrorInfo() + " : " + e.httpBody, e);
        } finally {
            if (!messageSubscription.isUnsubscribed()) {
                messageListModel.startRefreshTimer();
            }
        }
    }

    private void updateMarker() {
        try {
            if (messageState.getLastUpdateLinkId() > 0) {
                messageListModel.updateMarker(messageState.getLastUpdateLinkId());
            }
        } catch (JandiNetworkException e) {
            logger.error("set marker failed", e);
        } catch (Exception e) {
            logger.error("set marker failed", e);
        }
    }
}

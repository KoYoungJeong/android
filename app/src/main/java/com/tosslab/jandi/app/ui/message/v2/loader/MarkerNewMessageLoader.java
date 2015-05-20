package com.tosslab.jandi.app.ui.message.v2.loader;

import android.content.Context;

import com.tosslab.jandi.app.events.messages.ChatModeChangeEvent;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.to.MessageState;
import com.tosslab.jandi.app.ui.message.v2.MessageListPresenter;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import de.greenrobot.event.EventBus;
import rx.Subscription;

/**
 * Created by Steve SeongUg Jung on 15. 3. 17..
 */
public class MarkerNewMessageLoader implements NewsMessageLoader {

    private final Context context;
    MessageListModel messageListModel;
    MessageListPresenter messageListPresenter;
    private MessageState messageState;

    private Subscription messageSubscription;

    public MarkerNewMessageLoader(Context context) {

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

        try {
            ResMessages newMessage = messageListModel.getAfterMarkerMessage(linkId);

            boolean isLastLinkId = false;

            if (newMessage.records != null && newMessage.records.size() > 0) {
                isLastLinkId = newMessage.lastLinkId == newMessage.records.get(newMessage.records.size() - 1).id;

                messageListPresenter.addAndMove(newMessage.records);

                int lastLinkId = newMessage.records.get(newMessage.records.size() - 1).id;
                messageState.setLastUpdateLinkId(lastLinkId);
            }

            if (!isLastLinkId) {
                messageListPresenter.setNewLoadingComplete();
            } else {
                messageListPresenter.setNewNoMoreLoading();
                EventBus.getDefault().post(new ChatModeChangeEvent(false));
            }

        } catch (JandiNetworkException e) {
            LogUtil.e(e.getErrorInfo() + " : " + e.httpBody, e);
        } catch (Exception e) {
        } finally {
        }
    }

}

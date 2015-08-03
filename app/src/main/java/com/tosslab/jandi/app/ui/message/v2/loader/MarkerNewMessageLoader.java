package com.tosslab.jandi.app.ui.message.v2.loader;

import android.content.Context;

import com.tosslab.jandi.app.events.messages.ChatModeChangeEvent;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.to.MessageState;
import com.tosslab.jandi.app.ui.message.v2.MessageListPresenter;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

/**
 * Created by Steve SeongUg Jung on 15. 3. 17..
 */
public class MarkerNewMessageLoader implements NewsMessageLoader {

    private final Context context;
    MessageListModel messageListModel;
    MessageListPresenter messageListPresenter;
    private MessageState messageState;

    private boolean firstLoad = true;

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

    @Override
    public void load(int roomId, int linkId) {
        if (linkId <= 0) {
            return;
        }

        try {
            ResMessages newMessage = messageListModel.getAfterMarkerMessage(linkId);

            boolean isLastLinkId = false;

            if (newMessage.records != null) {
                if (newMessage.records.size() > 0) {
                    isLastLinkId = newMessage.lastLinkId == newMessage.records.get(newMessage.records.size() - 1).id;

                    int lastLinkId = newMessage.records.get(newMessage.records.size() - 1).id;
                    messageState.setLastUpdateLinkId(lastLinkId);
                } else {
                    isLastLinkId = true;
                }
            }

            messageListPresenter.updateMarkerNewMessage(newMessage, isLastLinkId, firstLoad);

            if (isLastLinkId) {
                EventBus.getDefault().post(new ChatModeChangeEvent(false));
            }

            firstLoad = false;


        } catch (RetrofitError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }
}

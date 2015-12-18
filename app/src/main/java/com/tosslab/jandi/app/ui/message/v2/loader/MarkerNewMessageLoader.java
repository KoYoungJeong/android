package com.tosslab.jandi.app.ui.message.v2.loader;

import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.MarkerRepository;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResRoomInfo;
import com.tosslab.jandi.app.ui.message.to.MessageState;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;
import com.tosslab.jandi.app.ui.message.v2.search.presenter.MessageSearchListPresenter;

import retrofit.RetrofitError;

public class MarkerNewMessageLoader implements NewsMessageLoader {

    MessageListModel messageListModel;
    private MessageState messageState;

    private boolean firstLoad = true;
    private MessageSearchListPresenter.View view;

    public void setMessageListModel(MessageListModel messageListModel) {
        this.messageListModel = messageListModel;
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

                    int myId = EntityManager.getInstance().getMe().getId();
                    ResRoomInfo.MarkerInfo myMarker = MarkerRepository.getRepository().getMyMarker(roomId, myId);

                    if (myMarker.getLastLinkId() < lastLinkId) {
                        messageListModel.updateMarker(messageState.getLastUpdateLinkId());
                        messageListModel.updateMarkerInfo(AccountRepository.getRepository().getSelectedTeamId(),
                                roomId);

                    }

                } else {
                    isLastLinkId = true;
                }
            }

            view.updateMarkerNewMessage(newMessage, isLastLinkId, firstLoad);

            firstLoad = false;


        } catch (RetrofitError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    public void setView(MessageSearchListPresenter.View view) {
        this.view = view;
    }
}

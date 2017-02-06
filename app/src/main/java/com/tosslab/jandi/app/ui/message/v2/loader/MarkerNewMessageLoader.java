package com.tosslab.jandi.app.ui.message.v2.loader;

import com.tosslab.jandi.app.local.orm.repositories.info.RoomMarkerRepository;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.start.Marker;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.message.to.MessageState;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;
import com.tosslab.jandi.app.ui.message.v2.search.presenter.MessageSearchListPresenter;


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
    public void load(long roomId, long linkId) {
        if (linkId <= 0) {
            return;
        }

        try {
            int itemCount = Math.min(
                    Math.max(MessageManipulator.NUMBER_OF_MESSAGES, view.getItemCount()),
                    MessageManipulator.MAX_OF_MESSAGES);
            ResMessages newMessage = messageListModel.getAfterMessage(linkId, itemCount);

            boolean isLastLinkId = false;

            if (newMessage.records != null) {
                if (newMessage.records.size() > 0) {

                    isLastLinkId = newMessage.lastLinkId == newMessage.records.get(newMessage.records.size() - 1).id;

                    long lastLinkId = newMessage.records.get(newMessage.records.size() - 1).id;
                    messageState.setLastUpdateLinkId(lastLinkId);

                    long myId = TeamInfoLoader.getInstance().getMyId();
                    Marker myMarker = RoomMarkerRepository.getInstance().getMarker(roomId, myId);

                    if (myMarker == null || myMarker.getReadLinkId() < lastLinkId) {
                        messageListModel.updateLastLinkId(messageState.getLastUpdateLinkId());
                        RoomMarkerRepository.getInstance().upsertRoomMarker(roomId, myId, messageState.getLastUpdateLinkId());
                    }

                    if (TeamInfoLoader.getInstance().isDefaultTopic(roomId)) {
                        for (int i = newMessage.records.size() - 1; i >= 0; i--) {
                            if (newMessage.records.get(i).info instanceof ResMessages.InviteEvent
                                    || newMessage.records.get(i).info instanceof ResMessages.LeaveEvent
                                    || newMessage.records.get(i).info instanceof ResMessages.JoinEvent) {
                                newMessage.records.remove(i);
                            }
                        }
                    }


                } else {
                    isLastLinkId = true;
                }
            }

            view.updateMarkerNewMessage(newMessage, isLastLinkId, firstLoad);

            firstLoad = false;


        } catch (RetrofitException e) {
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

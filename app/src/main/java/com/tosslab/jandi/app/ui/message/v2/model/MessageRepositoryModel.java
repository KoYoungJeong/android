package com.tosslab.jandi.app.ui.message.v2.model;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.client.MessageManipulator_;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public class MessageRepositoryModel {

    public static final int MAX_COUNT = 50;
    MessageManipulator messageManipulator;

    private boolean isFirst = true;

    public MessageRepositoryModel() {
        messageManipulator = MessageManipulator_.getInstance_(JandiApplication.getContext());
    }

    public void setEntityInfo(int entityType, long entityId) {
        messageManipulator.initEntity(entityType, entityId);
    }

    public List<ResMessages.Link> getMessages(long roomId, long startLinkId) {

        List<ResMessages.Link> oldMessages = MessageRepository.getRepository().getOldMessages(roomId, startLinkId, MAX_COUNT);

        if (oldMessages.isEmpty()) {
            if (!isFirst) {

                try {
                    ResMessages messages = messageManipulator.getMessages(startLinkId, MAX_COUNT);
                    oldMessages = messages.records;
                } catch (RetrofitException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    oldMessages = messageManipulator.getBeforeMarkerMessage(startLinkId, MAX_COUNT).records;
                } catch (RetrofitException e) {
                    e.printStackTrace();
                }
            }

            if (oldMessages == null) {
                oldMessages = new ArrayList<>(0);
            } else {
                MessageRepository.getRepository().upsertMessages(oldMessages);
                if (oldMessages != null && !oldMessages.isEmpty()) {
                    long firstId = oldMessages.get(0).id;
                    long lastId = oldMessages.get(oldMessages.size() - 1).id;
                    oldMessages = MessageRepository.getRepository()
                            .getMessages(roomId, firstId, lastId + 1);

                    MessageRepository.getRepository().updateDirty(roomId, firstId, lastId);
                }
            }


        } else if (oldMessages.size() < MAX_COUNT) {
            try {

                ResMessages.Link first = Observable.from(oldMessages)
                        .reduce((link, link2) -> {
                            if (link.id < link2.id) {
                                return link;
                            } else {
                                return link2;
                            }
                        }).toBlocking().first();

                ResMessages messages = messageManipulator.getMessages(first.id, MAX_COUNT - oldMessages.size());
                MessageRepository.getRepository().upsertMessages(messages.records);
                if (messages.records != null && !messages.records.isEmpty()) {
                    long firstId = messages.records.get(0).id;
                    long lastId = messages.records.get(messages.records.size() - 1).id;
                    messages.records = MessageRepository.getRepository()
                            .getMessages(roomId, firstId - 1, lastId + 1);
                    MessageRepository.getRepository().updateDirty(roomId, firstId, lastId);
                }
                oldMessages.addAll(messages.records);
            } catch (RetrofitException e) {
                e.printStackTrace();
            }
        }

        if (!oldMessages.isEmpty()) {
            isFirst = false;
        }

        return oldMessages;
    }

}

package com.tosslab.jandi.app.ui.message.v2.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.domain.SendMessage;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.SendMessageRepository;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.client.MessageManipulator_;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.message.to.DummyMessageLink;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rx.Observable;

public class MessageRepositoryModel {

    public static final int MAX_COUNT = 50;
    MessageManipulator messageManipulator;

    public MessageRepositoryModel() {
        messageManipulator = MessageManipulator_.getInstance_(JandiApplication.getContext());
    }

    public void setEntityInfo(int entityType, long entityId) {
        messageManipulator.initEntity(entityType, entityId);
    }

    public List<ResMessages.Link> getMessages(long roomId, long startLinkId) {

        List<ResMessages.Link> oldMessages = MessageRepository.getRepository().getOldMessages(roomId, startLinkId, MAX_COUNT);

        if (oldMessages.isEmpty()) {
            if (startLinkId > 0) {
                try {
                    ResMessages messages = messageManipulator.getMessages(startLinkId, MAX_COUNT);
                    oldMessages = messages.records;
                } catch (RetrofitException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    if (startLinkId < 0) {
                        oldMessages = messageManipulator.getBeforeMarkerMessage(startLinkId, MAX_COUNT).records;
                    }
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

        if (startLinkId > 0) {
            List<SendMessage> sendMessageOfRoom = SendMessageRepository.getRepository().getSendMessageOfRoom(roomId);
            List<ResMessages.Link> dummyLinks = new ArrayList<>();
            for (SendMessage sendMessage : sendMessageOfRoom) {
                DummyMessageLink dummyMessageLink;
                if (sendMessage.getStickerGroupId() > 0 && !TextUtils.isEmpty(sendMessage.getStickerId())) {
                    dummyMessageLink = new DummyMessageLink(sendMessage.getId(), sendMessage.getStatus(),
                            sendMessage.getStickerGroupId(), sendMessage.getStickerId());
                } else {
                    dummyMessageLink = new DummyMessageLink(sendMessage.getId(), sendMessage.getMessage(),
                            sendMessage.getStatus(), new ArrayList<>(sendMessage.getMentionObjects()));
                }
                dummyMessageLink.message.writerId = TeamInfoLoader.getInstance().getMyId();
                dummyMessageLink.message.createTime = new Date();
                dummyMessageLink.id = sendMessage.getMessageId();

                dummyLinks.add(dummyMessageLink);
            }
            oldMessages.addAll(dummyLinks);
        }

        if (TeamInfoLoader.getInstance().isDefaultTopic(roomId)) {
            for (int i = oldMessages.size() - 1; i >= 0; i--) {
                if (oldMessages.get(i).info instanceof ResMessages.InviteEvent
                        || oldMessages.get(i).info instanceof ResMessages.LeaveEvent
                        || oldMessages.get(i).info instanceof ResMessages.JoinEvent) {
                    oldMessages.remove(i);
                }
            }
        }

        return oldMessages;
    }

    public List<ResMessages.Link> getAfterMessages(long startLinkId, long roomId) {
        List<ResMessages.Link> messages = new ArrayList<>();
        boolean hasMore = true;

        ResMessages afterMarkerMessage;
        long linkId = startLinkId;
        while (hasMore) {
            try {
                afterMarkerMessage = messageManipulator.getAfterMarkerMessage(linkId, MessageManipulator.MAX_OF_MESSAGES);
            } catch (RetrofitException retrofitError) {
                return messages;
            }

            if (afterMarkerMessage != null
                    && afterMarkerMessage.records != null
                    && !afterMarkerMessage.records.isEmpty()) {
                messages.addAll(afterMarkerMessage.records);
                long lastLinkId = afterMarkerMessage.records.get(afterMarkerMessage.records.size() - 1).id;

                hasMore = lastLinkId < afterMarkerMessage.lastLinkId;
                linkId = lastLinkId;
            } else {
                hasMore = false;
            }
        }

        if (TeamInfoLoader.getInstance().isDefaultTopic(roomId)) {
            for (int i = messages.size() - 1; i >= 0; i--) {
                if (messages.get(i).info instanceof ResMessages.LeaveEvent
                        || messages.get(i).info instanceof ResMessages.InviteEvent
                        || messages.get(i).info instanceof ResMessages.JoinEvent) {
                    messages.remove(i);
                }
            }
        }

        return messages;
    }

}

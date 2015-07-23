package com.tosslab.jandi.app.ui.message.model;

import android.content.Context;

import com.tosslab.jandi.app.lists.messages.MessageItem;
import com.tosslab.jandi.app.lists.messages.MessageItemListAdapter;
import com.tosslab.jandi.app.local.orm.domain.ReadyMessage;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.models.ResMessages;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 1. 7..
 */
@Deprecated
@EBean
public class MessageListModel {

    @RootContext
    Context context;

    @Background
    public void saveMessagesForCache(int teamId, int entityId, MessageItemListAdapter messageItemListAdapter) {

        int count = messageItemListAdapter.getCount();

        int lastSaveIndex = Math.max(0, count - 20);

        List<ResMessages.Link> links = new ArrayList<ResMessages.Link>();

        for (int idx = lastSaveIndex; idx < count; ++idx) {
            MessageItem item = messageItemListAdapter.getItem(idx);

            ResMessages.Link link = item.getLink();
            if (link != null) {
                links.add(link);
            }
        }

        MessageRepository.getRepository().upsertMessages(links);
    }

    public List<ResMessages.Link> getCachedMessage(int entityId) {
        return MessageRepository.getRepository().getMessages(entityId);
    }

    public void saveTempMessage(int entityId, String message) {
        ReadyMessage readyMessage = new ReadyMessage();
        readyMessage.setRoomId(entityId);
        readyMessage.setText(message);
        MessageRepository.getRepository().upsertReadyMessage(readyMessage);
    }

    public String getTempMessage(int entityId) {
        return MessageRepository.getRepository().getReadyMessage(entityId).getText();
    }
}

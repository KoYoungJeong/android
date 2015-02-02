package com.tosslab.jandi.app.ui.message.model;

import android.content.Context;

import com.tosslab.jandi.app.lists.messages.MessageItem;
import com.tosslab.jandi.app.lists.messages.MessageItemListAdapter;
import com.tosslab.jandi.app.local.database.message.JandiMessageDatabaseManager;
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

        JandiMessageDatabaseManager.getInstance(context).upsertMessage(teamId, entityId, links);
    }

    public List<ResMessages.Link> getCachedMessage(int teamId, int entityId) {
        return JandiMessageDatabaseManager.getInstance(context).getSavedMessages(teamId, entityId);
    }

    public void saveTempMessage(int teamId, int entityId, String message) {
        JandiMessageDatabaseManager.getInstance(context).upsertTempMessage(teamId, entityId, message);
    }

    public String getTempMessage(int teamId, int entityId) {
        return JandiMessageDatabaseManager.getInstance(context).getTempMessage(teamId, entityId);
    }
}

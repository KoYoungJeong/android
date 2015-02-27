package com.tosslab.jandi.app.ui.maintab.chat.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.local.database.chats.JandiChatsDatabaseManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.manager.RequestManager;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.ui.maintab.chat.to.ChatItem;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EBean
public class MainChatListModel {

    @RootContext
    Context context;

    public static boolean hasAlarmCount(List<ChatItem> chatItems) {

        for (ChatItem chatItem : chatItems) {
            if (chatItem.getUnread() > 0) {
                return true;
            }
        }

        return false;

    }

    public int getMemberId() {
        return JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo().getMemberId();
    }

    public int getTeamId() {
        return JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo().getTeamId();
    }

    public List<ResChat> getChatList(int memberId) throws JandiNetworkException {
        return RequestManager.newInstance(context, ChatListRequest.create(context, memberId)).request();
    }

    public List<ChatItem> convertChatItem(int teamId, List<ResChat> chatList) {

        List<ChatItem> chatItems = new ArrayList<ChatItem>();

        Iterator<ChatItem> iterator = Observable.from(chatList)
                .filter(resChat -> EntityManager.getInstance(context).getEntityById(resChat.getCompanionId()) != null)
                .map(resChat -> {

                    FormattedEntity userEntity = EntityManager.getInstance(context).getEntityById(resChat.getCompanionId());

                    ChatItem chatItem = new ChatItem();
                    chatItem.entityId(userEntity.getId())
                            .lastLinkId(resChat.getLastLinkId())
                            .lastMessage(resChat.getLastMessage())
                            .lastMessageId(resChat.getLastMessageId())
                            .name(userEntity.getName())
                            .starred(JandiEntityDatabaseManager.getInstance(context).isStarredEntity(teamId, resChat.getCompanionId()))
                            .unread(resChat.getUnread())
                            .status(TextUtils.equals(userEntity.getUser().status, "enabled"))
                            .photo(userEntity.getUserLargeProfileUrl());

                    return chatItem;
                })

                .toBlocking()
                .getIterator();

        while (iterator.hasNext()) {
            chatItems.add(iterator.next());
        }

        return chatItems;
    }

    public List<ChatItem> getSavedChatList(int teamId) {
        return JandiChatsDatabaseManager.getInstance(context).getSavedChatItems(teamId);
    }

    public void saveChatList(int teamId, List<ChatItem> chatItems) {
        JandiChatsDatabaseManager.getInstance(context).upsertChatList(teamId, chatItems);
    }
}

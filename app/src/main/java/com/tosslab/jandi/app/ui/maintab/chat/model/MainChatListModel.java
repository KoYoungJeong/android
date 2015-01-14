package com.tosslab.jandi.app.ui.maintab.chat.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.local.database.chats.JandiChatsDatabaseManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.manager.RequestManager;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.maintab.chat.to.ChatItem;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EBean
public class MainChatListModel {

    @RootContext
    Context context;

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

        Observable.from(chatList)
                .map(resChat -> {

                    ResLeftSideMenu.User userEntity = JandiEntityDatabaseManager.getInstance(context).getUserEntity(teamId, resChat.getEntityId());

                    ChatItem chatItem = new ChatItem();
                    chatItem.entityId(userEntity.id)
                            .lastLinkId(resChat.getLastLinkId())
                            .lastMessage(resChat.getLastMessage())
                            .lastMessageId(resChat.getLastMessageId())
                            .name(userEntity.name)
                            .starred(JandiEntityDatabaseManager.getInstance(context).isStarredEntity(teamId, resChat.getEntityId()))
                            .unread(resChat.getUnread())
                            .photo(!(TextUtils.isEmpty(userEntity.u_photoThumbnailUrl.largeThumbnailUrl)) ? userEntity.u_photoThumbnailUrl.largeThumbnailUrl : userEntity.u_photoUrl);

                    return chatItem;
                })
                .collect(() -> chatItems, (chatItems1, chatItem) -> chatItems1.add(chatItem))
                .subscribe()
                .unsubscribe();

        return chatItems;
    }

    public List<ChatItem> getSavedChatList(int teamId) {
        return JandiChatsDatabaseManager.getInstance(context).getSavedChatItems(teamId);
    }

    public void saveChatList(int teamId, List<ChatItem> chatItems) {
        JandiChatsDatabaseManager.getInstance(context).upsertChatList(teamId, chatItems);
    }
}

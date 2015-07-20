package com.tosslab.jandi.app.ui.maintab.chat.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.database.chats.JandiChatsDatabaseManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.ui.maintab.chat.to.ChatItem;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import retrofit.RetrofitError;
import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EBean
public class MainChatListModel {

    public boolean hasAlarmCount(List<ChatItem> chatItems) {

        for (ChatItem chatItem : chatItems) {
            if (chatItem.getUnread() > 0) {
                return true;
            }
        }

        return false;

    }

    public int getMemberId(Context context) {
        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();
        return selectedTeamInfo != null ? selectedTeamInfo.getMemberId() : -1;
    }

    public int getTeamId(Context context) {
        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();
        return selectedTeamInfo != null ? selectedTeamInfo.getTeamId() : -1;
    }

    public List<ResChat> getChatList(int memberId) throws RetrofitError {
        return RequestApiManager.getInstance().getChatListByChatApi(memberId);
    }

    public List<ChatItem> convertChatItem(Context context, int teamId, List<ResChat> chatList) {

        List<ChatItem> chatItems = new ArrayList<ChatItem>();

        Iterator<ChatItem> iterator = Observable.from(chatList)
                .filter(resChat -> EntityManager.getInstance(context).getEntityById(resChat.getCompanionId()) != null)
                .map(resChat -> {

                    FormattedEntity userEntity = EntityManager.getInstance(context).getEntityById(resChat.getCompanionId());

                    ChatItem chatItem = new ChatItem();
                    chatItem.entityId(userEntity.getId())
                            .roomId(resChat.getEntityId())
                            .lastLinkId(resChat.getLastLinkId())
                            .lastMessage(!TextUtils.equals(resChat.getLastMessageStatus(), "archived") ? resChat.getLastMessage() : context.getString(R.string.jandi_deleted_message))
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

    public List<ChatItem> getSavedChatList(Context context, int teamId) {
        return JandiChatsDatabaseManager.getInstance(context).getSavedChatItems(teamId);
    }

    public void saveChatList(Context context, int teamId, List<ChatItem> chatItems) {
        JandiChatsDatabaseManager.getInstance(context).upsertChatList(teamId, chatItems);
    }

    public int getRoomId(Context context, int teamId, int userId) {

        List<ChatItem> savedChatItems = JandiChatsDatabaseManager.getInstance(context).getSavedChatItems(teamId);

        ChatItem first = Observable.from(savedChatItems)
                .filter(chatItem -> chatItem.getEntityId() == userId)
                .firstOrDefault(new ChatItem())
                .toBlocking().first();

        return first.getRoomId();
    }

    public boolean isStarred(Context context, int entityId) {
        return EntityManager.getInstance(context).getEntityById(entityId).isStarred;
    }
}

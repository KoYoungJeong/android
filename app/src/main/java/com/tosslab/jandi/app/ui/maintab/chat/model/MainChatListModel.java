package com.tosslab.jandi.app.ui.maintab.chat.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.ChatRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.ui.maintab.chat.to.ChatItem;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
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

    public List<ChatItem> convertChatItems(Context context, int teamId, List<ResChat> chatList) {

        List<ChatItem> chatItems = new ArrayList<ChatItem>();

        Observable.from(chatList)
                .filter(resChat -> EntityManager.getInstance().getEntityById(resChat.getCompanionId()) != null)
                .map(resChat -> {

                    FormattedEntity userEntity = EntityManager.getInstance().getEntityById(resChat.getCompanionId());

                    ChatItem chatItem = new ChatItem();
                    chatItem.entityId(userEntity.getId())
                            .roomId(resChat.getEntityId())
                            .lastLinkId(resChat.getLastLinkId())
                            .lastMessage(!TextUtils.equals(resChat.getLastMessageStatus(), "archived") ? resChat.getLastMessage() : context.getString(R.string.jandi_deleted_message))
                            .lastMessageId(resChat.getLastMessageId())
                            .name(userEntity.getName())
                            .starred(EntityManager.getInstance()
                                    .getEntityById(resChat.getCompanionId()).isStarred)
                            .unread(resChat.getUnread())
                            .status(TextUtils.equals(userEntity.getUser().status, "enabled"))
                            .photo(userEntity.getUserLargeProfileUrl());

                    return chatItem;
                })
                .collect(() -> chatItems, List::add)
                .subscribe();

        return chatItems;
    }

    public ChatItem convertChatItem(Context context, int teamId, ResChat resChat) {
        FormattedEntity userEntity = EntityManager.getInstance().getEntityById(resChat.getCompanionId());

        ChatItem chatItem = new ChatItem();
        chatItem.entityId(userEntity.getId())
                .roomId(resChat.getEntityId())
                .lastLinkId(resChat.getLastLinkId())
                .lastMessage(!TextUtils.equals(resChat.getLastMessageStatus(), "archived") ? resChat.getLastMessage() : context.getString(R.string.jandi_deleted_message))
                .lastMessageId(resChat.getLastMessageId())
                .name(userEntity.getName())
                .starred(EntityManager.getInstance()
                        .getEntityById(resChat.getCompanionId()).isStarred)
                .unread(resChat.getUnread())
                .status(TextUtils.equals(userEntity.getUser().status, "enabled"))
                .photo(userEntity.getUserLargeProfileUrl());

        return chatItem;
    }

    public List<ResChat> getSavedChatList() {
        return ChatRepository.getRepository().getChats();
    }

    public void saveChatList(int teamId, List<ResChat> chatItems) {
        for (ResChat chatItem : chatItems) {
            chatItem.setTeamId(teamId);
        }
        ChatRepository.getRepository().upsertChats(chatItems);
    }

    public int getRoomId(int userId) {

        return ChatRepository.getRepository().getChat(userId).getEntityId();

    }

    public boolean isStarred(Context context, int entityId) {
        return EntityManager.getInstance().getEntityById(entityId).isStarred;
    }
}

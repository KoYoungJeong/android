package com.tosslab.jandi.app.ui.maintab.chat.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.ChatRepository;
import com.tosslab.jandi.app.network.client.chat.ChatApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResChat;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.maintab.chat.to.ChatItem;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
@EBean
public class MainChatListModel {

    @Inject
    Lazy<ChatApi> chatApi;

    @AfterInject
    void initObject() {
        DaggerApiClientComponent.create().inject(this);
    }

    public boolean hasAlarmCount(List<ChatItem> chatItems) {

        for (ChatItem chatItem : chatItems) {
            if (chatItem.getUnread() > 0) {
                return true;
            }
        }

        return false;

    }

    public long getMemberId() {
        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();
        return selectedTeamInfo != null ? selectedTeamInfo.getMemberId() : -1;
    }

    public long getTeamId() {
        return TeamInfoLoader.getInstance().getTeamId();
    }

    public List<ResChat> getChatList(long memberId) {
        try {
            return chatApi.get().getChatList(memberId);
        } catch (RetrofitException e) {
            return new ArrayList<>();
        }
    }

    public List<ChatItem> convertChatItems(Context context, List<ResChat> chatList) {

        List<ChatItem> chatItems = new ArrayList<ChatItem>();

        Observable.from(chatList)
                .filter(resChat -> TeamInfoLoader.getInstance().isUser(resChat.getCompanionId()))
                .map(resChat -> {

                    User userEntity = TeamInfoLoader.getInstance().getUser(resChat.getCompanionId());

                    ChatItem chatItem = new ChatItem();
                    chatItem.entityId(userEntity.getId())
                            .roomId(resChat.getEntityId())
                            .lastLinkId(resChat.getLastLinkId())
                            .lastMessage(!TextUtils.equals(resChat.getLastMessageStatus(), "archived") ? resChat.getLastMessage() : context.getString(R.string.jandi_deleted_message))
                            .lastMessageId(resChat.getLastMessageId())
                            .name(userEntity.getName())
                            .starred(TeamInfoLoader.getInstance().isChatStarred(resChat.getCompanionId()))
                            .unread(resChat.getUnread())
                            .status(userEntity.isEnabled())
                            .inactive(userEntity.isInactive())
                            .email(userEntity.getEmail())
                            .photo(userEntity.getPhotoUrl());


                    return chatItem;
                })
                .collect(() -> chatItems, List::add)
                .subscribe();

        return chatItems;
    }

    public List<ResChat> getSavedChatList() {
        return ChatRepository.getRepository().getChats();
    }

    public void saveChatList(long teamId, List<ResChat> chatItems) {
        for (ResChat chatItem : chatItems) {
            chatItem.setTeamId(teamId);
        }
        ChatRepository.getRepository().upsertChats(chatItems);
    }

    public long getRoomId(long userId) {

        return ChatRepository.getRepository().getChat(userId).getEntityId();

    }

    public boolean isStarred(long entityId) {
        return TeamInfoLoader.getInstance().isChatStarred(entityId);
    }

    public int getUnreadCount(List<ChatItem> chatItems) {
        int total = 0;
        int size = chatItems.size();
        for (int idx = 0; idx < size; idx++) {
            total += chatItems.get(idx).getUnread();
        }

        return total;
    }
}

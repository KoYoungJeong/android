package com.tosslab.jandi.app.ui.maintab.tabs.chat.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.DirectMessageRoom;
import com.tosslab.jandi.app.ui.maintab.tabs.chat.to.ChatItem;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public class MainChatListModel {

    public long getMemberId() {
        ResAccountInfo.UserTeam selectedTeamInfo = AccountRepository.getRepository().getSelectedTeamInfo();
        return selectedTeamInfo != null ? selectedTeamInfo.getMemberId() : -1;
    }

    public long getTeamId() {
        return TeamInfoLoader.getInstance().getTeamId();
    }

    public List<ChatItem> convertChatItems(List<DirectMessageRoom> rooms) {

        List<ChatItem> chatItems = new ArrayList<ChatItem>();

        Observable.from(rooms)
                .filter(room -> TeamInfoLoader.getInstance().isUser(room.getCompanionId()))
                .map(room -> {

                    long companionId = room.getCompanionId();

                    User userEntity = TeamInfoLoader.getInstance().getUser(companionId);

                    ChatItem chatItem = new ChatItem();
                    chatItem.entityId(userEntity.getId())
                            .roomId(room.getId())
                            .lastLinkId(room.getLastLinkId())
                            .lastMessage(!TextUtils.equals(room.getLastMessageStatus(), "archived") ? room.getLastMessage() : JandiApplication.getContext().getString(R.string.jandi_deleted_message))
                            .lastMessageId(room.getLastMessageId())
                            .name(userEntity.getName())
                            .starred(TeamInfoLoader.getInstance().isStarredUser(companionId))
                            .unread(room.getUnreadCount())
                            .status(userEntity.isEnabled())
                            .inactive(userEntity.isInactive())
                            .email(userEntity.getEmail())
                            .photo(userEntity.getPhotoUrl());


                    return chatItem;
                })
                .toSortedList((lhs, rhs) -> ((int) (rhs.getLastLinkId() - lhs.getLastLinkId())))
                .subscribe(chatItems::addAll, Throwable::printStackTrace);

        return chatItems;
    }

    public List<DirectMessageRoom> getSavedChatList() {
        return Observable.from(TeamInfoLoader.getInstance().getDirectMessageRooms())
                .filter(DirectMessageRoom::isJoined)
                .toList()
                .toBlocking()
                .firstOrDefault(new ArrayList<>());
    }

    public long getRoomId(long userId) {

        return TeamInfoLoader.getInstance().getChatId(userId);

    }

    public boolean isStarred(long entityId) {
        return TeamInfoLoader.getInstance().isStarredUser(entityId);
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

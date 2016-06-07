package com.tosslab.jandi.app.ui.maintab.chat.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.DirectMessageRoom;
import com.tosslab.jandi.app.ui.maintab.chat.to.ChatItem;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

@EBean
public class MainChatListModel {

    @AfterInject
    void initObject() {
        DaggerApiClientComponent.create().inject(this);
    }

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
                .filter(room -> Observable.from(room.getMembers())
                        .takeFirst(memberId -> memberId != TeamInfoLoader.getInstance().getMyId())
                        .map(memberId -> true)
                        .toBlocking()
                        .firstOrDefault(false)
                )
                .map(room -> {

                    long companionId = Observable.from(room.getMembers())
                            .takeFirst(memberId -> memberId != TeamInfoLoader.getInstance().getMyId())
                            .toBlocking()
                            .first();

                    User userEntity = TeamInfoLoader.getInstance().getUser(companionId);

                    ChatItem chatItem = new ChatItem();
                    chatItem.entityId(userEntity.getId())
                            .roomId(room.getId())
                            .lastLinkId(room.getLastLinkId())
                            .lastMessage(!TextUtils.equals(room.getLastMessageStatus(), "archived") ? room.getLastMessage() : JandiApplication.getContext().getString(R.string.jandi_deleted_message))
                            .lastMessageId(room.getLastMessageId())
                            .name(userEntity.getName())
                            .starred(TeamInfoLoader.getInstance().isChatStarred(companionId))
                            .unread(room.getUnreadCount())
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

    public List<DirectMessageRoom> getSavedChatList() {
        return TeamInfoLoader.getInstance().getDirectMessageRooms();
    }

    public long getRoomId(long userId) {

        return TeamInfoLoader.getInstance().getChatId(userId);

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

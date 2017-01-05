package com.tosslab.jandi.app.team.room;

import android.text.TextUtils;

import com.tosslab.jandi.app.network.models.start.Chat;
import com.tosslab.jandi.app.network.models.start.LastMessage;
import com.tosslab.jandi.app.network.models.start.Marker;
import com.tosslab.jandi.app.network.models.start.RealmLong;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DirectMessageRoom implements Room {


    private final Chat chat;

    public DirectMessageRoom(Chat chat) {
        this.chat = chat;
    }

    @Override
    public long getId() {
        return chat.getId();
    }

    @Override
    public long getTeamId() {
        return chat.getTeamId();
    }

    @Override
    public String getType() {
        return chat.getType();
    }

    @Override
    public long getLastLinkId() {
        return chat.getLastLinkId();
    }

    @Override
    public long getReadLinkId() {
        return chat.getReadLinkId();
    }

    @Override
    public int getUnreadCount() {
        return chat.getUnreadCount();
    }

    public long getCompanionId() {
        return chat.getCompanionId();
    }

    @Override
    public boolean isEnabled() {
        return TextUtils.equals(chat.getStatus(), "active");
    }

    @Override
    public boolean isPublicTopic() {
        return false;
    }

    @Override
    public boolean isChat() {
        return true;
    }

    @Override
    public boolean isJoined() {
        return chat.isOpened();
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public Collection<Marker> getMarkers() {
        return chat.getMarkers();
    }

    @Override
    public Collection<Long> getMembers() {
        List<Long> ids = new ArrayList<>();
        for (RealmLong realmLong : chat.getMemberIds()) {
            ids.add(realmLong.getValue());
        }
        return ids;
    }

    public String getLastMessageStatus() {
        LastMessage lastMessage = chat.getLastMessage();
        return lastMessage != null ? lastMessage.getStatus() : "";
    }

    public String getLastMessage() {
        LastMessage lastMessage = chat.getLastMessage();
        return lastMessage != null ? lastMessage.getText() : "";
    }

    public long getLastMessageId() {
        LastMessage lastMessage = chat.getLastMessage();
        return lastMessage != null ? lastMessage.getId() : -1;
    }
}

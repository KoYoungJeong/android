package com.tosslab.jandi.app.team.room;

import android.text.TextUtils;

import com.tosslab.jandi.app.network.models.start.Chat;
import com.tosslab.jandi.app.network.models.start.Marker;

import java.util.Collection;

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
    public Collection<Marker> getMarkers() {
        return chat.getMarkers();
    }

    @Override
    public Collection<Long> getMembers() {
        return chat.getMembers();
    }

    public String getLastMessageStatus() {
        Chat.LastMessage lastMessage = chat.getLastMessage();
        return lastMessage != null ? lastMessage.getStatus() : "";
    }

    public String getLastMessage() {
        Chat.LastMessage lastMessage = chat.getLastMessage();
        return lastMessage != null ? lastMessage.getText() : "";
    }

    public long getLastMessageId() {
        Chat.LastMessage lastMessage = chat.getLastMessage();
        return lastMessage != null ? lastMessage.getId() : -1;
    }
}

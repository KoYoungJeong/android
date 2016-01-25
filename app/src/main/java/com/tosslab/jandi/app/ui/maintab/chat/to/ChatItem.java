package com.tosslab.jandi.app.ui.maintab.chat.to;

/**
 * Created by Steve SeongUg Jung on 15. 1. 13..
 */
public class ChatItem {
    private String name;
    private boolean isStarred;
    private long lastMessageId;
    private int unread;
    private long entityId;
    private long lastLinkId;
    private String lastMessage;
    private String photo;
    private boolean status;
    private long roomId;

    public long getLastMessageId() {
        return lastMessageId;
    }

    public ChatItem lastMessageId(long lastMessageId) {
        this.lastMessageId = lastMessageId;
        return this;
    }

    public int getUnread() {
        return unread;
    }

    public ChatItem unread(int unread) {
        this.unread = unread;
        return this;
    }

    public long getEntityId() {
        return entityId;
    }

    public ChatItem entityId(long entityId) {
        this.entityId = entityId;
        return this;
    }

    public long getLastLinkId() {
        return lastLinkId;
    }

    public ChatItem lastLinkId(long lastLinkId) {
        this.lastLinkId = lastLinkId;
        return this;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public ChatItem lastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
        return this;
    }

    public String getName() {
        return name;
    }

    public ChatItem name(String name) {
        this.name = name;
        return this;
    }

    public boolean isStarred() {
        return isStarred;
    }

    public ChatItem starred(boolean isStarred) {
        this.isStarred = isStarred;
        return this;
    }

    public String getPhoto() {
        return photo;
    }

    public ChatItem photo(String photo) {
        this.photo = photo;
        return this;
    }

    public boolean getStatus() {
        return status;
    }

    public ChatItem status(boolean status) {
        this.status = status;
        return this;
    }

    public ChatItem roomId(long roomId) {
        this.roomId = roomId;
        return this;
    }

    public long getRoomId() {
        return roomId;
    }

}

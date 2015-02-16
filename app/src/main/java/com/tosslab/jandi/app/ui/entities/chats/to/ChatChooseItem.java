package com.tosslab.jandi.app.ui.entities.chats.to;

/**
 * Created by Steve SeongUg Jung on 15. 1. 14..
 */
public class ChatChooseItem {
    private int entityId;
    private String name;
    private String email;
    private String photoUrl;
    private boolean isStarred;
    private boolean isEnabled;

    public ChatChooseItem name(String name) {
        this.name = name;
        return this;
    }

    public ChatChooseItem email(String email) {
        this.email = email;
        return this;
    }

    public ChatChooseItem photoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
        return this;
    }

    public ChatChooseItem entityId(int entityId) {

        this.entityId = entityId;
        return this;
    }

    public ChatChooseItem starred(boolean isStarred) {
        this.isStarred = isStarred;
        return this;

    }

    public boolean isStarred() {
        return isStarred;
    }

    public int getEntityId() {
        return entityId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public ChatChooseItem enabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
        return this;
    }
}

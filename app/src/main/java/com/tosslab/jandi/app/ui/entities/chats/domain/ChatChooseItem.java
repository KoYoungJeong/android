package com.tosslab.jandi.app.ui.entities.chats.domain;

/**
 * Created by Steve SeongUg Jung on 15. 1. 14..
 */
public class ChatChooseItem {
    private long entityId;
    private String name;
    private String statusMessage;
    private String photoUrl;
    private String email;
    private String department;
    private String jobTitle;
    private boolean isBot = false;
    private boolean isStarred;
    private boolean isEnabled;
    private boolean isInactive;
    private boolean isChooseItem = false;
    private boolean isOwner = false;

    public ChatChooseItem name(String name) {
        this.name = name;
        return this;
    }

    public ChatChooseItem statusMessage(String email) {
        this.statusMessage = email;
        return this;
    }

    public ChatChooseItem photoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
        return this;
    }

    public ChatChooseItem entityId(long entityId) {

        this.entityId = entityId;
        return this;
    }

    public ChatChooseItem starred(boolean isStarred) {
        this.isStarred = isStarred;
        return this;

    }

    public ChatChooseItem owner(boolean isOwner) {
        this.isOwner = isOwner;
        return this;

    }

    public ChatChooseItem department(String department) {
        this.department = department;
        return this;
    }

    public ChatChooseItem jobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
        return this;
    }

    public boolean isStarred() {
        return isStarred;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public long getEntityId() {
        return entityId;
    }

    public String getName() {
        return name;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public String getDepartment() {
        return department;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public ChatChooseItem enabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
        return this;
    }

    public boolean isChooseItem() {
        return isChooseItem;
    }

    public void setIsChooseItem(boolean isChooseItem) {
        this.isChooseItem = isChooseItem;
    }

    public boolean isBot() {
        return isBot;
    }

    public ChatChooseItem isBot(boolean isBot) {
        this.isBot = isBot;
        return this;
    }

    public boolean isInactive() {
        return isInactive;
    }

    public ChatChooseItem inactive(boolean isInactive) {
        this.isInactive = isInactive;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public ChatChooseItem email(String email) {
        this.email = email;
        return this;
    }
}

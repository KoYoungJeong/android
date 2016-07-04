package com.tosslab.jandi.app.ui.selector.room.domain;

import android.support.annotation.Nullable;

import com.tosslab.jandi.app.team.member.Member;

public class ExpandRoomData {

    private long entityId;
    private String name;
    private boolean isFolder;
    private boolean isUser;
    private boolean isStarred;
    private boolean isPublicTopic;
    private String profileUrl;
    private boolean enabled;
    private boolean inactive;
    private String email;
    private int type;
    private boolean isFirstAmongNoFolderItem;

    public static ExpandRoomData newMemberData(Member member) {
        ExpandRoomData userData = new ExpandRoomData();
        userData.setIsUser(true);
        userData.setName(member.getName());
        userData.setEnabled(member.isEnabled());
        try {
            userData.setProfileUrl(member.getPhotoUrl());
        } catch (Exception e) {
            e.printStackTrace();
        }
        userData.setInactive(member.isInactive());
        userData.setEmail(member.getEmail());
        userData.setEntityId(member.getId());
        userData.setIsStarred(member.isStarred());
        userData.setIsFolder(false);
        return userData;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    public boolean isPublicTopic() {
        return isPublicTopic;
    }

    public void setIsPublicTopic(boolean isPublicTopic) {
        this.isPublicTopic = isPublicTopic;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setIsUser(boolean isUser) {
        this.isUser = isUser;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setIsFolder(boolean isFolder) {
        this.isFolder = isFolder;
    }

    public boolean isStarred() {
        return isStarred;
    }

    public void setIsStarred(boolean isStarred) {
        this.isStarred = isStarred;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(@Nullable String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public boolean isFirstAmongNoFolderItem() {
        return isFirstAmongNoFolderItem;
    }

    public void setIsFirstAmongNoFolderItem(boolean isFirstAmongNoFolderItem) {
        this.isFirstAmongNoFolderItem = isFirstAmongNoFolderItem;
    }

    @Override
    public String toString() {
        return "ExpandRoomData{" +
                "entityId=" + entityId +
                ", name='" + name + '\'' +
                ", isFolder=" + isFolder +
                ", isUser=" + isUser +
                ", isStarred=" + isStarred +
                ", isPublicTopic=" + isPublicTopic +
                ", profileUrl='" + profileUrl + '\'' +
                ", type=" + type +
                ", isFirstAmongNoFolderItem=" + isFirstAmongNoFolderItem +
                '}';
    }

    public boolean isInactive() {
        return inactive;
    }

    public void setInactive(boolean inactive) {
        this.inactive = inactive;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public static class DummyDisabledRoomData extends ExpandRoomData {
        private boolean expanded;
        private int count;

        public DummyDisabledRoomData(int count) {
            this.count = count;
            setEnabled(false);
            setIsUser(true);
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public boolean isExpanded() {
            return expanded;
        }

        public void setExpanded(boolean expanded) {
            this.expanded = expanded;
        }
    }
}

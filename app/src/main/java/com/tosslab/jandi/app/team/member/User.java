package com.tosslab.jandi.app.team.member;

import android.text.TextUtils;

import com.tosslab.jandi.app.network.models.start.Human;

public class User implements Member {
    private final Human human;

    public User(Human human) {
        this.human = human;
    }

    @Override
    public long getId() {
        return human.getId();
    }

    @Override
    public String getName() {
        return human.getName();
    }

    @Override
    public String getPhotoUrl() {
        return human.getPhotoUrl();
    }

    @Override
    public boolean isEnabled() {
        return TextUtils.equals(human.getStatus(), "enabled") || TextUtils.equals(human.getStatus(), "inactive");
    }

    public String getStatusMessage() {
        return human.getProfile() != null ? human.getProfile().getStatusMessage() : "";
    }

    @Override
    public String getEmail() {
        return human.getProfile() != null ? human.getProfile().getEmail() : "";
    }

    @Override
    public boolean isInactive() {
        return TextUtils.equals(human.getStatus(), "inactive");
    }

    @Override
    public boolean isTeamOwner() {
        return TextUtils.equals(human.getRole(), "owner");
    }

    @Override
    public String getType() {
        return human.getType();
    }

    @Override
    public boolean isBot() {
        return TextUtils.equals(getType(), "bot");
    }

    public String getDivision() {
        return human.getProfile() != null ? human.getProfile().getDepartment() : "";
    }

    public String getPosition() {
        return human.getProfile() != null ? human.getProfile().getPosition() : "";
    }

    public String getPhoneNumber() {
        return human.getProfile() != null ? human.getProfile().getPhoneNumber() : "";
    }

    @Override
    public boolean isStarred() {
        return human.isStarred();
    }

    public boolean isProfileUpdated() {
        return human.getProfile() != null ? human.getProfile().isUpdated() : false;
    }
}

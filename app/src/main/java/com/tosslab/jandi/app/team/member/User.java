package com.tosslab.jandi.app.team.member;

import android.text.TextUtils;

import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.network.models.team.rank.Rank;
import com.tosslab.jandi.app.team.authority.Level;

public class User implements Member {
    private final Human human;
    private final Rank rank;

    public User(Human human) {
        this.human = human;
        rank = null;
    }

    public User(Human human, Rank rank) {
        this.human = human;
        this.rank = rank;
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

    public boolean isRemoved() {
        return TextUtils.equals(human.getStatus(), "removed");
    }

    public boolean isDeleted() {
        return TextUtils.equals(human.getStatus(), "deleted");
    }

    public boolean isDisabled() {
        return TextUtils.equals(human.getStatus(), "disabled");
    }

    @Override
    public boolean isTeamOwner() {
        return rank != null &&
                (rank.getLevel() == Level.Owner.getLevel() ||
                        rank.getLevel() == Level.Admin.getLevel());
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
        return human.getProfile() != null && human.getProfile().isUpdated();
    }

    public Level getLevel() {
        if (rank != null) {
            return Level.valueOf(rank.getLevel());
        } else {
            return Level.Member;
        }
    }
}

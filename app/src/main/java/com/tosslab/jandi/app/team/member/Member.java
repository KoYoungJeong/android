package com.tosslab.jandi.app.team.member;

public interface Member {
    long getId();

    String getName();

    String getPhotoUrl();

    boolean isEnabled();

    String getEmail();

    boolean isInactive();

    boolean isTeamOwner();

    String getType();

    boolean isBot();

    boolean isStarred();
}

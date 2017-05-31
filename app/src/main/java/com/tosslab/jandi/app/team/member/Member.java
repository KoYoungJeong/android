package com.tosslab.jandi.app.team.member;

import com.tosslab.jandi.app.network.models.start.Absence;

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

    Absence getAbsence();
}

package com.tosslab.jandi.app.team.room;

import com.tosslab.jandi.app.network.models.start.Marker;

import java.util.Collection;

public interface Room {

    long getId();

    long getTeamId();

    String getType();

    long getLastLinkId();

    long getReadLinkId();

    int getUnreadCount();

    boolean isEnabled();

    boolean isPublicTopic();

    boolean isChat();

    boolean isJoined();

    boolean isReadOnly();

    Collection<Marker> getMarkers();

    Collection<Long> getMembers();
}

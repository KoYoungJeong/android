package com.tosslab.jandi.app.team.room;

import android.text.TextUtils;

import com.tosslab.jandi.app.network.models.start.Marker;
import com.tosslab.jandi.app.network.models.start.Topic;

import java.util.Collection;

public class TopicRoom implements Room {
    private final Topic topic;

    public TopicRoom(Topic topic) {
        this.topic = topic;
    }

    @Override
    public long getId() {
        return topic.getId();
    }

    @Override
    public long getTeamId() {
        return topic.getTeamId();
    }

    @Override
    public String getType() {
        return topic.getType();
    }

    @Override
    public long getLastLinkId() {
        return topic.getLastLinkId();
    }

    @Override
    public long getReadLinkId() {
        return topic.getReadLinkId();
    }

    @Override
    public int getUnreadCount() {
        return topic.getUnreadCount();
    }

    @Override
    public boolean isStarred() {
        return topic.isStarred();
    }

    @Override
    public boolean isEnabled() {
        return TextUtils.equals(topic.getStatus(), "active");
    }

    public String getName() {
        return topic.getName();
    }

    public boolean hasAnnouncement() {
        return topic.getAnnouncement() != null;
    }

    public boolean isPushSubscribe() {
        return topic.isSubscribe();
    }

    public long getCreatorId() {
        return topic.getCreatorId();
    }

    @Override
    public boolean isPublicTopic() {
        return TextUtils.equals(topic.getType(), "channel");
    }

    @Override
    public boolean isChat() {
        return false;
    }

    @Override
    public boolean isJoined() {
        return topic.isJoined();
    }

    @Override
    public Collection<Marker> getMarkers() {
        return topic.getMarkers();
    }

    public int getMemberCount() {
        return topic.getMembers().size();
    }

    public Collection<Long> getMembers() {
        return topic.getMembers();
    }

    public boolean isDefaultTopic() {
        return topic.isDefault();
    }

    public String getDescription() {
        return topic.getDescription();
    }

    public boolean isAutoJoin() {
        return topic.isAutoJoin();
    }
}

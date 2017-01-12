package com.tosslab.jandi.app.team.room;

import android.text.TextUtils;

import com.tosslab.jandi.app.network.models.start.Announcement;
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

    public boolean isAnnouncementOpened() {
        return topic.getAnnouncement() != null && topic.getAnnouncement().isOpened();
    }

    public Announcement getAnnouncement() {
        return topic.getAnnouncement();
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
    public boolean isReadOnly() {
        return topic.isAnnouncement();
    }

    @Override
    public Collection<Marker> getMarkers() {
        return topic.getMarkers();
    }

    public int getMemberCount() {
        return topic.getMembers().size();
    }

    @Override
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

    public Topic getRaw() {
        return topic;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}

package com.tosslab.jandi.app.events.entities;

/**
 * Created by Steve SeongUg Jung on 15. 4. 9..
 */
public class TopicLeftEvent {
    private final long teamId;
    private final long topicId;
    private final boolean isMe;

    public TopicLeftEvent(long teamId, long topicId, boolean isMe) {
        this.teamId = teamId;
        this.topicId = topicId;
        this.isMe = isMe;
    }

    public long getTopicId() {
        return topicId;
    }

    public long getTeamId() {
        return teamId;
    }

    public boolean isMe() {
        return isMe;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TopicLeftEvent that = (TopicLeftEvent) o;

        if (teamId != that.teamId) return false;
        if (topicId != that.topicId) return false;
        return isMe == that.isMe;

    }

    @Override
    public int hashCode() {
        int result = (int) (teamId ^ (teamId >>> 32));
        result = 31 * result + (int) (topicId ^ (topicId >>> 32));
        result = 31 * result + (isMe ? 1 : 0);
        return result;
    }
}

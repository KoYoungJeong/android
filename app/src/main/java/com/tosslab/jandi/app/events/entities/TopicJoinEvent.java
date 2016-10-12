package com.tosslab.jandi.app.events.entities;

/**
 * Created by Steve SeongUg Jung on 15. 4. 9..
 */
public class TopicJoinEvent {
    private final long teamId;
    private final long topicId;

    public TopicJoinEvent(long teamId, long topicId) {
        this.teamId = teamId;
        this.topicId = topicId;
    }

    public long getTopicId() {
        return topicId;
    }

    public long getTeamId() {
        return teamId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TopicJoinEvent that = (TopicJoinEvent) o;

        if (teamId != that.teamId) return false;
        return topicId == that.topicId;

    }

    @Override
    public int hashCode() {
        int result = (int) (teamId ^ (teamId >>> 32));
        result = 31 * result + (int) (topicId ^ (topicId >>> 32));
        return result;
    }
}

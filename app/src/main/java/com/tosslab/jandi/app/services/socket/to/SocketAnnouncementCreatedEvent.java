package com.tosslab.jandi.app.services.socket.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.network.models.EventHistoryInfo;
import com.tosslab.jandi.app.network.models.start.Announcement;
import com.tosslab.jandi.app.services.socket.annotations.Version;

/**
 * Created by tonyjs on 15. 6. 24..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)

@Version(2)
public class SocketAnnouncementCreatedEvent implements EventHistoryInfo {
    private String event;
    private int version;
    private long ts;
    private long teamId;
    private Data data;
    private String unique;

    @Override
    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    @Override
    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Type getEventType() {
        Type temp = Type.STATUS_UPDATED;
        Type[] types = Type.values();
        for (Type type : types) {
            if (type.getName().equals(event)) {
                temp = type;
                break;
            }
        }
        return temp;
    }

    @Override
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "SocketAnnouncementDeletedEvent{" +
                "event='" + event + '\'' +
                ", version=" + version +
                ", data=" + data +
                '}';
    }

    @Override
    public long getTeamId() {
        return teamId;
    }

    @Override
    public String getUnique() {
        return unique;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public void setUnique(String unique) {
        this.unique = unique;
    }

    public enum Type {
        CREATED("announcement_created"),
        STATUS_UPDATED("announcement_status_updated"),
        DELETED("announcement_deleted");

        String name;

        Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Data {
        private long topicId;
        private Announcement announcement;

        public long getTopicId() {
            return topicId;
        }

        public void setTopicId(long topicId) {
            this.topicId = topicId;
        }

        public Announcement getAnnouncement() {
            return announcement;
        }

        public void setAnnouncement(Announcement announcement) {
            this.announcement = announcement;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Data data = (Data) o;

            if (topicId != data.topicId) return false;
            return announcement != null ? announcement.equals(data.announcement) : data.announcement == null;

        }

        @Override
        public int hashCode() {
            int result = (int) (topicId ^ (topicId >>> 32));
            result = 31 * result + (announcement != null ? announcement.hashCode() : 0);
            return result;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SocketAnnouncementCreatedEvent that = (SocketAnnouncementCreatedEvent) o;

        if (version != that.version) return false;
        if (ts != that.ts) return false;
        if (teamId != that.teamId) return false;
        if (event != null ? !event.equals(that.event) : that.event != null) return false;
        return data != null ? data.equals(that.data) : that.data == null;

    }

    @Override
    public int hashCode() {
        int result = event != null ? event.hashCode() : 0;
        result = 31 * result + version;
        result = 31 * result + (int) (ts ^ (ts >>> 32));
        result = 31 * result + (int) (teamId ^ (teamId >>> 32));
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }
}

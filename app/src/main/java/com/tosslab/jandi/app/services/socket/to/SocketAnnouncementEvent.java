package com.tosslab.jandi.app.services.socket.to;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Created by tonyjs on 15. 6. 24..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class SocketAnnouncementEvent {
    public enum Type {
        CREATED("announcement_created"),
        UPDATED("announcement_status_updated"),
        DELETED("announcement_deleted");

        String name;
        Type(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private String event;
    private int version;
    private Data data;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Type getEventType() {
        Type temp = Type.UPDATED;
        Type[] types = Type.values();
        for (Type type : types) {
            if (type.getName().equals(event)) {
                temp = type;
                break;
            }
        }
        return temp;
    }

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
        return "SocketAnnouncementEvent{" +
                "event='" + event + '\'' +
                ", version=" + version +
                ", data=" + data +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Data {
        private int topicId;
        private boolean status = false;

        public int getTopicId() {
            return topicId;
        }

        public void setTopicId(int topicId) {
            this.topicId = topicId;
        }

        public boolean isStatus() {
            return status;
        }

        public void setStatus(boolean status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "status=" + status +
                    ", topicId=" + topicId +
                    '}';
        }
    }
}

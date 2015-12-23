package com.tosslab.jandi.app.services.socket.to;

import com.tosslab.jandi.app.services.socket.annotations.Version;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Created by tonyjs on 15. 7. 30..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Version(1)
public class SocketTopicPushEvent {

    private String event;
    private int version;
    private Data data;

    public String getEvent() {
        return event;
    }

    public int getVersion() {
        return version;
    }

    public Data getData() {
        return data;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setData(Data data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "SocketTopicPushEvent{" +
                "data=" + data +
                ", version=" + version +
                ", event='" + event + '\'' +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Data {
        private int roomId;
        private boolean subscribe = false;

        public int getRoomId() {
            return roomId;
        }

        public void setRoomId(int roomId) {
            this.roomId = roomId;
        }

        public boolean isSubscribe() {
            return subscribe;
        }

        public void setSubscribe(boolean subscribe) {
            this.subscribe = subscribe;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "roomId=" + roomId +
                    ", subscribe=" + subscribe +
                    '}';
        }
    }
}

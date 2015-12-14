package com.tosslab.jandi.app.services.socket.to;

import com.tosslab.jandi.app.network.models.ResEventHistory;
import com.tosslab.jandi.app.services.socket.annotations.Version;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Created by Steve SeongUg Jung on 15. 4. 7..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonDeserialize(using = JsonDeserializer.None.class)
@Version(1)
public class SocketFileEvent extends ResEventHistory.EventHistoryInfo {
    private int teamId;
    private long ts;
    private int version;
    private EventFileInfo file;
    private String event;

    public EventFileInfo getFile() {
        return file;
    }

    public void setFile(EventFileInfo file) {
        this.file = file;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "SocketFileEvent{" +
                "teamId=" + teamId +
                ", ts=" + ts +
                ", version=" + version +
                ", file=" + file +
                ", event='" + event + '\'' +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class EventFileInfo {
        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "EventFileInfo{" +
                    "id=" + id +
                    '}';
        }
    }
}

package com.tosslab.jandi.app.services.socket.to;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Created by Steve SeongUg Jung on 15. 4. 7..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class SocketFileEvent {
    private int teamId;
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
    }
}

package com.tosslab.jandi.app.services.socket.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.network.models.EventHistoryInfo;
import com.tosslab.jandi.app.services.socket.annotations.Version;

/**
 * Created by tee on 16. 2. 5..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Version(1)
public class SocketFileShareEvent implements EventHistoryInfo {
    private String event;
    private int version;
    private long teamId;
    private long writer;
    private FileObject file;
    private long ts;

    @Override
    public long getTs() {
        return ts;
    }

    @Override
    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    @Override
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public long getWriter() {
        return writer;
    }

    public void setWriter(long writer) {
        this.writer = writer;
    }

    public FileObject getFile() {
        return file;
    }

    public void setFile(FileObject file) {
        this.file = file;
    }

    @Override
    public String toString() {
        return "SocketFileShareEvent{" +
                "event='" + event + '\'' +
                ", version=" + version +
                ", teamId=" + teamId +
                ", writer=" + writer +
                ", file=" + file +
                '}';
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class FileObject {
        private long id;

        public long getId() {
            return id;
        }

        public void setWriter(long id) {
            this.id = id;
        }
    }
}

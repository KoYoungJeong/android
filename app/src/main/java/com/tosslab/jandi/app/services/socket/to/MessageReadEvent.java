package com.tosslab.jandi.app.services.socket.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.services.socket.annotations.Version;
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Version(1)
public class MessageReadEvent {
    private boolean fromSelf = false;

    private long teamId;

    private int version;

    private long ts;

    private int readCount;

    public MessageReadEvent(long teamId) {
        this(teamId, 0);
    }

    public MessageReadEvent(long teamId, int readCount) {
        this.teamId = teamId;
        this.readCount = readCount;
    }

    private MessageReadEvent(long teamId, int readCount, boolean fromSelf) {
        this(teamId, readCount);
        this.fromSelf = fromSelf;
    }

    public static MessageReadEvent fromSelf(long teamId, int readCount) {
        return new MessageReadEvent(teamId, readCount, true);
    }

    public boolean fromSelf() {
        return fromSelf;
    }

    public long getTeamId() {
        return teamId;
    }

    public int getReadCount() {
        return readCount;
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
        return "MessageReadEvent{" +
                "version=" + version +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageReadEvent that = (MessageReadEvent) o;

        if (version != that.version) return false;
        return ts == that.ts;

    }

    @Override
    public int hashCode() {
        int result = version;
        result = 31 * result + (int) (ts ^ (ts >>> 32));
        return result;
    }
}

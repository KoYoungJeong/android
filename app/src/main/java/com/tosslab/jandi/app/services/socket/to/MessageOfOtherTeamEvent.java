package com.tosslab.jandi.app.services.socket.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.services.socket.annotations.Version;
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Version(1)
public class MessageOfOtherTeamEvent {
    private int version;

    private long ts;

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
        return "MessageOfOtherTeamEvent{" +
                "version=" + version +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageOfOtherTeamEvent that = (MessageOfOtherTeamEvent) o;

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

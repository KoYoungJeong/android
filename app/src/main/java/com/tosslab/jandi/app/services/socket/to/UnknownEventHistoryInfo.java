package com.tosslab.jandi.app.services.socket.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.network.models.EventHistoryInfo;
import com.tosslab.jandi.app.services.socket.annotations.Version;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonDeserialize(using = JsonDeserializer.None.class)
@Version(1)
public class UnknownEventHistoryInfo implements EventHistoryInfo {
    private String event;
    private int version;
    private long ts;
    private long teamId;
    @Override
    public long getTs() {
        return ts;
    }

    @Override
    public String getEvent() {
        return event;
    }

    @Override
    public int getVersion() {
        return version;
    }
}

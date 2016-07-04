package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResUpdateMessages {
    public long lastLinkId;
    public int messageCount;
    public List<ResMessages.Link> messages;

    @Override
    public String toString() {
        return "ResUpdateMessages{" +
                "lastLinkId=" + lastLinkId +
                ", messageCount=" + messageCount +
                ", messages=" + messages +
                '}';
    }

}

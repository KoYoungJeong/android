package com.tosslab.jandi.app.network.models;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 19..
 */
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

package com.tosslab.jandi.app.network.models;

import com.tosslab.jandi.app.network.jackson.deserialize.message.SocketHistoryDeserializer;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;

/**
 * Created by tee on 15. 11. 17..
 */
public class ResEventHistory {

    public int size;
    public boolean hasMore;
    public long lastTs;
    public List<EventHistoryInfo> records;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonDeserialize(using = SocketHistoryDeserializer.class)
    public static class EventHistoryInfo {
        public long _id;
    }

}

package com.tosslab.jandi.app.network.models;

import com.tosslab.jandi.app.network.models.commonobject.StarMentionedMessageObject;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;

/**
 * Created by tee on 15. 7. 29..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResStarMentioned {

    private boolean hasMore;

    private List<StarMentionedMessageObject> records;

    public boolean hasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public List<StarMentionedMessageObject> getRecords() {
        return records;
    }

    public void setRecords(List<StarMentionedMessageObject> records) {
        this.records = records;
    }

    @Override
    public String toString() {
        return "ResMentioned{" +
                "hasMore=" + hasMore +
                ", records=" + records.toString() +
                '}';
    }
}

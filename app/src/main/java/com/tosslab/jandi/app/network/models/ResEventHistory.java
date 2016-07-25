package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResEventHistory {

    private int total;
    private int size;
    private boolean hasMore;
    private long lastTs;
    private List<EventHistoryInfo> records;



    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public long getLastTs() {
        return lastTs;
    }

    public void setLastTs(long lastTs) {
        this.lastTs = lastTs;
    }

    public List<EventHistoryInfo> getRecords() {
        return records;
    }

    public void setRecords(List<EventHistoryInfo> records) {
        this.records = records;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}

package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.network.models.poll.Poll;

import java.util.List;

/**
 * Created by tonyjs on 16. 6. 28..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResPollList {
    private List<Poll> onGoing;
    private List<Poll> finished;
    private boolean hasMore;
    private int votableCount;

    @JsonIgnore
    private List<Poll> pollList;

    public List<Poll> getOnGoing() {
        return onGoing;
    }

    public void setOnGoing(List<Poll> onGoing) {
        this.onGoing = onGoing;
    }

    public List<Poll> getFinished() {
        return finished;
    }

    public void setFinished(List<Poll> finished) {
        this.finished = finished;
    }

    public boolean hasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public List<Poll> getPollList() {
        return pollList;
    }

    public void setPollList(List<Poll> pollList) {
        this.pollList = pollList;
    }

    public int getVotableCount() {
        return votableCount;
    }

    public ResPollList setVotableCount(int votableCount) {
        this.votableCount = votableCount;
        return this;
    }
}

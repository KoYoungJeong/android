package com.tosslab.jandi.app.network.models.start;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Poll {
    private int votableCount;

    public int getVotableCount() {
        return votableCount;
    }

    public void setVotableCount(int votableCount) {
        this.votableCount = votableCount;
    }

    @Override
    public String toString() {
        return "Poll{" +
                "votableCount=" + votableCount +
                '}';
    }

}

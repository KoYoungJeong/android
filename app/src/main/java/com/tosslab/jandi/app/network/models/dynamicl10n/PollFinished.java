package com.tosslab.jandi.app.network.models.dynamicl10n;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class PollFinished extends FormatParam {
    private List<ElectedItem> electedItems;
    private int votedCount;

    public List<ElectedItem> getElectedItems() {
        return electedItems;
    }

    public void setElectedItems(List<ElectedItem> electedItems) {
        this.electedItems = electedItems;
    }

    public int getVotedCount() {
        return votedCount;
    }

    public void setVotedCount(int votedCount) {
        this.votedCount = votedCount;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class ElectedItem {
        private int votedCount;
        private String name;
        private int seq;

        public int getVotedCount() {
            return votedCount;
        }

        public void setVotedCount(int votedCount) {
            this.votedCount = votedCount;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getSeq() {
            return seq;
        }

        public void setSeq(int seq) {
            this.seq = seq;
        }

        @Override
        public String toString() {
            return "ElectedItem{" +
                    "votedCount=" + votedCount +
                    ", name='" + name + '\'' +
                    ", seq=" + seq +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "PollFinished{" +
                "electedItems=" + electedItems +
                ", votedCount=" + votedCount +
                '}';
    }
}

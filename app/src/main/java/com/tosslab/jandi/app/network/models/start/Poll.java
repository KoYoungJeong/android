package com.tosslab.jandi.app.network.models.start;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Poll extends RealmObject {
    @JsonIgnore
    @PrimaryKey
    private long id;
    private int votableCount;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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

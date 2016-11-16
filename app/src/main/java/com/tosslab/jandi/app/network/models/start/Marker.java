package com.tosslab.jandi.app.network.models.start;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Marker extends RealmObject {

    @JsonIgnore
    @PrimaryKey
    private String id;
    @JsonIgnore
    private long roomId;

    private long memberId;
    private long readLinkId;

    public long getMemberId() {
        return memberId;
    }

    public void setMemberId(long memberId) {
        this.memberId = memberId;
    }

    public long getReadLinkId() {
        return readLinkId;
    }

    public void setReadLinkId(long readLinkId) {
        this.readLinkId = readLinkId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }
}

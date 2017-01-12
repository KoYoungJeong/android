package com.tosslab.jandi.app.network.models.start;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gson.annotations.Expose;
import com.vimeo.stag.GsonAdapterKey;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Folder {
    @GsonAdapterKey
    long id;
    @GsonAdapterKey
    String name;
    @GsonAdapterKey
    int seq;
    @GsonAdapterKey
    List<Long> rooms;
    @JsonIgnore
    @Expose(deserialize = false)
    private boolean isOpened;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public List<Long> getRooms() {
        return rooms;
    }

    public void setRooms(List<Long> rooms) {
        this.rooms = rooms;
    }

    public boolean isOpened() {
        return isOpened;
    }

    public void setOpened(boolean opened) {
        isOpened = opened;
    }

}

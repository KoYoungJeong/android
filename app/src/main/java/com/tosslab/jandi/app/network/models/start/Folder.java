package com.tosslab.jandi.app.network.models.start;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.tosslab.jandi.app.local.orm.persister.CollectionLongConverter;

import java.util.Collection;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@DatabaseTable(tableName = "initial_info_folder")
public class Folder {
    @DatabaseField(id = true)
    private long id;
    @DatabaseField
    private String name;
    @DatabaseField
    private int seq;
    @DatabaseField(persisterClass = CollectionLongConverter.class)
    private Collection<Long> rooms;
    @JsonIgnore
    @DatabaseField
    private boolean isOpened;
    @JsonIgnore
    @DatabaseField(foreign = true)
    private InitialInfo initialInfo;

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

    public Collection<Long> getRooms() {
        return rooms;
    }

    public void setRooms(Collection<Long> rooms) {
        this.rooms = rooms;
    }

    public InitialInfo getInitialInfo() {
        return initialInfo;
    }

    public void setInitialInfo(InitialInfo initialInfo) {
        this.initialInfo = initialInfo;
    }

    public boolean isOpened() {
        return isOpened;
    }

    public void setOpened(boolean opened) {
        isOpened = opened;
    }
}

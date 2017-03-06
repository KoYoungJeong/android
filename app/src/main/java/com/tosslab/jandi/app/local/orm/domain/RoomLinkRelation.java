package com.tosslab.jandi.app.local.orm.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "room_link_relation")
public class RoomLinkRelation {
    @DatabaseField(id = true, useGetSet = true)
    private String id;
    @DatabaseField
    private long roomId;
    @DatabaseField
    private long linkId;
    @DatabaseField
    private boolean dirty = true;

    public String getId() {
        return roomId + "_" + linkId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
        if (linkId > 0) {
            setId(roomId + "_" + linkId);
        }
    }

    public long getLinkId() {
        return linkId;
    }

    public void setLinkId(long linkId) {
        this.linkId = linkId;
        if (roomId > 0) {
            setId(roomId + "_" + linkId);
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
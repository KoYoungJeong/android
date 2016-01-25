package com.tosslab.jandi.app.network.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.Collection;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 4. 15..
 */
@DatabaseTable(tableName = "room_info")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResRoomInfo {

    @DatabaseField(id = true, columnName = "roomId")
    private long id;
    @DatabaseField
    private long teamId;
    @DatabaseField
    private String type;
    @DatabaseField
    private String name;
    @DatabaseField
    @JsonProperty("default")
    private boolean isDefault;
    // Non-use
    private List<Long> members;

    @DatabaseField
    private long creatorId;
    @DatabaseField
    private String status;
    @ForeignCollectionField
    private Collection<MarkerInfo> markers;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public List<Long> getMembers() {
        return members;
    }

    public void setMembers(List<Long> members) {
        this.members = members;
    }

    public long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(long creatorId) {
        this.creatorId = creatorId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Collection<MarkerInfo> getMarkers() {
        return markers;
    }

    public void setMarkers(Collection<MarkerInfo> markers) {
        this.markers = markers;
    }

    @DatabaseTable(tableName = "room_info_marker")
    public static class MarkerInfo {
        @DatabaseField(columnName = "roomId", foreign = true, foreignAutoRefresh = true)
        private ResRoomInfo room;

        @DatabaseField(generatedId = true)
        private long _id;

        @DatabaseField
        private long memberId;
        @DatabaseField
        private long lastLinkId;

        public long getLastLinkId() {
            return lastLinkId;
        }

        public void setLastLinkId(long lastLinkId) {
            this.lastLinkId = lastLinkId;
        }

        public long getMemberId() {
            return memberId;
        }

        public void setMemberId(long memberId) {
            this.memberId = memberId;
        }

        public ResRoomInfo getRoom() {
            return room;
        }

        public void setRoom(ResRoomInfo room) {
            this.room = room;
        }
    }
}

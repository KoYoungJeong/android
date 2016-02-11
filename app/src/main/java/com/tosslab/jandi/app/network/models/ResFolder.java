package com.tosslab.jandi.app.network.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.Date;

/**
 * Created by tee on 15. 8. 25..
 */

@DatabaseTable(tableName = "topic_folders")
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResFolder {

    @DatabaseField(id = true)
    public long id;
    @DatabaseField
    public String name;
    @DatabaseField
    public int seq;
    @DatabaseField
    public long memberId;
    @DatabaseField
    public long teamId;
    @DatabaseField
    public Date createdAt;
    @DatabaseField
    public Date updatedAt;

    @Override
    public String toString() {
        return "ResFolder{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", seq=" + seq +
                ", teamId=" + teamId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResFolder resFolder = (ResFolder) o;

        if (id != resFolder.id) return false;
        if (memberId != resFolder.memberId) return false;
        return teamId == resFolder.teamId;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (memberId ^ (memberId >>> 32));
        result = 31 * result + (int) (teamId ^ (teamId >>> 32));
        return result;
    }
}

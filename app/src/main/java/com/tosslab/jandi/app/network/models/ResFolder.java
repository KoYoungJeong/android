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
    public int id;
    @DatabaseField
    public String name;
    @DatabaseField
    public int seq;
    @DatabaseField
    public int memberId;
    @DatabaseField
    public int teamId;
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
        if (seq != resFolder.seq) return false;
        if (memberId != resFolder.memberId) return false;
        if (teamId != resFolder.teamId) return false;
        return name.equals(resFolder.name);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + name.hashCode();
        result = 31 * result + seq;
        result = 31 * result + memberId;
        result = 31 * result + teamId;
        return result;
    }
}

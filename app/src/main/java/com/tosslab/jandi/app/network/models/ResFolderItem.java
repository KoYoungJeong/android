package com.tosslab.jandi.app.network.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Created by tee on 15. 8. 25..
 */


@DatabaseTable(tableName = "topic_folder_items")
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResFolderItem {

    @JsonIgnore
    @DatabaseField(generatedId = true)
    public long _id;

    @JsonIgnore
    @DatabaseField
    public long teamId;

    @DatabaseField
    public long folderId;

    @DatabaseField
    public long roomId;

    @Override
    public String toString() {
        return "ResFolderItem{" +
                "_id=" + _id +
                ", teamId=" + teamId +
                ", folderId=" + folderId +
                ", roomId=" + roomId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResFolderItem that = (ResFolderItem) o;

        if (teamId != that.teamId) return false;
        return folderId == that.folderId;

    }

    @Override
    public int hashCode() {
        int result = (int) (teamId ^ (teamId >>> 32));
        result = 31 * result + (int) (folderId ^ (folderId >>> 32));
        return result;
    }
}
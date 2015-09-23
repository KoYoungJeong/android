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
    public int _id;
    @DatabaseField
    public int folderId;
    @DatabaseField
    public int roomId;

    @Override
    public String toString() {
        return "ResFolderItem{" +
                "folderId=" + folderId +
                ", roomId=" + roomId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResFolderItem that = (ResFolderItem) o;

        if (folderId != that.folderId) return false;
        return roomId == that.roomId;
    }

    @Override
    public int hashCode() {
        int result = folderId;
        result = 31 * result + roomId;
        return result;
    }
}
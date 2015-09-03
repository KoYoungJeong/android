package com.tosslab.jandi.app.network.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.codehaus.jackson.annotate.JsonIgnore;
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

    @JsonIgnore
    @DatabaseField(generatedId = true)
    public int _id;
    @DatabaseField
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
                "id=" + _id +
                ", name='" + name + '\'' +
                ", seq=" + seq +
                ", teamId=" + teamId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

}

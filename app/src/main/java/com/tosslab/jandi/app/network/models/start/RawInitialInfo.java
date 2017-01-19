package com.tosslab.jandi.app.network.models.start;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "raw_initial_info")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class RawInitialInfo {
    @DatabaseField(id = true)
    private long teamId;
    @DatabaseField
    private String rawValue;

    public RawInitialInfo() { }

    public RawInitialInfo(long teamId, String rawValue) {
        this.teamId = teamId;
        this.rawValue = rawValue;
    }

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public String getRawValue() {
        return rawValue;
    }

    public void setRawValue(String rawValue) {
        this.rawValue = rawValue;
    }
}

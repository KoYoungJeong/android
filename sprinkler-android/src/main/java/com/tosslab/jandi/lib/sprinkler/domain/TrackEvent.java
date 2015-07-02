package com.tosslab.jandi.lib.sprinkler.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Collection;
import java.util.Date;

@DatabaseTable(tableName = "trackEvent")
public class TrackEvent {

    @DatabaseField(generatedId = true, readOnly = true)
    private long id;

    @DatabaseField
    private String event;
    @DatabaseField(foreign = true, columnName = "trackId_id")
    private TrackId trackId;
    @DatabaseField
    private String platform = "android";
    @ForeignCollectionField
    private Collection<EventProperty> properties;

    @DatabaseField
    private Date time;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public TrackId getTrackId() {
        return trackId;
    }

    public void setTrackId(TrackId trackId) {
        this.trackId = trackId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Collection<EventProperty> getProperties() {
        return properties;
    }

    public void setProperties(Collection<EventProperty> properties) {
        this.properties = properties;
    }
}

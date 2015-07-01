package com.tosslab.jandi.lib.sprinkler.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;
import java.util.List;
import java.util.Map;

@DatabaseTable(tableName = "tracker_event")
public class TrackEvent {

    @DatabaseField(generatedId = true, canBeNull = false)
    private long _id;

    @DatabaseField()
    private String event;
    private TrackerId trackerId;
    private String platform;
    private List<EventProperty> properties;

    private Date time;

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public TrackerId getTrackerId() {
        return trackerId;
    }

    public void setTrackerId(TrackerId trackerId) {
        this.trackerId = trackerId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}

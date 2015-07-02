package com.tosslab.jandi.lib.sprinkler.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "eventProperty")
public class EventProperty {
    @DatabaseField(generatedId = true, readOnly = true)
    private long id;

    @DatabaseField
    private String propertyName;
    @DatabaseField
    private String propertyValue;

    @DatabaseField(foreign = true)
    private TrackEvent trackEvent;

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public TrackEvent getTrackEvent() {
        return trackEvent;
    }

    public void setTrackEvent(TrackEvent trackEvent) {
        this.trackEvent = trackEvent;
    }


    public static class Builder {
        private String propertyName;
        private String propertyValue;

        public Builder propertyName(String propertyName) {
            this.propertyName = propertyName;
            return this;
        }

        public Builder propertyValue(String propertyValue) {
            this.propertyValue = propertyValue;
            return this;
        }

        public EventProperty build() {
            EventProperty eventProperty = new EventProperty();
            eventProperty.setPropertyName(propertyName);
            eventProperty.setPropertyValue(propertyValue);

            return eventProperty;
        }
    }
}

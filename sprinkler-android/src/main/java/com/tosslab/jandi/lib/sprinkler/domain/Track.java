package com.tosslab.jandi.lib.sprinkler.domain;

import io.realm.RealmObject;

/**
 * Created by tonyjs on 15. 7. 8..
 */
public class Track extends RealmObject {
    private String event;
    private String propertyKey;
    private String propertyValue;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public void setPropertyKey(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }
}

package com.tosslab.jandi.lib.sprinkler.io.domain.event;

/**
 * Created by tonyjs on 15. 7. 22..
 * <p>
 * EventCategory(Event 의 종류 파악),
 * EventName,
 * AvailablePropertyKeys(Event 에 적재 가능한 Property 파악)
 */
public class Event {

    private String eventCategory;
    private String eventName;
    private String[] availablePropertyKeys;

    Event(String category, String name, String[] propertyKeys) {
        eventCategory = category;
        eventName = name;
        availablePropertyKeys = propertyKeys;
    }

    public static Event create(String category, String name, String[] propertyKeys) {
        return new Event(category, name, propertyKeys);
    }

    public String getName() {
        return eventName;
    }

    public String getEventCategory() {
        return eventCategory;
    }

    public String getEventName() {
        return eventName;
    }

    public String[] getAvailablePropertyKeys() {
        return availablePropertyKeys;
    }
}

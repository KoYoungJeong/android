package com.tosslab.jandi.app.network.jackson.deserialize;

import android.text.TextUtils;

import com.tosslab.jandi.app.network.models.ResMessages;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class EventInfoDeserialize extends JsonDeserializer<ResMessages.EventInfo> {


    @Override
    public ResMessages.EventInfo deserialize(JsonParser jp, DeserializationContext ctxt) throws
            IOException {

        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        JsonNode root = mapper.readTree(jp);

        String eventTypeValue = getEventTypeValue(root);

        EventType eventType = getEventType(eventTypeValue);

        return createEventInfo(mapper, root, eventType);


    }

    public ResMessages.EventInfo createEventInfo(ObjectMapper mapper, JsonNode root, EventType eventType) throws IOException {
        switch (eventType) {

            case AnnounceCreate:
                return mapper.readValue(root, ResMessages.AnnouncementCreateEvent.class);
            case AnnounceDelete:
                return mapper.readValue(root, ResMessages.AnnouncementDeleteEvent.class);
            case AnnounceStatusUpdate:
                return mapper.readValue(root, ResMessages.AnnouncementUpdateEvent.class);
            case Create:
                return mapper.readValue(root, ResMessages.CreateEvent.class);
            case Invite:
                return mapper.readValue(root, ResMessages.InviteEvent.class);
            case Leave:
                return mapper.readValue(root, ResMessages.LeaveEvent.class);
            case Join:
                return mapper.readValue(root, ResMessages.JoinEvent.class);
            default:
                return new ResMessages.EventInfo();
        }
    }

    public String getEventTypeValue(JsonNode root) {
        Iterator<Map.Entry<String, JsonNode>> fields = root.getFields();

        String key;
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> next = fields.next();

            key = next.getKey();
            if (TextUtils.equals(key, "eventType")) {
                return next.getValue().getTextValue();
            }
        }
        return "";
    }

    public EventType getEventType(String eventTypeValue) {

        if (TextUtils.isEmpty(eventTypeValue)) {
            return EventType.Unknown;
        }

        EventType eventType = EventType.Unknown;
        try {
            for (EventType type : EventType.values()) {
                if (TextUtils.equals(type.getRawType(), eventTypeValue)) {
                    eventType = type;
                    break;
                }
            }
        } catch (IllegalArgumentException e) {
        }
        return eventType;
    }

    private enum EventType {
        AnnounceCreate("announcement_created"),
        AnnounceDelete("announcement_deleted"),
        AnnounceStatusUpdate("announcement_status_updated"),
        Create("create"),
        Invite("invite"),
        Leave("leave"),
        Join("join"),
        Unknown("");

        private final String rawType;

        EventType(String rawType) {

            this.rawType = rawType;
        }

        public String getRawType() {
            return rawType;
        }

    }
}
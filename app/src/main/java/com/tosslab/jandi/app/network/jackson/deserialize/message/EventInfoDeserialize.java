package com.tosslab.jandi.app.network.jackson.deserialize.message;

import android.text.TextUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tosslab.jandi.app.network.models.ResMessages;

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
                return mapper.treeToValue(root, ResMessages.AnnouncementCreateEvent.class);
            case AnnounceDelete:
                return mapper.treeToValue(root, ResMessages.AnnouncementDeleteEvent.class);
            case AnnounceStatusUpdate:
                return mapper.treeToValue(root, ResMessages.AnnouncementUpdateEvent.class);
            case Create:
                return mapper.treeToValue(root, ResMessages.CreateEvent.class);
            case Invite:
                return mapper.treeToValue(root, ResMessages.InviteEvent.class);
            case Leave:
                return mapper.treeToValue(root, ResMessages.LeaveEvent.class);
            case Join:
                return mapper.treeToValue(root, ResMessages.JoinEvent.class);
            default:
                return new ResMessages.EventInfo();
        }
    }

    public String getEventTypeValue(JsonNode root) {
        Iterator<Map.Entry<String, JsonNode>> fields = root.fields();

        String key;
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> next = fields.next();

            key = next.getKey();
            if (TextUtils.equals(key, "eventType")) {
                return next.getValue().textValue();
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
package com.tosslab.jandi.app.network.jackson.deserialize.message;

import android.text.TextUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tosslab.jandi.app.network.models.ResEventHistory;
import com.tosslab.jandi.app.services.socket.to.SocketFileUnsharedEvent;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class SocketHistoryDeserializer extends JsonDeserializer<ResEventHistory.EventHistoryInfo> {

    @Override
    public ResEventHistory.EventHistoryInfo deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {

        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        JsonNode root = mapper.readTree(jp);

        String eventTypeValue = getEventTypeValue(root);
        EventType eventType = getEventType(eventTypeValue);

        return createEventInfo(mapper, root, eventType);
    }

    public String getEventTypeValue(JsonNode root) {
        Iterator<Map.Entry<String, JsonNode>> fields = root.fields();

        String key;
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> next = fields.next();

            key = next.getKey();
            if (TextUtils.equals(key, "event")) {
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


    public ResEventHistory.EventHistoryInfo createEventInfo(ObjectMapper mapper, JsonNode root, EventType eventType) throws IOException {
        LogUtil.e("eventType", eventType.getRawType());
        switch (eventType) {
            case FileUnshared:
                return mapper.treeToValue(root, SocketFileUnsharedEvent.class);
            default:
                return new ResEventHistory.EventHistoryInfo();
        }
    }

    private enum EventType {
        FileUnshared("file_unshared"),
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
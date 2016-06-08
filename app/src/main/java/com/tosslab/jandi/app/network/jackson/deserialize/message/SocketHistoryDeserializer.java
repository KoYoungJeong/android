package com.tosslab.jandi.app.network.jackson.deserialize.message;

import android.text.TextUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tosslab.jandi.app.network.models.EventHistoryInfo;
import com.tosslab.jandi.app.services.socket.to.SocketFileCommentDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileUnsharedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageEvent;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import rx.Observable;

public class SocketHistoryDeserializer extends JsonDeserializer<EventHistoryInfo> {

    @Override
    public EventHistoryInfo deserialize(JsonParser jp, DeserializationContext ctxt)
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

        return Observable.from(EventType.values())
                .takeFirst(type -> TextUtils.equals(type.getRawType(), eventTypeValue))
                .toBlocking()
                .firstOrDefault(EventType.Unknown);
    }


    public EventHistoryInfo createEventInfo(ObjectMapper mapper, JsonNode root, EventType eventType) throws IOException {
        LogUtil.e("eventType", eventType.getRawType());
        switch (eventType) {
            case FileUnshared:
                return mapper.treeToValue(root, SocketFileUnsharedEvent.class);
            case MessageCreated:
                return mapper.treeToValue(root, SocketMessageCreatedEvent.class);
            case CommentDeleted:
                return mapper.treeToValue(root, SocketFileCommentDeletedEvent.class);
            case FileDeleted:
                return mapper.treeToValue(root, SocketFileEvent.class);
            case MessageDeleted:
                return mapper.treeToValue(root, SocketMessageEvent.class);
            case Unknown:
            default:
                return new EventHistoryInfo();
        }
    }

    public enum EventType {
        FileUnshared("file_unshared"),
        MessageCreated("message_created"),
        CommentDeleted("file_comment_deleted"),
        FileDeleted("file_deleted"),
        MessageDeleted("message"),
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
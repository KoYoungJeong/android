package com.tosslab.jandi.app.network.jackson.deserialize.start;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.tosslab.jandi.app.network.models.start.Chat;
import com.tosslab.jandi.app.network.models.start.Marker;

import java.util.ArrayList;
import java.util.List;


/**
 * Do use for JacksonParser
 */
@Deprecated
public class ChatConverter implements Converter<Chat, Chat> {
    @Override
    public Chat convert(Chat chat) {
        if (chat != null) {
            List<Marker> markers = chat.getMarkers();

            long lastLinkId = chat.getLastLinkId() > 0 ? chat.getLastLinkId() : 0;
            if (markers == null || markers.isEmpty()) {
                // marker 가 없으면 임의로 지정함
                List<Marker> markers1 = new ArrayList<>();
                for (Long id : chat.getMembers()) {
                    Marker marker = new Marker();
                    marker.setMemberId(id);
                    marker.setReadLinkId(lastLinkId);
                    markers1.add(marker);
                }
                chat.setMarkers(markers1);
            }

            if (!chat.isOpened()) {
                chat.setReadLinkId(lastLinkId);
            }
        }

        return chat;
    }

    @Override
    public JavaType getInputType(TypeFactory typeFactory) {
        return typeFactory.constructType(Chat.class);
    }

    @Override
    public JavaType getOutputType(TypeFactory typeFactory) {
        return typeFactory.constructType(Chat.class);
    }
}

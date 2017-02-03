package com.tosslab.jandi.app.network.jackson.deserialize.start;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.tosslab.jandi.app.network.models.start.Marker;
import com.tosslab.jandi.app.network.models.start.Topic;

import java.util.ArrayList;
import java.util.List;


/**
 * Do use for JacksonParser
 */
@Deprecated
public class TopicConverter implements Converter<Topic, Topic> {
    @Override
    public Topic convert(Topic topic) {
        if (topic != null) {
            List<Marker> markers = topic.getMarkers();

            if (markers == null || markers.isEmpty()) {
                // marker 가 없으면 임의로 지정함
                List<Marker> markers1 = new ArrayList<>();
                for (Long id : topic.getMembers()) {
                    Marker marker = new Marker();
                    marker.setMemberId(id);
                    marker.setReadLinkId(topic.getLastLinkId() > 0 ? topic.getLastLinkId() : -1);
                    markers1.add(marker);
                }
                topic.setMarkers(markers1);
            }
        }

        return topic;
    }

    @Override
    public JavaType getInputType(TypeFactory typeFactory) {
        return typeFactory.constructType(Topic.class);
    }

    @Override
    public JavaType getOutputType(TypeFactory typeFactory) {
        return typeFactory.constructType(Topic.class);
    }
}

package com.tosslab.jandi.app.services.socket.to;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 4. 7..
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonDeserialize(using = JsonDeserializer.None.class)
public class SocketFileUnsharedEvent extends SocketFileEvent {

    public int writer;
    public Room_ room;

    @Override
    public String toString() {
        return super.toString() +
                "SocketFileUnsharedEvent{" +
                "writer=" + writer +
                ", room=" + room +
                '}';
    }

    public static class Room_ {
        public int id;
        public String type;
        public List<Integer> members;
    }
}

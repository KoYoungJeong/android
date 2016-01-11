package com.tosslab.jandi.app.services.socket.to;

import com.tosslab.jandi.app.services.socket.annotations.Version;

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
@Version(1)
public class SocketFileUnsharedEvent extends SocketFileEvent {
    public int writer;
    public Room_ room;

    private long ts;

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }


    @Override
    public String toString() {
        return "SocketFileUnsharedEvent{" +
                "writer=" + writer +
                ", room=" + room +
                "} " + super.toString();
    }

    public static class Room_ {
        public int id;
        public String type;
        public List<Integer> members;
    }
}

package com.tosslab.jandi.lib.sprinkler.io.model;

import java.util.Map;

/**
 * Created by tonyjs on 15. 7. 22..
 * <p>
 * Flush 를 위한 Track
 */
public class Track {
    // Gson Serialize 불필요.
    private transient int index;
    private String ev;
    private Map<String, Object> id;
    private String pl;
    private Map<String, Object> pr;
    private long time;

    public Track(int index, String ev, Map<String, Object> id, String pl, Map<String, Object> pr, long time) {
        this.index = index;
        this.ev = ev;
        this.id = id;
        this.pl = pl;
        this.pr = pr;
        this.time = time;
    }

    public int getIndex() {
        return index;
    }

    public String getEvent() {
        return ev;
    }

    public Map<String, Object> getId() {
        return id;
    }

    public String getPlatform() {
        return pl;
    }

    public Map<String, Object> getProperties() {
        return pr;
    }

    public long getTime() {
        return time;
    }
}

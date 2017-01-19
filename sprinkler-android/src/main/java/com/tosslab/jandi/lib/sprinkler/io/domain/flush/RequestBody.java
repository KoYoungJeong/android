package com.tosslab.jandi.lib.sprinkler.io.domain.flush;

import com.tosslab.jandi.lib.sprinkler.io.domain.track.Track;

import java.util.List;

/**
 * Created by tonyjs on 15. 7. 23..
 */
public final class RequestBody {
    private final String version;
    private int num;
    private String deviceId;
    private long lastDate;
    private List<Track> data;

    public RequestBody(int num, String deviceId, long lastDate, List<Track> data, String version) {
        this.num = num;
        this.deviceId = deviceId;
        this.lastDate = lastDate;
        this.data = data;
        this.version = version;
    }
}

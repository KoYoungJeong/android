package com.tosslab.jandi.lib.sprinkler.io;

import com.tosslab.jandi.lib.sprinkler.io.model.Track;

import java.util.List;

/**
 * Created by tonyjs on 15. 7. 23..
 */
final class RequestBody {
    private int num;
    private String deviceId;
    private long lastDate;
    private List<Track> data;

    public RequestBody(int num, String deviceId, long lastDate, List<Track> data) {
        this.num = num;
        this.deviceId = deviceId;
        this.lastDate = lastDate;
        this.data = data;
    }
}

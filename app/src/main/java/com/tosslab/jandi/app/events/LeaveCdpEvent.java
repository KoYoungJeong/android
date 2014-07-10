package com.tosslab.jandi.app.events;

/**
 * Created by justinygchoi on 2014. 7. 10..
 */
public class LeaveCdpEvent {
    public int cdpType;
    public int cdpId;

    public LeaveCdpEvent(int cdpId, int cdpType) {
        this.cdpId = cdpId;
        this.cdpType = cdpType;
    }
}

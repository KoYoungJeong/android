package com.tosslab.jandi.app.events;

/**
 * Created by justinygchoi on 2014. 7. 15..
 */
public class InviteCdpEvent {
    public int cdpType;
    public int cdpId;

    public InviteCdpEvent(int cdpId, int cdpType) {
        this.cdpId = cdpId;
        this.cdpType = cdpType;
    }
}

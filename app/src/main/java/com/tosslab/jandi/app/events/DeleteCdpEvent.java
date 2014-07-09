package com.tosslab.jandi.app.events;

/**
 * Created by justinygchoi on 2014. 5. 28..
 */
public class DeleteCdpEvent {
    public int cdpType;
    public int cdpId;

    public DeleteCdpEvent(int cdpId, int cdpType) {
        this.cdpId = cdpId;
        this.cdpType = cdpType;
    }
}

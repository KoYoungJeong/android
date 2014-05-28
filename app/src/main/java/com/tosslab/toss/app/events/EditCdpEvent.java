package com.tosslab.toss.app.events;

/**
 * Created by justinygchoi on 2014. 5. 28..
 */
public class EditCdpEvent {
    public int cdpType;
    public int cdpId;

    public EditCdpEvent(int cdpId, int cdpType) {
        this.cdpId = cdpId;
        this.cdpType = cdpType;
    }
}

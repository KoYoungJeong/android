package com.tosslab.jandi.app.events;

/**
 * Created by justinygchoi on 2014. 5. 28..
 */
public class ModifyCdpEvent {
    public int cdpType;
    public int cdpId;
    public String currentName;

    public ModifyCdpEvent(int cdpId, int cdpType, String currentName) {
        this.cdpId = cdpId;
        this.cdpType = cdpType;
        this.currentName = currentName;
    }
}

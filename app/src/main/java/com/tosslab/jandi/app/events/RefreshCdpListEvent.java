package com.tosslab.jandi.app.events;

import com.tosslab.jandi.app.lists.CdpItemManager;

/**
 * Created by justinygchoi on 2014. 5. 27..
 * MainActivity -> MainLeftFragment
 */
public class RefreshCdpListEvent {
    public CdpItemManager cdpItemManager;

    public RefreshCdpListEvent(CdpItemManager cdpItemManager) {
        this.cdpItemManager = cdpItemManager;
    }
}

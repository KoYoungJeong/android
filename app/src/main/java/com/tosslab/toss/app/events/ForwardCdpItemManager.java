package com.tosslab.toss.app.events;

import com.tosslab.toss.app.lists.CdpItemManager;

/**
 * Created by justinygchoi on 2014. 7. 8..
 */
public class ForwardCdpItemManager {
    public CdpItemManager cdpItemManager;

    public ForwardCdpItemManager(CdpItemManager cdpItemManager) {
        this.cdpItemManager = cdpItemManager;
    }
}

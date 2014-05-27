package com.tosslab.toss.app.events;

import com.tosslab.toss.app.network.entities.TossRestInfosForSideMenu;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
public class RefreshCdpListEvent {
    public TossRestInfosForSideMenu mInfos;

    public RefreshCdpListEvent(TossRestInfosForSideMenu infos) {
        mInfos = infos;
    }
}

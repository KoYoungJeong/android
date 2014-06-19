package com.tosslab.toss.app.events;

import com.tosslab.toss.app.network.models.ResLeftSideMenu;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
public class RefreshCdpListEvent {
    public ResLeftSideMenu mResLeftSideMenu;

    public RefreshCdpListEvent(ResLeftSideMenu infos) {
        mResLeftSideMenu = infos;
    }
}

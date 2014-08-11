package com.tosslab.jandi.app.ui.events;

import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
public class RetrievePrivateGroupList {
    public List<ResLeftSideMenu.PrivateGroup> privateGroups;
    public RetrievePrivateGroupList(List<ResLeftSideMenu.PrivateGroup> privateGroups) {
        this.privateGroups = privateGroups;
    }
}

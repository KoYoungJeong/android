package com.tosslab.jandi.app.events;

import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
public class RetrieveUserList {
    public List<ResLeftSideMenu.User> users;
    public RetrieveUserList(List<ResLeftSideMenu.User> users) {
        this.users = users;
    }
}

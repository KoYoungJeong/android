package com.tosslab.jandi.app.events;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
public class RetrieveUserList {
    public List<FormattedEntity> users;
    public RetrieveUserList(List<FormattedEntity> users) {
        this.users = users;
    }
}

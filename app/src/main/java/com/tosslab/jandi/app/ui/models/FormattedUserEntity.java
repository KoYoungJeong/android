package com.tosslab.jandi.app.ui.models;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

/**
 * Created by justinygchoi on 2014. 8. 14..
 */
public class FormattedUserEntity {
    private ResLeftSideMenu.Entity entity;
    public boolean isSelectedToBeJoined = false;

    public FormattedUserEntity(ResLeftSideMenu.User user) {
        entity = user;
    }

    public ResLeftSideMenu.User getUser() {
        return (entity instanceof ResLeftSideMenu.User)
                ? (ResLeftSideMenu.User) entity
                : null;
    }

    public String getProfileUrl() {
        return JandiConstants.SERVICE_ROOT_URL + getUser().u_photoUrl;
    }
}

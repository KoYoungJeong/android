package com.tosslab.jandi.app.events;

import com.tosslab.jandi.app.lists.FormattedEntity;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 9. 18..
 */
public class RetrieveTeamInformation {
    public List<FormattedEntity> users;
    public RetrieveTeamInformation(List<FormattedEntity> users) {
        this.users = users;
    }
}

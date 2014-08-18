package com.tosslab.jandi.app.events;

import com.tosslab.jandi.app.lists.FormattedEntity;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
public class RetrievePrivateGroupList {
    public List<FormattedEntity> privateGroups;

    public RetrievePrivateGroupList(List<FormattedEntity> channels) {
        this.privateGroups = channels;
    }
}

package com.tosslab.jandi.app.ui.events;

import com.tosslab.jandi.app.ui.models.FormattedEntity;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
public class RetrieveChannelList {
    public List<FormattedEntity> channels;

    public RetrieveChannelList(List<FormattedEntity> channels) {
        this.channels = channels;
    }
}

package com.tosslab.jandi.app.ui.events;

import com.tosslab.jandi.app.ui.models.FormattedChannel;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
public class RetrieveChannelList {
    public List<FormattedChannel> channels;

    public RetrieveChannelList(List<FormattedChannel> channels) {
        this.channels = channels;
    }
}

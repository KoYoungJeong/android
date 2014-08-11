package com.tosslab.jandi.app.ui.events;

import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
public class RetrieveChannelList {
    public List<ResLeftSideMenu.Channel> joinedChannels;
    public List<ResLeftSideMenu.Channel> unJoinedChannels;
    public RetrieveChannelList(List<ResLeftSideMenu.Channel> joinedChannels,
                               List<ResLeftSideMenu.Channel> unJoinedChannels) {
        this.joinedChannels = joinedChannels;
        this.unJoinedChannels = unJoinedChannels;
    }
}

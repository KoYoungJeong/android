package com.tosslab.toss.app.network.entities;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 6. 5..
 */
public class ResChannelMessagesUpdated extends CdpMessages {
    public int messageCount;
    public List<ChannelMessage> messages;
}

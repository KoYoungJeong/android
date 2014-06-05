package com.tosslab.toss.app.network.entities;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 6. 5..
 */
public class ResDirectMessagesUpdated extends CdpMessages {
    public int messageCount;
    public List<DirectMessage> messages;
}

package com.tosslab.toss.app.network.models;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 6. 5..
 */
public class ResDirectMessagesUpdated extends CdpMessages {
    public int messageCount;
    public List<DirectMessage> messages;
}

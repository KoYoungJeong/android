package com.tosslab.toss.app.network.entities;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
public class ResChannelMessages extends CdpMessages {
    public int numOfPage;
    public int firstIdOfReceviedList;
    public boolean isFirst;
    public int messageCount;
    public List<ChannelMessage> messages;
}

package com.tosslab.toss.app.network.entities;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 6. 3..
 */
public class ResPrivateGroupMessages extends CdpMessages {
    public int numOfPage;
    public int firstIdOfReceviedList;
    public boolean isFirst;
    public int messageCount;
    public List<PrivateGroupMessage> messages;
}

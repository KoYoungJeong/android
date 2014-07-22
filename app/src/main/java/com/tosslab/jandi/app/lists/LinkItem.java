package com.tosslab.jandi.app.lists;

import com.tosslab.jandi.app.network.models.ResMessages;

import org.apache.log4j.Logger;

/**
 * Created by justinygchoi on 2014. 7. 22..
 */
public class LinkItem {
    private final Logger log = Logger.getLogger(LinkItem.class);

    public static final int MESSAGE_TYPE_STRING     = 0;
    public static final int MESSAGE_TYPE_IMAGE      = 1;
    public static final int MESSAGE_TYPE_COMMENT    = 2;
    public static final int MESSAGE_TYPE_FILE       = 3;

    private ResMessages.Link mLink;

    public LinkItem(ResMessages.Link link) {
        mLink = link;
    }

    public int getLinkId() {
        return mLink.id;
    }

    public int getMessageId() {
        return mLink.messageId;
    }
}

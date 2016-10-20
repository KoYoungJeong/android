package com.tosslab.jandi.app.events.messages;

import com.tosslab.jandi.app.network.models.ResMessages;

/**
 * Created by tonyjs on 2016. 9. 22..
 */
public class MentionMessageEvent {

    private ResMessages.Link link;

    public MentionMessageEvent(ResMessages.Link link) {
        this.link = link;
    }

    public ResMessages.Link getLink() {
        return link;
    }

}

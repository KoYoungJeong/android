package com.tosslab.jandi.app.events.messages;

import com.tosslab.jandi.app.network.models.ResMessages;

/**
 * Created by tonyjs on 16. 6. 16..
 */
public class RequestUpsertLinkEvent {
    private ResMessages.Link link;

    private RequestUpsertLinkEvent(ResMessages.Link link) {
        this.link = link;
    }

    public static RequestUpsertLinkEvent create(ResMessages.Link link) {
        return new RequestUpsertLinkEvent(link);
    }

    public ResMessages.Link getLink() {
        return link;
    }

    public void setLink(ResMessages.Link link) {
        this.link = link;
    }
}

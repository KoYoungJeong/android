package com.tosslab.jandi.app.ui.message.to.queue;

import com.tosslab.jandi.app.network.models.ResMessages;

public class NewMessageFromLocalContainer implements MessageContainer<ResMessages.Link> {
    private final ResMessages.Link link;

    public NewMessageFromLocalContainer(ResMessages.Link link) {this.link = link;}

    @Override
    public LoadType getQueueType() {
        return LoadType.NewFromLocal;
    }

    @Override
    public ResMessages.Link getData() {
        return link;
    }
}

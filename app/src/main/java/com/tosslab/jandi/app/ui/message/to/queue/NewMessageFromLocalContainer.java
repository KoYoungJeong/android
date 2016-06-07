package com.tosslab.jandi.app.ui.message.to.queue;

import com.tosslab.jandi.app.ui.message.to.MessageState;

public class NewMessageFromLocalContainer implements MessageContainer<MessageState> {
    private final MessageState messageState;

    public NewMessageFromLocalContainer(MessageState messageState) {this.messageState = messageState;}

    @Override
    public LoadType getQueueType() {
        return LoadType.NewFromLocal;
    }

    @Override
    public MessageState getData() {
        return messageState;
    }
}

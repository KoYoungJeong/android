package com.tosslab.jandi.app.ui.message.to.queue;

import com.tosslab.jandi.app.ui.message.to.MessageState;

/**
 * Created by Steve SeongUg Jung on 15. 3. 5..
 */
public class OldMessageContainer implements MessageContainer<MessageState> {

    private final MessageState messageState;
    public OldMessageContainer(MessageState messageState) {
        this.messageState = messageState;
    }

    @Override
    public LoadType getQueueType() {
        return LoadType.Old;
    }

    @Override
    public MessageState getData() {
        return messageState;
    }

}

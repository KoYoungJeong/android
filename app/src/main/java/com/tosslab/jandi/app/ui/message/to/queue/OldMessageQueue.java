package com.tosslab.jandi.app.ui.message.to.queue;

import com.tosslab.jandi.app.ui.message.to.MessageState;

/**
 * Created by Steve SeongUg Jung on 15. 3. 5..
 */
public class OldMessageQueue implements MessageQueue {

    private final MessageState messageState;

    public OldMessageQueue(MessageState messageState) {
        this.messageState = messageState;
    }

    @Override
    public LoadType getQueueType() {
        return LoadType.Old;
    }

    @Override
    public Object getData() {
        return messageState;
    }

}

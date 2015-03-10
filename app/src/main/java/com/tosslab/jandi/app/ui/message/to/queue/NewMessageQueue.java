package com.tosslab.jandi.app.ui.message.to.queue;

import com.tosslab.jandi.app.ui.message.to.MessageState;

/**
 * Created by Steve SeongUg Jung on 15. 3. 5..
 */
public class NewMessageQueue implements MessageQueue {

    private final MessageState messageState;

    public NewMessageQueue(MessageState messageState) {
        this.messageState = messageState;
    }


    @Override
    public LoadType getQueueType() {
        return LoadType.New;
    }

    @Override
    public Object getData() {
        return messageState;
    }

}
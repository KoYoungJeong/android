package com.tosslab.jandi.app.ui.message.to.queue;

import com.tosslab.jandi.app.ui.message.to.SendingMessage;

/**
 * Created by Steve SeongUg Jung on 15. 3. 5..
 */
public class SendingMessageQueue implements MessageQueue {

    private final SendingMessage sendingMessage;

    public SendingMessageQueue(SendingMessage sendingMessage) {
        this.sendingMessage = sendingMessage;
    }

    @Override
    public LoadType getQueueType() {
        return LoadType.Send;
    }

    @Override
    public Object getData() {
        return sendingMessage;
    }

}

package com.tosslab.jandi.app.ui.message.to.queue;

import com.tosslab.jandi.app.ui.message.to.SendingMessage;

/**
 * Created by Steve SeongUg Jung on 15. 3. 5..
 */
public class SendingMessageContainer implements MessageContainer<SendingMessage> {

    private final SendingMessage sendingMessage;

    public SendingMessageContainer(SendingMessage sendingMessage) {
        this.sendingMessage = sendingMessage;
    }

    @Override
    public LoadType getQueueType() {
        return LoadType.Send;
    }

    @Override
    public SendingMessage getData() {
        return sendingMessage;
    }

}

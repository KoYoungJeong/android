package com.tosslab.jandi.app.ui.message.to.queue;

/**
 * Created by tonyjs on 15. 6. 17..
 */
public class UpdateLinkPreviewMessageQueue implements MessageQueue {

    private long messageId;

    public UpdateLinkPreviewMessageQueue(long messageId) {
        this.messageId = messageId;
    }

    @Override
    public LoadType getQueueType() {
        return LoadType.UpdateLinkPreview;
    }

    @Override
    public Long getData() {
        return messageId;
    }
}

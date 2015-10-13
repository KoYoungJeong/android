package com.tosslab.jandi.app.ui.message.to.queue;

/**
 * Created by tonyjs on 15. 6. 17..
 */
public class UpdateLinkPreviewMessageQueue implements MessageQueue {

    private int messageId;

    public UpdateLinkPreviewMessageQueue(int messageId) {
        this.messageId = messageId;
    }

    @Override
    public LoadType getQueueType() {
        return LoadType.UpdateLinkPreview;
    }

    @Override
    public Integer getData() {
        return messageId;
    }
}

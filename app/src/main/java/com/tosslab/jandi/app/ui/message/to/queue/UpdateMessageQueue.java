package com.tosslab.jandi.app.ui.message.to.queue;

import com.tosslab.jandi.app.ui.message.to.UpdateMessage;

/**
 * Created by tonyjs on 15. 6. 17..
 */
public class UpdateMessageQueue implements MessageQueue {

    private UpdateMessage updateMessage;

    public UpdateMessageQueue(int teamId, int messageId) {
        updateMessage = new UpdateMessage(teamId, messageId);
    }

    @Override
    public LoadType getQueueType() {
        return LoadType.Update;
    }

    @Override
    public UpdateMessage getData() {
        return updateMessage;
    }
}

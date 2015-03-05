package com.tosslab.jandi.app.ui.message.to.queue;

/**
 * Created by Steve SeongUg Jung on 15. 3. 5..
 */
public class SavedMessageQueue implements MessageQueue {


    @Override
    public LoadType getQueueType() {
        return LoadType.Saved;
    }

    @Override
    public Object getData() {
        return null;
    }

}

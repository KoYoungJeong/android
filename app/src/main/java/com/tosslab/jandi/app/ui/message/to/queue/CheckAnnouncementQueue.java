package com.tosslab.jandi.app.ui.message.to.queue;

/**
 * Created by tonyjs on 15. 6. 24..
 */
public class CheckAnnouncementQueue implements MessageQueue {
    @Override
    public LoadType getQueueType() {
        return LoadType.CheckAnnouncement;
    }

    @Override
    public Object getData() {
        return null;
    }
}

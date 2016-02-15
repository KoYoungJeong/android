package com.tosslab.jandi.app.events.entities;

/**
 * Created by tee on 15. 8. 31..
 */
public class TopicFolderMoveCallEvent {
    private long folderId;
    private long topicId;

    public long getFolderId() {
        return folderId;
    }

    public void setFolderId(long folderId) {
        this.folderId = folderId;
    }

    public long getTopicId() {
        return topicId;
    }

    public void setTopicId(long topicId) {
        this.topicId = topicId;
    }
}

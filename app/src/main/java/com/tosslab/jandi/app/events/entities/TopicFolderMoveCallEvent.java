package com.tosslab.jandi.app.events.entities;

/**
 * Created by tee on 15. 8. 31..
 */
public class TopicFolderMoveCallEvent {
    private int folderId;
    private int topicId;

    public int getFolderId() {
        return folderId;
    }

    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }

    public int getTopicId() {
        return topicId;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }
}

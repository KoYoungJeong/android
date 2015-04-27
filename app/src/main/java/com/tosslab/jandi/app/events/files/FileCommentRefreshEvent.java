package com.tosslab.jandi.app.events.files;

/**
 * Created by Steve SeongUg Jung on 15. 4. 7..
 */
public class FileCommentRefreshEvent {
    private final int id;

    public FileCommentRefreshEvent(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}

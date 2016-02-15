package com.tosslab.jandi.app.events.files;

/**
 * Created by tonyjs on 16. 2. 4..
 */
public class FileStarredStateChangeEvent {
    private boolean starredState;

    public FileStarredStateChangeEvent(boolean starredState) {
        this.starredState = starredState;
    }

    public boolean getStarredState() {
        return starredState;
    }
}

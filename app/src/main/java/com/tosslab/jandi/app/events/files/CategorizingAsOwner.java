package com.tosslab.jandi.app.events.files;

/**
 * Created by justinygchoi on 2014. 8. 15..
 */
public class CategorizingAsOwner {
    public static final long EVERYONE = -1;
    public long userId;

    public CategorizingAsOwner(long userId) {
        this.userId = userId;
    }
}

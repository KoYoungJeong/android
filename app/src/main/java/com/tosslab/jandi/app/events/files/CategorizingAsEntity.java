package com.tosslab.jandi.app.events.files;

/**
 * Created by justinygchoi on 2014. 9. 11..
 */
public class CategorizingAsEntity {
    public static final int ACCESSIBLE = -1;
    public static final int JOINED = -2;
    public long sharedEntityId;

    public CategorizingAsEntity(long sharedEntityId) {
        this.sharedEntityId = sharedEntityId;
    }
}

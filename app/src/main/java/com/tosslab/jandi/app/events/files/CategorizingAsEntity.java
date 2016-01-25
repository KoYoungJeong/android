package com.tosslab.jandi.app.events.files;

/**
 * Created by justinygchoi on 2014. 9. 11..
 */
public class CategorizingAsEntity {
    public static final int EVERYWHERE = -1;
    public long sharedEntityId;

    public CategorizingAsEntity(long sharedEntityId) {
        this.sharedEntityId = sharedEntityId;
    }
}

package com.tosslab.jandi.app.events;

/**
 * Created by justinygchoi on 2014. 9. 11..
 */
public class CategorizingAsEntity {
    public static final int EVERYWHERE = -1;
    public int sharedEntityId;

    public CategorizingAsEntity(int sharedEntityId) {
        this.sharedEntityId = sharedEntityId;
    }
}

package com.tosslab.jandi.app.ui.events;

/**
 * Created by justinygchoi on 2014. 8. 15..
 */
public class CategorizingAsOwner {
    public static final String EVERYONE = "all";
    public String userId;
    public CategorizingAsOwner(String userId) {
        this.userId = userId;
    }

    public CategorizingAsOwner(int userId) {
        this.userId = userId + "";
    }
}

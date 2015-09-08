package com.tosslab.jandi.app.events.profile;

import android.view.View;

/**
 * Created by justinygchoi on 2014. 9. 4..
 */
public class ShowProfileEvent {
    public int userId;

    public ShowProfileEvent(int userId) {
        this.userId = userId;
    }
}
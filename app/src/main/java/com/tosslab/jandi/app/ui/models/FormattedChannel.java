package com.tosslab.jandi.app.ui.models;

import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

/**
 * Created by justinygchoi on 2014. 8. 12..
 */
public class FormattedChannel {
    public static final int TYPE_REAL_CHANNEL   = 0;
    public static final int TYPE_TITLE_JOINED   = 1;
    public static final int TYPE_TITLE_UNJOINED = 2;

    public static final boolean JOINED      = true;
    public static final boolean UNJOINED    = false;

    public ResLeftSideMenu.Channel original;
    public boolean isJoined;
    public int type;

    public FormattedChannel(ResLeftSideMenu.Channel original, boolean isJoined) {
        this.original = original;
        this.isJoined = isJoined;
        this.type = TYPE_REAL_CHANNEL;
    }

    public FormattedChannel(int type) {
        this.original = null;
        this.isJoined = true;   // NO MATTER
        this.type = type;
    }
}

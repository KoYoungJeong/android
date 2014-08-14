package com.tosslab.jandi.app.ui.models;

import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 12..
 */
public class FormattedEntity {
    // 채널 일 경우
    public static final int TYPE_REAL_CHANNEL           = 0;
    public static final int TYPE_REAL_PRIVATE_GROUP     = 1;

    public static final int TYPE_TITLE_JOINED_CHANNEL   = 2;
    public static final int TYPE_TITLE_UNJOINED_CHANNEL = 3;

    public static final boolean JOINED      = true;
    public static final boolean UNJOINED    = false;

    private ResLeftSideMenu.Entity entity;

    public boolean isJoined;
    public int type;

    public FormattedEntity(ResLeftSideMenu.Channel channel, boolean isJoined) {
        this.entity = channel;
        this.isJoined = isJoined;
        this.type = TYPE_REAL_CHANNEL;
    }

    public FormattedEntity(ResLeftSideMenu.PrivateGroup privateGroup) {
        this.entity = privateGroup;
        this.type = TYPE_REAL_PRIVATE_GROUP;
    }

    public FormattedEntity(int type) {
        this.isJoined = JOINED;   // NO MATTER
        this.type = type;
    }

    public boolean isChannel() {
        return (type == FormattedEntity.TYPE_REAL_CHANNEL);
    }

    public boolean isPrivateGroup() {
        return (type == FormattedEntity.TYPE_REAL_PRIVATE_GROUP);
    }

    public ResLeftSideMenu.Channel getChannel() {
        return (entity instanceof ResLeftSideMenu.Channel)
                ? (ResLeftSideMenu.Channel) entity
                : null;
    }

    public ResLeftSideMenu.PrivateGroup getPrivateGroup() {
        return (entity instanceof ResLeftSideMenu.PrivateGroup)
                ? (ResLeftSideMenu.PrivateGroup) entity
                : null;
    }

    public List<Integer> getMembers() {
        if (this.type == TYPE_REAL_CHANNEL) {
            return getChannel().ch_members;
        } else if (this.type == TYPE_REAL_PRIVATE_GROUP) {
            return getPrivateGroup().pg_members;
        } else {
            return null;
        }
    }
}

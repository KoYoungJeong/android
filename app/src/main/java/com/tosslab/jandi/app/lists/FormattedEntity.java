package com.tosslab.jandi.app.lists;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 12..
 */
public class FormattedEntity {
    // 채널 일 경우
    public static final int TYPE_REAL_CHANNEL           = 0;
    public static final int TYPE_REAL_PRIVATE_GROUP     = 1;
    public static final int TYPE_REAL_USER              = 3;

    public static final int TYPE_TITLE_JOINED_CHANNEL   = 4;
    public static final int TYPE_TITLE_UNJOINED_CHANNEL = 5;

    public static final boolean JOINED      = true;
    public static final boolean UNJOINED    = false;

    private ResLeftSideMenu.Entity entity;

    public int type;
    public boolean isJoined;                        // if type is channel
    public boolean isSelectedToBeJoined = false;    // if type is user

    // MessageMarker
    public int lastLinkId = -1;
    public int alarmCount = 0;


    public FormattedEntity(ResLeftSideMenu.Channel channel, boolean isJoined, List<ResLeftSideMenu.MessageMarker> markers) {
        patchMessageMarker(channel, markers);
        this.entity = channel;
        this.isJoined = isJoined;
        this.type = TYPE_REAL_CHANNEL;
    }

    public FormattedEntity(ResLeftSideMenu.PrivateGroup privateGroup, List<ResLeftSideMenu.MessageMarker> markers) {
        patchMessageMarker(privateGroup, markers);
        this.entity = privateGroup;
        this.type = TYPE_REAL_PRIVATE_GROUP;
    }

    public FormattedEntity(ResLeftSideMenu.User user) {
        this.entity = user;
        this.type = TYPE_REAL_USER;
    }

    public FormattedEntity(ResLeftSideMenu.User user, List<ResLeftSideMenu.MessageMarker> markers) {
        patchMessageMarker(user, markers);
        this.entity = user;
        this.type = TYPE_REAL_USER;
    }

    public FormattedEntity(int type) {
        this.isJoined = JOINED;   // NO MATTER
        this.type = type;
    }

    private void patchMessageMarker(ResLeftSideMenu.Entity entity, List<ResLeftSideMenu.MessageMarker> markers) {
        for (ResLeftSideMenu.MessageMarker marker : markers) {
            if (entity.id == marker.entityId) {
                alarmCount = marker.alarmCount;
                lastLinkId = marker.lastLinkId;
            }
        }
    }

    public boolean isChannel() {
        return (type == FormattedEntity.TYPE_REAL_CHANNEL);
    }
    public boolean isPrivateGroup() {
        return (type == FormattedEntity.TYPE_REAL_PRIVATE_GROUP);
    }
    public boolean isUser() {
        return (type == FormattedEntity.TYPE_REAL_USER);
    }

    public ResLeftSideMenu.Entity getEntity() {
        return entity;
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

    public ResLeftSideMenu.User getUser() {
        return (entity instanceof ResLeftSideMenu.User)
                ? (ResLeftSideMenu.User) entity
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

    public String getUserName() {
        ResLeftSideMenu.User me = getUser();
        return me.u_lastName + " " + me.u_firstName;
    }

    public String getUserEmail() {
        ResLeftSideMenu.User me = getUser();
        return me.u_email;
    }

    public String getUserProfileUrl() {
        return JandiConstants.SERVICE_ROOT_URL + getUser().u_photoUrl;
    }

    public boolean hasGivenId(int entityId) {
        return (this.entity.id == entityId);
    }

    public boolean hasGivenIds(List<Integer> entityIds) {
        for (int entityId : entityIds) {
            if (hasGivenId(entityId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        switch (this.type) {
            case TYPE_REAL_CHANNEL:
                return "#" + entity.name;
            case TYPE_REAL_USER:
                return "@" + entity.name;
            default:
                return entity.name;
        }
    }
}

package com.tosslab.jandi.app.lists;

import android.graphics.Color;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import java.util.ArrayList;
import java.util.Collection;

import rx.Observable;
import rx.functions.Func0;

/**
 * Created by justinygchoi on 2014. 8. 12..
 */
public class FormattedEntity {
    // 채널 일 경우
    public static final int TYPE_REAL_CHANNEL = JandiConstants.TYPE_PUBLIC_TOPIC;
    public static final int TYPE_REAL_USER = JandiConstants.TYPE_DIRECT_MESSAGE;
    public static final int TYPE_REAL_PRIVATE_GROUP = JandiConstants.TYPE_PRIVATE_TOPIC;

    // Dummy Entity
    // TODO 통합
    public static final int TYPE_TITLE_JOINED_CHANNEL = 4;
    public static final int TYPE_TITLE_UNJOINED_CHANNEL = 5;
    public static final int TYPE_EVERYWHERE = 6;

    public static final boolean JOINED = true;
    public static final boolean UNJOINED = false;
    public int type;
    public boolean isJoined;                        // if type is channel
    public boolean isSelectedToBeJoined = false;    // if type is user
    // MessageMarker
    public int lastLinkId = -1;
    public int alarmCount = 0;
    public boolean announcementOpened = false;
    // Starred
    public boolean isStarred = false;
    private ResLeftSideMenu.Entity entity;
    // Topic Push
    public boolean isTopicPushOn = true;

    public FormattedEntity(ResLeftSideMenu.Channel channel, boolean isJoined) {
        this.entity = channel;
        this.isJoined = isJoined;
        this.type = TYPE_REAL_CHANNEL;
    }

    public FormattedEntity(ResLeftSideMenu.Channel channel, boolean isJoined, int alarmCount, int lastLinkId) {
        this(channel, isJoined);
        this.alarmCount = alarmCount;
        this.lastLinkId = lastLinkId;
    }

    public FormattedEntity(ResLeftSideMenu.PrivateGroup privateGroup) {
        this.entity = privateGroup;
        this.type = TYPE_REAL_PRIVATE_GROUP;
    }

    public FormattedEntity(ResLeftSideMenu.PrivateGroup privateGroup, int alarmCount, int lastLinkId) {
        this(privateGroup);
        this.alarmCount = alarmCount;
        this.lastLinkId = lastLinkId;
    }

    public FormattedEntity(ResLeftSideMenu.User user) {
        this.entity = user;
        this.type = TYPE_REAL_USER;
    }

    public FormattedEntity(ResLeftSideMenu.User user, int alarmCount, int lastLinkId) {
        this(user);
        this.alarmCount = alarmCount;
        this.lastLinkId = lastLinkId;
    }

    public FormattedEntity(int type) {
        this.isJoined = JOINED;   // NO MATTER
        this.type = type;
    }

    public FormattedEntity() {

    }

//    private void patchMessageMarker(ResLeftSideMenu.Entity entity, List<ResLeftSideMenu.MessageMarker> markers) {
//        for (ResLeftSideMenu.MessageMarker marker : markers) {
//            if (entity.id == marker.entityId) {
//                alarmCount = marker.alarmCount;
//                lastLinkId = marker.lastLinkId;
//            }
//        }
//    }

    public boolean isPublicTopic() {
        return (type == FormattedEntity.TYPE_REAL_CHANNEL);
    }

    public boolean isPrivateGroup() {
        return (type == FormattedEntity.TYPE_REAL_PRIVATE_GROUP);
    }

    public boolean isUser() {
        return (type == FormattedEntity.TYPE_REAL_USER);
    }

    public boolean isDummy() {
        return (!isPublicTopic() && !isPrivateGroup() && !isUser());
    }

    /**
     * *********************************************************
     * 공통 Getter
     * **********************************************************
     */
    public ResLeftSideMenu.Entity getEntity() {
        return entity;
    }

    public int getId() {
        return getEntity().id;
    }

    public String getName() {
        // Dummy entity일 경우 이름이 없다.
        if (isDummy()) {
            return null;
        }

        return getEntity().name;
    }

    public int getMemberCount() {
        if (isPublicTopic()) {
            return getChannel().ch_members.size();
        }

        if (isPrivateGroup()) {
            return getPrivateGroup().pg_members.size();
        }

        return 0;
    }

    public int getIconImageResId() {
        if (isPublicTopic()) {
            return R.drawable.topiclist_icon_topic;
        } else if (isPrivateGroup()) {
            return R.drawable.topiclist_icon_topic_private;
        } else if (isDummy()) {
            return getDummyImageRes();
        } else {
            // User
            return -1;
        }
    }

    /**
     * *********************************************************
     * 개별 Getter
     * **********************************************************
     */
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

    public Collection<Integer> getMembers() {
        if (this.type == TYPE_REAL_CHANNEL) {
            return Observable.from(getChannel().ch_members)
                    .collect((Func0<ArrayList<Integer>>) ArrayList::new,
                            ArrayList::add)
                    .toBlocking()
                    .first();

        } else if (this.type == TYPE_REAL_PRIVATE_GROUP) {
            return Observable.from(getPrivateGroup().pg_members)
                    .collect((Func0<ArrayList<Integer>>) ArrayList::new,
                            ArrayList::add)
                    .toBlocking()
                    .first();
        } else {
            return null;
        }
    }

    public String getUserStatusMessage() {
        ResLeftSideMenu.User me = getUser();
        return me.u_statusMessage;
    }

    public String getUserEmail() {
        ResLeftSideMenu.User me = getUser();
        return me.u_email;
    }

    public String getUserSmallProfileUrl() {
        String userProfileUrl;
        if (getUser().u_photoThumbnailUrl != null) {
            userProfileUrl = getUser().u_photoThumbnailUrl.smallThumbnailUrl;
        } else {
            userProfileUrl = getUser().u_photoUrl;
        }

        if (TextUtils.isEmpty(userProfileUrl)) {
            return null;
        }

        if (hasProtocol(userProfileUrl)) {
            return userProfileUrl;
        }

        return JandiConstantsForFlavors.SERVICE_FILE_URL + userProfileUrl;
    }

    public String getUserLargeProfileUrl() {
        String userProfileUrl;
        if (getUser().u_photoThumbnailUrl != null) {
            userProfileUrl = getUser().u_photoThumbnailUrl.largeThumbnailUrl;
        } else {
            userProfileUrl = getUser().u_photoUrl;
        }

        if (TextUtils.isEmpty(userProfileUrl)) {
            return null;
        }

        if (hasProtocol(userProfileUrl)) {
            return userProfileUrl;
        }

        return JandiConstantsForFlavors.SERVICE_FILE_URL + userProfileUrl;
    }

    private boolean hasProtocol(String url) {
        return !TextUtils.isEmpty(url) && url.startsWith("http");
    }

    public String getUserPhoneNumber() {
        ResLeftSideMenu.User me = getUser();
        if (me.u_extraData != null && me.u_extraData.phoneNumber != null) {
            return me.u_extraData.phoneNumber;
        } else {
            return "";
        }
    }

    public String getUserDivision() {
        ResLeftSideMenu.User me = getUser();
        if (me.u_extraData != null && me.u_extraData.department != null) {
            return me.u_extraData.department;
        } else {
            return "";
        }
    }

    public String getUserPosition() {
        ResLeftSideMenu.User me = getUser();
        if (me.u_extraData != null && me.u_extraData.position != null) {
            return me.u_extraData.position;
        } else {
            return "";
        }
    }

    public int getDummyNameRes() {
        if (this.type == TYPE_EVERYWHERE) {
            return R.string.jandi_file_category_everywhere;
        } else {
            return 0;
        }
    }

    private int getDummyImageRes() {
        return R.drawable.topiclist_icon_topic;
    }

    public int getMyColor() {
        final int color[] = {
                Color.parseColor("#e91e63"),
                Color.parseColor("#9c27b0"),
                Color.parseColor("#3f51b5"),
                Color.parseColor("#03a9f4"),
                Color.parseColor("#00bcd4"),
                Color.parseColor("#009688"),
                Color.parseColor("#795548"),
                Color.parseColor("#259b24"),
                Color.parseColor("#8bc34a"),
                Color.parseColor("#cddc39"),
                Color.parseColor("#ffc107"),
                Color.parseColor("#795548"),
        };

        return color[this.entity.id % color.length];
    }

    public boolean hasGivenId(int entityId) {
        return (this.entity.id == entityId);
    }

    public boolean hasGivenIds(Collection<Integer> entityIds) {
        for (int entityId : entityIds) {
            if (hasGivenId(entityId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isMine(int myId) {
        if (this.type == TYPE_REAL_CHANNEL) {
            return (getChannel().ch_creatorId == myId) ? true : false;
        } else if (this.type == TYPE_REAL_PRIVATE_GROUP) {
            return (getPrivateGroup().pg_creatorId == myId) ? true : false;
        } else {
            return false;
        }
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

    public String getDescription() {

        switch (this.type) {
            case TYPE_REAL_CHANNEL:
                return ((ResLeftSideMenu.Channel) entity).description;
            case TYPE_REAL_PRIVATE_GROUP:
                return ((ResLeftSideMenu.PrivateGroup) entity).description;
            default:
            case TYPE_REAL_USER:
                return "";
        }
    }
}

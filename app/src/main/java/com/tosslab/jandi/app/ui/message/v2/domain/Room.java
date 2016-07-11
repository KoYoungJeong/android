package com.tosslab.jandi.app.ui.message.v2.domain;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.team.TeamInfoLoader;

public class Room {
    public static final long INVALID_ROOM_ID = -1;

    private final int entityType;
    private final long entityId;
    private final long teamId;
    private final boolean fromPush;
    private long roomId;

    private Room(int entityType, long entityId, long teamId, boolean fromPush) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.teamId = teamId;
        this.fromPush = fromPush;
    }

    private Room(int entityType, long entityId, long teamId, boolean fromPush, long roomId) {
        this(entityType, entityId, teamId, fromPush);
        this.roomId = roomId;
    }

    public static Room create(long entityId, boolean fromPush) {
        int entityType;

        if (TeamInfoLoader.getInstance().isTopic(entityId)) {
            if (TeamInfoLoader.getInstance().isPublicTopic(entityId)) {
                entityType = JandiConstants.TYPE_PUBLIC_TOPIC;
            } else {
                entityType = JandiConstants.TYPE_PRIVATE_TOPIC;
            }

        } else {
            entityType = JandiConstants.TYPE_DIRECT_MESSAGE;
        }

        long roomId;
        if (entityType != JandiConstants.TYPE_DIRECT_MESSAGE) {
            roomId = entityId;
        } else {
            roomId = TeamInfoLoader.getInstance().getChatId(entityId);
        }

        long teamId = TeamInfoLoader.getInstance().getTeamId();

        return new Room(entityType, entityId, teamId, fromPush, roomId);
    }

    public int getEntityType() {
        return entityType;
    }

    public long getEntityId() {
        return entityId;
    }

    public long getTeamId() {
        return teamId;
    }

    public boolean isFromPush() {
        return fromPush;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }
}

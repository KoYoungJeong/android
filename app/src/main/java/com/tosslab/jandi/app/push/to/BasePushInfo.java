package com.tosslab.jandi.app.push.to;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        defaultImpl = MessagePushInfo.class,
        property = "push_type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = MarkerPushInfo.class, name = "marker_updated"),
        @JsonSubTypes.Type(value = MessagePushInfo.class, name = "message_created"),
        @JsonSubTypes.Type(value = MessagePushInfo.class, name = "comment_created"),
        @JsonSubTypes.Type(value = MessagePushInfo.class, name = "file_shared")})
public class BasePushInfo {
    @JsonProperty("push_type")
    private String pushType;
    @JsonProperty("badge_count")
    private int badgeCount;
    @JsonProperty("account_id")
    private String accountId;
    @JsonProperty("room_id")
    private long roomId;

    public int getBadgeCount() {
        return badgeCount;
    }

    public void setBadgeCount(int badgeCount) {
        this.badgeCount = badgeCount;
    }

    public String getPushType() {
        return pushType;
    }

    public void setPushType(String pushType) {
        this.pushType = pushType;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }
}

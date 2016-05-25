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
        @JsonSubTypes.Type(value = FilePushInfo.class, name = "file_shared")})
public class BasePushInfo {
    public static final String MENTION_TO_ME = "member";
    public static final String MENTION_TO_ALL = "room";
    @JsonProperty("push_type")
    private String pushType;
    @JsonProperty("badge_count")
    private int badgeCount;
    @JsonProperty("account_id")
    private String accountId;
    @JsonProperty("room_id")
    private long roomId;
    @JsonProperty("is_ringing")
    private boolean isRingIng; // 타 플랫폼 active && 토픽 푸쉬 on
    private String mentioned; // 나(value = member) 혹은 전체(value = room) 멘션했는지
    @JsonProperty("device_subscribe")
    private boolean deviceSubscribe; // 디바이스의 푸쉬 알림 구독 여부
    @JsonProperty("device_push_preview")
    private int devicePushPreview; // 푸쉬 메시지 미리볼지 여부

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

    public boolean isRingIng() {
        return isRingIng;
    }

    public void setRingIng(boolean ringIng) {
        isRingIng = ringIng;
    }

    public String getMentioned() {
        return mentioned;
    }

    public void setMentioned(String mentioned) {
        this.mentioned = mentioned;
    }

    public boolean isDeviceSubscribe() {
        return deviceSubscribe;
    }

    public void setDeviceSubscribe(boolean deviceSubscribe) {
        this.deviceSubscribe = deviceSubscribe;
    }

    public int getDevicePushPreview() {
        return devicePushPreview;
    }

    public void setDevicePushPreview(int devicePushPreview) {
        this.devicePushPreview = devicePushPreview;
    }
}

package com.tosslab.jandi.app.network.models.sticker;

/**
 * Created by Steve SeongUg Jung on 15. 6. 3..
 */
public class SendSticker {
    private String stickerId;
    private int groupId;
    private int teamId;
    private int share;
    private String type;
    private String content;

    private SendSticker(String stickerId, int groupId, int teamId, int share, String type, String content) {
        this.stickerId = stickerId;
        this.groupId = groupId;
        this.teamId = teamId;
        this.share = share;
        this.type = type;
        this.content = content;
    }

    public static SendSticker create(String sitkcerId, int groupId, int teamId, int share, String type, String content) {
        return new SendSticker(sitkcerId, groupId, teamId, share, type, content);
    }

    public String getStickerId() {
        return stickerId;
    }

    public int getGroupId() {
        return groupId;
    }

    public int getTeamId() {
        return teamId;
    }

    public int getShare() {
        return share;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }
}

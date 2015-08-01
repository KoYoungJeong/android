package com.tosslab.jandi.app.network.models.sticker;

import com.tosslab.jandi.app.network.models.commonobject.MentionObject;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 6. 3..
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ReqSendSticker {
    private String stickerId;
    private int groupId;
    private int teamId;
    private int share;
    private String type;
    private String content;
    private List<MentionObject> mentions;

    private ReqSendSticker(int groupId, String stickerId, int teamId, int share, String type,
                           String content, List<MentionObject> mentions) {
        this.stickerId = stickerId;
        this.groupId = groupId;
        this.teamId = teamId;
        this.share = share;
        this.type = type;
        this.content = content;
        this.mentions = mentions;
    }

    public static ReqSendSticker create(int groupId, String sitkcerId, int teamId, int share,
                                        String type, String content, List<MentionObject> mentions) {

        return new ReqSendSticker(groupId, sitkcerId, teamId, share, type, content, mentions);

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

    public List<MentionObject> getMentions() {
        return mentions;
    }
}

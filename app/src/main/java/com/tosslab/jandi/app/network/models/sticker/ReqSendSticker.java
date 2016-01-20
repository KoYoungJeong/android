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
    private long groupId;
    private long teamId;
    private long share;
    private String type;
    private String content;
    private List<MentionObject> mentions;

    private ReqSendSticker(long groupId, String stickerId, long teamId, long share, String type,
                           String content, List<MentionObject> mentions) {
        this.stickerId = stickerId;
        this.groupId = groupId;
        this.teamId = teamId;
        this.share = share;
        this.type = type;
        this.content = content;
        this.mentions = mentions;
    }

    public static ReqSendSticker create(long groupId, String sitkcerId, long teamId, long share,
                                        String type, String content, List<MentionObject> mentions) {

        return new ReqSendSticker(groupId, sitkcerId, teamId, share, type, content, mentions);

    }

    public String getStickerId() {
        return stickerId;
    }

    public long getGroupId() {
        return groupId;
    }

    public long getTeamId() {
        return teamId;
    }

    public long getShare() {
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

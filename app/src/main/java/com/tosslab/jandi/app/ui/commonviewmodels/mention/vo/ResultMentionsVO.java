package com.tosslab.jandi.app.ui.commonviewmodels.mention.vo;

import com.tosslab.jandi.app.network.models.commonobject.MentionObject;

import java.util.List;

/**
 * Created by tee on 15. 8. 1..
 */
public class ResultMentionsVO {

    private String message;
    private List<MentionObject> mentions;

    public ResultMentionsVO(String message, List<MentionObject> mentions) {
        this.message = message;
        this.mentions = mentions;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<MentionObject> getMentions() {
        return mentions;
    }

    public void setMentions(List<MentionObject> mentions) {
        this.mentions = mentions;
    }

    @Override
    public String toString() {
        return "ResultMentionsVO{" +
                "message='" + message + '\'' +
                ", mentions=" + mentions.toString() +
                '}';
    }
}

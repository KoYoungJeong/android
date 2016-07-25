package com.tosslab.jandi.app.network.models.messages;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;

import java.util.List;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ReqTextMessage implements ReqMessage {
    private String text;
    private List<MentionObject> mentions;

    public ReqTextMessage() { }

    public ReqTextMessage(String text, List<MentionObject> mentions) {
        this.text = text;
        this.mentions = mentions;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<MentionObject> getMentions() {
        return mentions;
    }

    public void setMentions(List<MentionObject> mentions) {
        this.mentions = mentions;
    }
}

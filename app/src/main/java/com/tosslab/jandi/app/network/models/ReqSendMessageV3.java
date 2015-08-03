package com.tosslab.jandi.app.network.models;

import com.tosslab.jandi.app.network.models.commonobject.MentionObject;

import java.util.List;

/**
 * Created by tee on 15. 7. 27..
 */
public class ReqSendMessageV3 {

    private String content;

    private List<MentionObject> mentions;

    public ReqSendMessageV3(String content, List<MentionObject> mentions) {
        this.content = content;
        this.mentions = mentions;
    }

    public String getContent() {
        return content;
    }

    public List<MentionObject> getMentions() {
        return mentions;
    }

}

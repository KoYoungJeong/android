package com.tosslab.jandi.app.network.models;

import java.util.List;

/**
 * Created by tee on 15. 7. 27..
 */
public class ReqSendMessageV3 {

    private String content;

    private List<ReqMention> mentions;

    public ReqSendMessageV3(String content, List<ReqMention> mentions) {
        this.content = content;
        this.mentions = mentions;
    }

    public String getContent() {
        return content;
    }

    public List<ReqMention> getMentions() {
        return mentions;
    }

}

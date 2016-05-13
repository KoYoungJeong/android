package com.tosslab.jandi.app.push.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class CommentPushInfo extends BaseMessagePushInfo {
    @JsonProperty("message_content")
    private BaseMessagePushInfo.MessageContent messageContent;

    public CommentPushInfo() {
        setPushType("comment_created");
    }

    public MessageContent getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(MessageContent messageContent) {
        this.messageContent = messageContent;
    }
}

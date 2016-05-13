package com.tosslab.jandi.app.push.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class MessagePushInfo extends BaseMessagePushInfo {
    @JsonProperty("message_content")
    private MessageContent messageContent;

    public MessagePushInfo() {
        setPushType("message_created");
    }

    @Override
    public String toString() {
        return "MessagePushInfo{" +
                "messageContent=" + messageContent +
                "} " + super.toString();
    }

    public MessageContent getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(MessageContent messageContent) {
        this.messageContent = messageContent;
    }
}

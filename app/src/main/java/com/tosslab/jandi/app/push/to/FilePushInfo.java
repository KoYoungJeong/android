package com.tosslab.jandi.app.push.to;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tosslab.jandi.app.network.models.ResMessages;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class FilePushInfo extends BaseMessagePushInfo {
    @JsonProperty("message_content")
    private ResMessages.FileContent messageContent;

    public FilePushInfo() {
        setPushType("file_shared");
    }

    public ResMessages.FileContent getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(ResMessages.FileContent messageContent) {
        this.messageContent = messageContent;
    }
}

package com.tosslab.jandi.app.services.socket.to;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Created by Steve SeongUg Jung on 15. 4. 9..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class SocketChatCloseEvent {
    private String event;
    private ChatInfo chat;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public ChatInfo getChat() {
        return chat;
    }

    public void setChat(ChatInfo chat) {
        this.chat = chat;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class ChatInfo {
        private int id;
        private int companionId;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getCompanionId() {
            return companionId;
        }

        public void setCompanionId(int companionId) {
            this.companionId = companionId;
        }
    }
}

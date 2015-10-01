package com.tosslab.jandi.app.services.socket.to;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;

/**
 * Created by tonyjs on 15. 6. 17..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class SocketLinkPreviewMessageEvent {
    private int teamId;
    private String event;
    private String messageType;

    private MessageRoom room;
    private Writer writer;

    private Message message;

    public MessageRoom getRoom() {
        return room;
    }

    public void setRoom(MessageRoom room) {
        this.room = room;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public Writer getWriter() {
        return writer;
    }

    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "SocketLinkPreviewMessageEvent{" +
                "teamId='" + teamId + '\'' +
                ", event='" + event + '\'' +
                ", messageType='" + messageType + '\'' +
                ", room=" + room +
                ", writer=" + writer +
                ", message=" + message +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class MessageRoom {
        private int id;
        private String type;
        private List<Integer> members;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<Integer> getMembers() {
            return members;
        }

        public void setMembers(List<Integer> members) {
            this.members = members;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Message {
        private int id = -1;
        private String type;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isEmpty() {
            return id == -1;
        }

        @Override
        public String toString() {
            return "Message{" +
                    "id=" + id +
                    ", type='" + type + '\'' +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Writer {
        private int id;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "Writter{" +
                    "id=" + id +
                    '}';
        }
    }

}

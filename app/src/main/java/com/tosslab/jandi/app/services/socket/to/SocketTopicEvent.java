package com.tosslab.jandi.app.services.socket.to;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Created by Steve SeongUg Jung on 15. 4. 9..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class SocketTopicEvent {
    private String event;
    private int teamId;
    private TopicInfo topic;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public TopicInfo getTopic() {
        return topic;
    }

    public void setTopic(TopicInfo topic) {
        this.topic = topic;
    }

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class TopicInfo {
        private int id;
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

    }
}
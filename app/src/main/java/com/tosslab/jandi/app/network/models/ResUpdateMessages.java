package com.tosslab.jandi.app.network.models;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 19..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class ResUpdateMessages {
    public int lastLinkId;
    public UpdateInfo updateInfo;
    public Alarm alarm;
    public Event event;

    @Override
    public String toString() {
        return "ResUpdateMessages{" +
                "lastLinkId=" + lastLinkId +
                ", updateInfo=" + updateInfo +
                ", alarm=" + alarm +
                ", event=" + event +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    public static class UpdateInfo {
        public int messageCount;
        public List<ResMessages.Link> messages;

        @Override
        public String toString() {
            return "UpdateInfo{" +
                    "messageCount=" + messageCount +
                    ", messages=" + messages +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    public static class Alarm {
        public int alarmCount;
        public List<AlarmTable> alarmTable;

        @Override
        public String toString() {
            return "Alarm{" +
                    "alarmCount=" + alarmCount +
                    ", alarmTable=" + alarmTable +
                    '}';
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
        public static class AlarmTable {
            public int fromEntity;
            public List<Integer> toEntity;

            @Override
            public String toString() {
                return "AlarmTable{" +
                        "fromEntity=" + fromEntity +
                        ", toEntity=" + toEntity +
                        '}';
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    public static class Event {
        public int eventCount;
        public List<EventTable> eventTable;

        @Override
        public String toString() {
            return "Event{" +
                    "eventCount=" + eventCount +
                    ", eventTable=" + eventTable +
                    '}';
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
        public static class EventTable {
            public int id;
            public int fromEntity;
            public List<Integer> toEntity;
            public String status;

            @Override
            public String toString() {
                return "EventTable{" +
                        "id=" + id +
                        ", fromEntity=" + fromEntity +
                        ", toEntity=" + toEntity +
                        ", status='" + status + '\'' +
                        ", info=" + eventInfo +
                        '}';
            }

            public ResMessages.EventInfo eventInfo;
        }
    }
}

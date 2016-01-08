package com.tosslab.jandi.app.network.models;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResLeftSideMenu {
    public Team team;
    public User user;
    public int entityCount;
    public List<Entity> entities;
    public int joinEntityCount;
    public List<Entity> joinEntities;
    public int alarmInfoCount;
    public List<AlarmInfo> alarmInfos;
    public List<Bot> bots;

    @Override
    public String toString() {
        return "ResLeftSideMenu{" +
                "team=" + team +
                ", user=" + user +
                ", entityCount=" + entityCount +
                ", entities=" + entities +
                ", joinEntityCount=" + joinEntityCount +
                ", joinEntities=" + joinEntities +
                ", alarmInfoCount=" + alarmInfoCount +
                ", alarmInfos=" + alarmInfos +
                ", bots=" + bots +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    static public class Team {
        public int id;
        public String name;
        public String t_domain;
        public int t_defaultChannelId;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type",
            defaultImpl = User.class)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = Channel.class, name = "channel"),
            @JsonSubTypes.Type(value = PrivateGroup.class, name = "privateGroup"),
            @JsonSubTypes.Type(value = User.class, name = "user")})
    static public class Entity {
        public int id;
        public int teamId;
        public String type;
        public String name;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    static public class Channel extends Entity {
        public int ch_creatorId;
        public Date ch_createTime;
        public List<Integer> ch_members;
        public String description;
        public boolean autoJoin;

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    static public class User extends Entity {
        public String u_email;
        public String u_authority;
        public String u_nickname;
        public String u_tutoredAt;
        public String accountId;
        public Date updatedAt;
        public Date createdAt;
        public String u_statusMessage;
        public ExtraData u_extraData;
        public String u_photoUrl;
        public ResMessages.ThumbnailUrls u_photoThumbnailUrl;
        public List<MessageMarker> u_messageMarkers;
        public List<Integer> u_starredEntities;
        public List<Integer> u_joinEntities;
        public List<Integer> u_starredMessages;
        public String status;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    static public class PrivateGroup extends Entity {
        public int pg_creatorId;
        public Date pg_createTime;
        public List<Integer> pg_members;
        public String description;
        public boolean autoJoin;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    static public class MessageMarker {
        public String entityType;
        public int entityId;
        public int lastLinkId;
        public int alarmCount;
        public boolean announcementOpened;
        public boolean subscribe = true;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    static public class ExtraData {
        public String phoneNumber;
        public String department;
        public String position;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class AlarmInfo {
        public String entityType;
        public int entityId;
        public int lastLinkId;
        public int alarmCount;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Bot {
        public int id;
        public int teamId;
        public String botType;
        public String name;
        public Date updatedAt;
        public Date createdAt;
        public String thumbnailUrl;
        public String status;
        public String type;
    }
}

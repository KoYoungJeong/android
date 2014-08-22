package com.tosslab.jandi.app.network.models;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.Date;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 6. 17..
 * 서버의 Rest 통신으로부터 왼쪽의 메뉴에 위치할 Channel, PrivateGroup, Users 의 목록을 받아온 결과
 * 사용자가 Join 된 Channel, PrivateGroup도 따로 가져온다.
 * Entity는 각각의 서브 클래스로 나뉘어진다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class ResLeftSideMenu {
    public Team team;
    public User user;
    public int entityCount;
    public List<Entity> entities;
    public int joinEntityCount;
    public List<Entity> joinEntities;

    @JsonIgnoreProperties(ignoreUnknown = true)
    static public class Team {
        public int id;
        public String name;
        public String t_domain;
        public int t_defaultChannelId;
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type")
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

    static public class Channel extends Entity {
        public int ch_creatorId;
        public Date ch_createTime;
        public List<Integer> ch_members;
    }

    static public class User extends Entity {
        public String u_email;
        public String u_authority;
        public String u_photoUrl;
        public String u_firstName;
        public String u_lastName;
        public List<MessageMarker> u_messageMarkers;
    }

    static public class PrivateGroup extends Entity {
        public int pg_creatorId;
        public Date pg_createTime;
        public List<Integer> pg_members;
    }

    static public class MessageMarker {
        public String entityType;
        public int entityId;
        public int lastLinkId;
        public int alarmCount;
    }
}

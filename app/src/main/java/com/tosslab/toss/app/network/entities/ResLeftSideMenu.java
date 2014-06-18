package com.tosslab.toss.app.network.entities;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonSerialize;

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
    public int entityCount;
    public List<Entity> entities;
    public int joinEntityCount;
    public List<Entity> joinEntity;

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
        public String type;
        public String name;
    }

    static public class Channel extends Entity {

    }

    static public class User extends Entity {
        public String u_photoUrl;
        public String u_firstName;
        public String u_lastName;
    }

    static public class PrivateGroup extends Entity {

    }

}

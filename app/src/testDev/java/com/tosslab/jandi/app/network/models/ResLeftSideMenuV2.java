package com.tosslab.jandi.app.network.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

@DatabaseTable(tableName = "entity_left_side_menu")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResLeftSideMenuV2 {
    @DatabaseField(generatedId = true, readOnly = true)
    @JsonIgnore
    public long _id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    @JsonIgnore
    public ResAccountInfo.UserTeam userTeam;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    public Team team;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    public User user;

    @DatabaseField
    public int entityCount;

    @ForeignCollectionField
    public Collection<Entity> entities;
    @ForeignCollectionField
    public Collection<Entity> joinEntities;
    @ForeignCollectionField
    public Collection<AlarmInfo> alarmInfos;

    @DatabaseField
    public int joinEntityCount;
    @DatabaseField
    public int alarmInfoCount;

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
                '}';
    }

    @DatabaseTable(tableName = "entity_team")
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    static public class Team {
        @DatabaseField(id = true, readOnly = true)
        public int id;
        @DatabaseField
        public String name;
        @DatabaseField
        public String t_domain;
        @DatabaseField
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
        @DatabaseField(foreign = true)
        public ResLeftSideMenuV2 leftSideMenu;
        @DatabaseField(id = true, readOnly = true)
        public int id;
        @DatabaseField
        public int teamId;
        public String type;
        @DatabaseField
        public String name;
    }

    @DatabaseTable(tableName = "entity_public_topic")
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    static public class Channel extends Entity {
        @DatabaseField
        public int ch_creatorId;
        @DatabaseField
        public Date ch_createTime;
        @ForeignCollectionField
        public Collection<EntityRef> ch_members;
        @DatabaseField
        public String description;
    }

    @DatabaseTable(tableName = "entity_user")
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    static public class User extends Entity {
        @DatabaseField
        public String u_email;
        @DatabaseField
        public String u_authority;
        @DatabaseField
        public String u_nickname;
        @DatabaseField
        public String u_tutoredAt;
        @DatabaseField
        public String accountId;
        @DatabaseField
        public Date updatedAt;
        @DatabaseField
        public Date createdAt;
        @DatabaseField
        public String u_statusMessage;
        @DatabaseField(foreign = true, foreignAutoRefresh = true)
        public ExtraData u_extraData;
        @DatabaseField
        public String u_photoUrl;
        @DatabaseField(foreign = true, foreignAutoRefresh = true)
        public UserThumbNailInfo u_photoThumbnailUrl;
        @ForeignCollectionField
        public Collection<MessageMarker> u_messageMarkers;
        @ForeignCollectionField
        public Collection<EntityRef> u_starredEntities;
        @ForeignCollectionField
        public Collection<EntityRef> u_joinEntities;
        @ForeignCollectionField
        public Collection<EntityRef> u_starredMessages;
        @DatabaseField
        public String status;
    }

    @DatabaseTable(tableName = "entity_private_topic")
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    static public class PrivateGroup extends Entity {
        @DatabaseField
        public int pg_creatorId;
        @DatabaseField
        public Date pg_createTime;
        @ForeignCollectionField
        public Collection<EntityRef> pg_members;
        @DatabaseField
        public String description;

    }

    @DatabaseTable(tableName = "entity_message_marker")
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    static public class MessageMarker {

        @DatabaseField(foreign = true)
        public User user;
        @DatabaseField
        public String entityType;
        @DatabaseField(id = true, readOnly = true)
        public int entityId;
        @DatabaseField
        public int lastLinkId;
        @DatabaseField
        public int alarmCount;
        @DatabaseField
        public boolean announcementOpened;
    }

    @DatabaseTable(tableName = "entity_member_extra_data")
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    static public class ExtraData {
        @DatabaseField(generatedId = true, readOnly = true)
        public long _id;

        @DatabaseField
        public String phoneNumber;
        @DatabaseField
        public String department;
        @DatabaseField
        public String position;
    }

    @DatabaseTable(tableName = "entity_alarm_info")
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class AlarmInfo {
        @DatabaseField(generatedId = true, readOnly = true)
        public long _id;

        @DatabaseField(foreign = true)
        public ResLeftSideMenuV2 leftSideMenu;
        @DatabaseField
        public String entityType;
        @DatabaseField
        public int entityId;
        @DatabaseField
        public int lastLinkId;
        @DatabaseField
        public int alarmCount;

    }

    @DatabaseTable(tableName = "entity_user_thumbnail")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UserThumbNailInfo {
        @DatabaseField(generatedId = true, readOnly = true)
        public long _id;

        @DatabaseField
        public String smallThumbnailUrl;
        @DatabaseField
        public String mediumThumbnailUrl;
        @DatabaseField
        public String largeThumbnailUrl;

        @Override
        public String toString() {
            return "ThumbnailUrls{" +
                    "smallThumbnailUrl='" + smallThumbnailUrl + '\'' +
                    ", mediumThumbnailUrl='" + mediumThumbnailUrl + '\'' +
                    ", largeThumbnailUrl='" + largeThumbnailUrl + '\'' +
                    '}';
        }
    }

    @DatabaseTable(tableName = "entity_ref_info")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonDeserialize(using = EntityRefDeserializeV2.class)
    public static class EntityRef {
        @DatabaseField(foreign = true, foreignColumnName = "id")
        public Channel channel;
        @DatabaseField(foreign = true, foreignColumnName = "id")
        public User user;
        @DatabaseField(foreign = true, foreignColumnName = "id")
        public PrivateGroup privateGroup;

        @DatabaseField(generatedId = true)
        public long _id;

        @DatabaseField
        public int value;

    }

    private static class EntityRefDeserializeV2 extends JsonDeserializer<EntityRef> {
        @Override
        public EntityRef deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            ObjectMapper mapper = (ObjectMapper) jp.getCodec();
            JsonNode root = mapper.readTree(jp);

            ResLeftSideMenuV2.EntityRef entityRef = new ResLeftSideMenuV2.EntityRef();
            entityRef.value = root.getIntValue();

            return entityRef;
        }
    }

}

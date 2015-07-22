package com.tosslab.jandi.app.network.models;

import android.text.TextUtils;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.tosslab.jandi.app.local.orm.dao.LinkDaoImpl;
import com.tosslab.jandi.app.network.jackson.deserialize.message.EventInfoDeserialize;
import com.tosslab.jandi.app.network.jackson.deserialize.message.InviteInfoDeserializer;
import com.tosslab.jandi.app.network.jackson.deserialize.message.TopicCreateInfoDeserializer;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by justinygchoi on 2014. 6. 18..
 * CDP 메시지 리스트 획득의 응답
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResMessages {
    public int entityId;
    public int lastLinkId;
    public int firstLinkId;
    public List<Link> records;

    @Override
    public String toString() {
        return "ResMessages{" +
                "lastLinkId=" + lastLinkId +
                ", firstLinkId=" + firstLinkId +
                ", records=" + records +
                '}';
    }

    public enum EventType {
        CREATE, JOIN, INVITE, LEAVE, ANNOUNCE_CREATE, ANNOUNCE_UPDATE, ANNOUNCE_DELETE, NONE
    }

    @DatabaseTable(tableName = "message_link", daoClass = LinkDaoImpl.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Link {
        @DatabaseField(id = true)
        public int id;
        @DatabaseField
        public int teamId;
        @DatabaseField
        public int fromEntity;
        @DatabaseField
        public Date time;
        @DatabaseField
        public int messageId;
        @DatabaseField
        public String status;
        @DatabaseField
        public int feedbackId;

        @DatabaseField(foreign = true)
        public EventInfo info; // How to convert other type
        @DatabaseField
        public String eventType;
        public OriginalMessage feedback;
        public OriginalMessage message;

        public boolean hasLinkPreview() {
            boolean isTextMessage = message != null && message instanceof TextMessage;
            if (!isTextMessage) {
                return false;
            }
            TextMessage textMessage = (TextMessage) message;
            LinkPreview linkPreview = textMessage.linkPreview;
            return linkPreview != null && !linkPreview.isEmpty();
        }

        @Override
        public String toString() {
            return "Link{" +
                    "id=" + id +
                    ", teamId=" + teamId +
                    ", fromEntity=" + fromEntity +
                    ", time=" + time +
                    ", messageId=" + messageId +
                    ", status='" + status + '\'' +
                    ", feedbackId=" + feedbackId +
                    ", info=" + info +
                    ", feedback=" + feedback +
                    ", message=" + message +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonDeserialize(using = EventInfoDeserialize.class)
    public static class EventInfo {
        @DatabaseField(generatedId = true)
        public long _id;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "contentType")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = TextMessage.class, name = "text"),
            @JsonSubTypes.Type(value = FileMessage.class, name = "file"),
            @JsonSubTypes.Type(value = StickerMessage.class, name = "sticker"),
            @JsonSubTypes.Type(value = CommentStickerMessage.class, name = "comment_sticker"),
            @JsonSubTypes.Type(value = CommentMessage.class, name = "comment")})
    public static class OriginalMessage {
        public int id;
        public int teamId;
        public int writerId;
        public Date createTime;
        public Date updateTime;
        public String contentType;
        public String status;
        public Collection<Integer> shareEntities;
        public int permission;
        public int feedbackId;
        public FileMessage feedback;
        public String linkPreviewId;

        @Override
        public String toString() {
            return "OriginalMessage{" +
                    "id=" + id +
                    ", teamId=" + teamId +
                    ", writerId=" + writerId +
                    ", createTime=" + createTime +
                    ", updateTime=" + updateTime +
                    ", contentType='" + contentType + '\'' +
                    ", status='" + status + '\'' +
                    ", shareEntities=" + shareEntities +
                    ", permission=" + permission +
                    ", feedbackId=" + feedbackId +
                    ", feedback=" + feedback +
                    ", linkPreviewId=" + linkPreviewId +
                    '}';
        }
    }

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TextMessage extends OriginalMessage {
        public TextContent content;
        public LinkPreview linkPreview;

        @Override
        public String toString() {
            return "TextMessage{" +
                    "id=" + id +
                    ", teamId=" + teamId +
                    ", writerId=" + writerId +
                    ", createTime=" + createTime +
                    ", updateTime=" + updateTime +
                    ", contentType='" + contentType + '\'' +
                    ", status='" + status + '\'' +
                    ", shareEntities=" + shareEntities +
                    ", permission=" + permission +
                    ", feedbackId=" + feedbackId +
                    ", feedback=" + feedback +
                    ", linkPreviewId=" + linkPreviewId +
                    ". content=" + content +
                    ", linkPreview=" + linkPreview +
                    '}';
        }
    }

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommentMessage extends OriginalMessage {
        public TextContent content;
    }

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FileMessage extends OriginalMessage {
        public FileContent content;
        public int commentCount;
    }

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TextContent {
        public String body;

        @Override
        public String toString() {
            return "TextContent{" +
                    "body='" + body + '\'' +
                    '}';
        }
    }

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StickerMessage extends OriginalMessage {
        public StickerContent content;
        public int version;
    }

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommentStickerMessage extends OriginalMessage {
        public StickerContent content;
        public int version;
    }

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FileContent {
        public String title;
        public String name;
        public String type;
        public String icon;
        public String serverUrl;
        public String filterType;
        public String fileUrl;
        public String ext;
        public int size;
        public ThumbnailUrls extraInfo;

        @Override
        public String toString() {
            return "FileContent{" +
                    "title='" + title + '\'' +
                    ", name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", icon='" + icon + '\'' +
                    ", serverUrl='" + serverUrl + '\'' +
                    ", filterType='" + filterType + '\'' +
                    ", fileUrl='" + fileUrl + '\'' +
                    ", ext='" + ext + '\'' +
                    ", size=" + size +
                    ", extraInfo=" + extraInfo +
                    '}';
        }
    }

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ThumbnailUrls {
        public String smallThumbnailUrl;
        public String mediumThumbnailUrl;
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

    @DatabaseTable(tableName = "message_info_create")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonDeserialize(using = JsonDeserializer.None.class)
    public static class CreateEvent extends EventInfo {


        @JsonTypeInfo(
                use = JsonTypeInfo.Id.NAME,
                include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
                property = "entityType")
        @JsonSubTypes({
                @JsonSubTypes.Type(value = PublicCreateInfo.class, name = "channel"),
                @JsonSubTypes.Type(value = PrivateCreateInfo.class, name = "privateGroup")})
        @DatabaseField(foreign = true)
        public CreateInfo createInfo;
        @DatabaseField(dataType = DataType.LONG_STRING)
        public String createType;
    }

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonDeserialize(using = JsonDeserializer.None.class)
    @DatabaseTable(tableName = "message_info_announce_create")
    public static class AnnouncementCreateEvent extends EventInfo {

        @DatabaseField(foreign = true, foreignAutoRefresh = true)
        private Info eventInfo;

        public Info getEventInfo() {
            return eventInfo;
        }

        public void setEventInfo(Info eventInfo) {
            this.eventInfo = eventInfo;
        }

        @Override
        public String toString() {
            return "AnnouncementEvent{" +
                    ", eventInfo=" + eventInfo +
                    '}';
        }

        @DatabaseTable(tableName = "message_info_annouce_create_writer")
        public static class Info {

            @DatabaseField(generatedId = true)
            public int _id;

            @DatabaseField
            private int writerId;

            public int getWriterId() {
                return writerId;
            }

            public void setWriterId(int writerId) {
                this.writerId = writerId;
            }

            @Override
            public String toString() {
                return "Info{" +
                        "writerId=" + writerId +
                        '}';
            }
        }
    }

    @DatabaseTable(tableName = "message_info_announce_update")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonDeserialize(using = JsonDeserializer.None.class)
    public static class AnnouncementUpdateEvent extends EventInfo {


        @DatabaseField(foreign = true, foreignAutoRefresh = true)
        private Info eventInfo;

        public Info getEventInfo() {
            return eventInfo;
        }

        public void setEventInfo(Info eventInfo) {
            this.eventInfo = eventInfo;
        }

        @Override
        public String toString() {
            return "AnnouncementEvent{" +
                    ", eventInfo=" + eventInfo +
                    '}';
        }

        @DatabaseTable(tableName = "message_info_announce_update_writer")
        public static class Info {

            @DatabaseField(generatedId = true)
            public long _id;

            @DatabaseField
            private int writerId;

            public int getWriterId() {
                return writerId;
            }

            public void setWriterId(int writerId) {
                this.writerId = writerId;
            }

            @Override
            public String toString() {
                return "Info{" +
                        "writerId=" + writerId +
                        '}';
            }
        }
    }

    @DatabaseTable(tableName = "message_info_announce_delete")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonDeserialize(using = JsonDeserializer.None.class)
    public static class AnnouncementDeleteEvent extends EventInfo {

    }

    @DatabaseTable(tableName = "message_info_invite")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonDeserialize(using = JsonDeserializer.None.class)
    public static class InviteEvent extends EventInfo {


        @DatabaseField
        public int invitorId;
        @ForeignCollectionField
        public Collection<IntegerWrapper> inviteUsers;

        @DatabaseTable(tableName = "message_info_invite_user")
        @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonDeserialize(using = InviteInfoDeserializer.class)
        public static class IntegerWrapper {

            @DatabaseField(generatedId = true)
            private long _id;
            @DatabaseField(foreign = true, foreignAutoRefresh = true)
            private InviteEvent inviteEvent;
            @DatabaseField
            private int inviteUserId;

            public long get_id() {
                return _id;
            }

            public void set_id(long _id) {
                this._id = _id;
            }

            public int getInviteUserId() {
                return inviteUserId;
            }

            public void setInviteUserId(int inviteUserId) {
                this.inviteUserId = inviteUserId;
            }

            public InviteEvent getInviteEvent() {
                return inviteEvent;
            }

            public void setInviteEvent(InviteEvent inviteEvent) {
                this.inviteEvent = inviteEvent;
            }
        }
    }

    @DatabaseTable(tableName = "message_info_leave")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonDeserialize(using = JsonDeserializer.None.class)
    public static class LeaveEvent extends EventInfo {


    }

    @DatabaseTable(tableName = "message_info_join")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonDeserialize(using = JsonDeserializer.None.class)
    public static class JoinEvent extends EventInfo {

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class CreateInfo {
        @DatabaseField(generatedId = true)
        public int _id;
    }

    @DatabaseTable(tableName = "message_info_create_topic")
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class PublicCreateInfo extends CreateInfo {

        @DatabaseField
        @JsonProperty("ch_creatorId")
        public int creatorId;
        @DatabaseField
        @JsonProperty("ch_createTime")
        public Date createTime;
        @DatabaseField
        @JsonProperty("ch_isDefault")
        public boolean isDefault;
        @JsonProperty("ch_members")
        @ForeignCollectionField
        public Collection<IntegerWrapper> members;

        @DatabaseTable(tableName = "message_info_create_topic_member")
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
        @JsonDeserialize(using = TopicCreateInfoDeserializer.class)
        public static class IntegerWrapper {
            @DatabaseField(generatedId = true)
            private long _id;
            @DatabaseField(foreign = true, foreignAutoRefresh = true)
            private PublicCreateInfo createInfo;
            @DatabaseField
            private int memberId;

            public long get_id() {
                return _id;
            }

            public void set_id(long _id) {
                this._id = _id;
            }

            public int getMemberId() {
                return memberId;
            }

            public void setMemberId(int memberId) {
                this.memberId = memberId;
            }

            public PublicCreateInfo getCreateInfo() {
                return createInfo;
            }

            public void setCreateInfo(PublicCreateInfo createInfo) {
                this.createInfo = createInfo;
            }
        }
    }

    @DatabaseTable(tableName = "message_info_create_topic")
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class PrivateCreateInfo extends CreateInfo {

        @DatabaseField
        @JsonProperty("pg_creatorId")
        public int creatorId;
        @DatabaseField
        @JsonProperty("pg_createTime")
        public Date createTime;
        @DatabaseField
        @JsonProperty("pg_isDefault")
        public boolean isDefault;
        @JsonProperty("pg_members")
        @ForeignCollectionField
        public Collection<IntegerWrapper> members;

        @DatabaseTable(tableName = "message_info_create_topic_member")
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
        @JsonDeserialize(using = TopicCreateInfoDeserializer.class)
        public static class IntegerWrapper {
            @DatabaseField(generatedId = true)
            private long _id;
            @DatabaseField(foreign = true, foreignAutoRefresh = true)
            private PrivateCreateInfo createInfo;
            @DatabaseField
            private int memberId;

            public long get_id() {
                return _id;
            }

            public void set_id(long _id) {
                this._id = _id;
            }

            public int getMemberId() {
                return memberId;
            }

            public void setMemberId(int memberId) {
                this.memberId = memberId;
            }

            public PrivateCreateInfo getCreateInfo() {
                return createInfo;
            }

            public void setCreateInfo(PrivateCreateInfo createInfo) {
                this.createInfo = createInfo;
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class StickerContent {
        public int groupId;
        public String stickerId;
        public String url;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class LinkPreview {
        public String linkUrl;
        public String description;
        public String title;
        public String imageUrl;
        public String domain;

        public boolean isEmpty() {
            return TextUtils.isEmpty(linkUrl);
        }

        @Override
        public String toString() {
            return "LinkPreview{" +
                    ", linkUrl='" + linkUrl + '\'' +
                    ", description='" + description + '\'' +
                    ", title='" + title + '\'' +
                    ", imageUrl='" + imageUrl + '\'' +
                    ", domain='" + domain + '\'' +
                    '}';
        }
    }

}

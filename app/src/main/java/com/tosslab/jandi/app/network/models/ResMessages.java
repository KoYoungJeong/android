package com.tosslab.jandi.app.network.models;

import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.tosslab.jandi.app.local.orm.dao.FileMessageDaoImpl;
import com.tosslab.jandi.app.local.orm.dao.LinkDaoImpl;
import com.tosslab.jandi.app.local.orm.dao.PollMessageDaoImpl;
import com.tosslab.jandi.app.local.orm.dao.TextMessageDaoImpl;
import com.tosslab.jandi.app.local.orm.dao.event.AnnounceCreateEventDaoImpl;
import com.tosslab.jandi.app.local.orm.dao.event.CreateEventDaoImpl;
import com.tosslab.jandi.app.local.orm.dao.event.InviteEventDaoImpl;
import com.tosslab.jandi.app.local.orm.dao.event.PrivateCreateInfoDaoImpl;
import com.tosslab.jandi.app.local.orm.dao.event.PublicCreateInfoDaoImpl;
import com.tosslab.jandi.app.local.orm.persister.CollectionLongConverter;
import com.tosslab.jandi.app.local.orm.persister.DateConverter;
import com.tosslab.jandi.app.local.orm.persister.FormatMessageConverter;
import com.tosslab.jandi.app.network.jackson.deserialize.message.CommentMessageConverter;
import com.tosslab.jandi.app.network.jackson.deserialize.message.EventInfoDeserialize;
import com.tosslab.jandi.app.network.jackson.deserialize.message.InviteInfoDeserializer;
import com.tosslab.jandi.app.network.jackson.deserialize.message.LinkShareEntityDeserializer;
import com.tosslab.jandi.app.network.jackson.deserialize.message.PrivateTopicCreateInfoDeserializer;
import com.tosslab.jandi.app.network.jackson.deserialize.message.PublicTopicCreateInfoDeserializer;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.network.models.dynamicl10n.FormatParam;
import com.tosslab.jandi.app.network.models.poll.Poll;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by justinygchoi on 2014. 6. 18..
 * CDP 메시지 리스트 획득의 응답
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResMessages {
    public long entityId;
    public long lastLinkId;
    public long firstLinkId;
    public List<Link> records;

    @Override
    public String toString() {
        return "ResMessages{" +
                "entityId=" + entityId +
                ", lastLinkId=" + lastLinkId +
                ", firstLinkId=" + firstLinkId +
                ", records=" + records +
                '}';
    }

    public enum EventType {
        CREATE, JOIN, INVITE, LEAVE, ANNOUNCE_CREATE, ANNOUNCE_UPDATE, ANNOUNCE_DELETE, NONE
    }

    public enum MessageType {
        TEXT, FILE, COMMENT, STICKER, COMMENT_STICKER, POLL, NONE
    }

    public enum FeedbackType {
        FILE("file"), POLL("poll");

        String value;

        FeedbackType(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    public interface Commentable {
        int getCommentCount();
    }

    @DatabaseTable(tableName = "message_link", daoClass = LinkDaoImpl.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Link {
        @DatabaseField(id = true)
        public long id;
        @DatabaseField
        public long teamId;
        @DatabaseField
        public long fromEntity;
        @DatabaseField(persisterClass = DateConverter.class)
        public Date time;
        @DatabaseField
        public long messageId;
        @DatabaseField
        public String status;
        @DatabaseField
        public long feedbackId;
        @DatabaseField(foreign = true)
        public EventInfo info; // How to convert other type
        @DatabaseField
        public String eventType;
        @DatabaseField
        public String feedbackType;
        @DatabaseField(foreign = true)
        public OriginalMessage feedback;
        @DatabaseField(foreign = true)
        public OriginalMessage message;
        @DatabaseField
        public String messageType;
        @DatabaseField(persisterClass = CollectionLongConverter.class)
        public List<Long> toEntity;

        @DatabaseField
        public long pollId;
        @DatabaseField(foreign = true, foreignAutoRefresh = true)
        public Poll poll;

        public long unreadCnt;

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
                    ", eventType='" + eventType + '\'' +
                    ", feedbackType='" + feedbackType + '\'' +
                    ", feedback=" + feedback +
                    ", message=" + message +
                    ", messageType='" + messageType + '\'' +
                    ", toEntity=" + toEntity +
                    ", pollId=" + pollId +
                    ", poll=" + poll +
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
            property = "contentType",
            defaultImpl = OriginalMessage.class)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = TextMessage.class, name = "text"),
            @JsonSubTypes.Type(value = FileMessage.class, name = "file"),
            @JsonSubTypes.Type(value = PollMessage.class, name = "poll"),
            @JsonSubTypes.Type(value = StickerMessage.class, name = "sticker"),
            @JsonSubTypes.Type(value = CommentStickerMessage.class, name = "comment_sticker"),
            @JsonSubTypes.Type(value = CommentMessage.class, name = "comment")})
    public static class OriginalMessage {
        @DatabaseField(id = true)
        public long id;
        @DatabaseField
        public long teamId;
        @DatabaseField
        public long writerId;
        @DatabaseField(persisterClass = DateConverter.class)
        public Date createTime;
        @DatabaseField(persisterClass = DateConverter.class)
        public Date updateTime;
        @DatabaseField
        public String contentType;
        @DatabaseField
        public String status;
        @DatabaseField
        public int permission;
        @DatabaseField
        public long feedbackId;
        @DatabaseField
        public String linkPreviewId;
        @DatabaseField
        public boolean isStarred;

        @DatabaseField
        public boolean isFormatted;
        @DatabaseField
        public String formatKey;
        public Map formatParams;
        @JsonIgnore
        @DatabaseField(persisterClass = FormatMessageConverter.class)
        public FormatParam formatMessage;

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
                    ", permission=" + permission +
                    ", feedbackId=" + feedbackId +
                    ", linkPreviewId=" + linkPreviewId +
                    ", isStarred=" + isStarred +
                    '}';
        }

        @DatabaseTable(tableName = "message_shareentity")
        @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonDeserialize(using = LinkShareEntityDeserializer.class)
        public static class IntegerWrapper {
            @DatabaseField(generatedId = true)
            private long _id;
            @DatabaseField
            private long shareEntity;

            @DatabaseField(foreign = true)
            private TextMessage textOf;
            @DatabaseField(foreign = true)
            private StickerMessage stickerOf;
            @DatabaseField(foreign = true)
            private CommentMessage commentOf;
            @DatabaseField(foreign = true)
            private CommentStickerMessage commentStickerOf;
            @DatabaseField(foreign = true)
            private FileMessage fileOf;

            public long get_id() {
                return _id;
            }

            public void set_id(long _id) {
                this._id = _id;
            }

            public long getShareEntity() {
                return shareEntity;
            }

            public void setShareEntity(long shareEntity) {
                this.shareEntity = shareEntity;
            }


            public void setStickerOf(StickerMessage stickerOf) {
                this.stickerOf = stickerOf;
            }

            public void setCommentOf(CommentMessage commentOf) {
                this.commentOf = commentOf;
            }

            public void setCommentStickerOf(CommentStickerMessage commentStickerOf) {
                this.commentStickerOf = commentStickerOf;
            }

            public void setFileOf(FileMessage fileOf) {
                this.fileOf = fileOf;
            }

            public void setTextOf(TextMessage textOf) {
                this.textOf = textOf;
            }
        }
    }

    @DatabaseTable(tableName = "message_text", daoClass = TextMessageDaoImpl.class)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TextMessage extends OriginalMessage {

        @ForeignCollectionField(foreignFieldName = "textOf")
        public Collection<IntegerWrapper> shareEntities;

        @ForeignCollectionField(foreignFieldName = "textOf")
        public Collection<MentionObject> mentions;


        @DatabaseField(foreign = true, foreignAutoRefresh = true)
        public TextContent content;
        @DatabaseField(foreign = true, foreignAutoRefresh = true)
        public LinkPreview linkPreview;

        public TextMessage() {
            contentType = "text";
        }

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
                    ", linkPreviewId=" + linkPreviewId +
                    ". content=" + content +
                    ", linkPreview=" + linkPreview +
                    '}';
        }
    }

    @DatabaseTable(tableName = "messagec_comment")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonDeserialize(converter = CommentMessageConverter.class)
    public static class CommentMessage extends OriginalMessage {
        @ForeignCollectionField(foreignFieldName = "commentOf")
        public Collection<IntegerWrapper> shareEntities;

        @ForeignCollectionField(foreignFieldName = "commentOf")
        public Collection<MentionObject> mentions;

        @DatabaseField(foreign = true, foreignAutoRefresh = true)
        public TextContent content;
        @DatabaseField
        public long pollId;

        public CommentMessage() {
            contentType = "comment";
        }
    }

    @DatabaseTable(tableName = "message_file", daoClass = FileMessageDaoImpl.class)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FileMessage extends OriginalMessage implements Commentable {
        @ForeignCollectionField(foreignFieldName = "fileOf")
        public Collection<IntegerWrapper> shareEntities;

        @DatabaseField(foreign = true, foreignAutoRefresh = true)
        public FileContent content;
        @DatabaseField
        public int commentCount;

        public FileMessage() {
            contentType = "file";
        }

        @Override
        public String toString() {
            return "FileMessage{" +
                    super.toString() +
                    ", shareEntities=" + shareEntities +
                    ", content=" + content +
                    ", commentCount=" + commentCount +
                    '}';
        }

        @Override
        public int getCommentCount() {
            return commentCount;
        }
    }

    @DatabaseTable(tableName = "message_text_content")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TextContent {
        @DatabaseField(generatedId = true)
        public long _id;

        @DatabaseField(foreign = true)
        @JsonIgnore
        public TextMessage textMessage;

        @DatabaseField
        public String body;

        @JsonIgnore
        public SpannableStringBuilder contentBuilder;

        @DatabaseField
        public String connectType;
        @DatabaseField
        public String connectColor;
        @ForeignCollectionField(foreignFieldName = "textContentOf")
        public Collection<ConnectInfo> connectInfo;

        @Override
        public String toString() {
            return "TextContent{" +
                    "body='" + body + '\'' +
                    '}';
        }
    }

    @DatabaseTable(tableName = "message_poll_content")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PollContent {
        @DatabaseField(generatedId = true)
        public long _id;

        @DatabaseField(foreign = true)
        @JsonIgnore
        public PollMessage pollMessage;

        @DatabaseField
        public String body;

        @JsonIgnore
        public SpannableStringBuilder contentBuilder;

        @DatabaseField
        public String connectType;
        @DatabaseField
        public String connectColor;
        @ForeignCollectionField(foreignFieldName = "pollContentOf")
        public Collection<PollConnectInfo> connectInfo;

        @Override
        public String toString() {
            return "PollContent{" +
                    "_id=" + _id +
                    ", body='" + body + '\'' +
                    '}';
        }
    }

    @DatabaseTable(tableName = "message_text_content_connectInfo")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ConnectInfo {
        @DatabaseField(generatedId = true)
        public long _id;
        @DatabaseField(foreign = true)
        @JsonIgnore
        public TextContent textContentOf;
        @DatabaseField
        public String event;
        @DatabaseField
        public String title;
        @DatabaseField
        public String imageUrl;
        @DatabaseField
        public String description;

        @Override
        public String toString() {
            return "ConnectInfo{" +
                    "_id=" + _id +
                    ", textContentOf=" + textContentOf +
                    ", event='" + event + '\'' +
                    ", title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }

    @DatabaseTable(tableName = "message_poll_content_connectInfo")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PollConnectInfo {
        @DatabaseField(generatedId = true)
        public long _id;
        @DatabaseField(foreign = true)
        @JsonIgnore
        public PollContent pollContentOf;
        @DatabaseField
        public String event;
        @DatabaseField
        public String title;
        @DatabaseField
        public String imageUrl;
        @DatabaseField
        public String description;

        @Override
        public String toString() {
            return "PollConnectInfo{" +
                    "_id=" + _id +
                    ", pollContentOf=" + pollContentOf +
                    ", event='" + event + '\'' +
                    ", title='" + title + '\'' +
                    ", imageUrl='" + imageUrl + '\'' +
                    ", description='" + description + '\'' +
                    '}';
        }
    }

    @DatabaseTable(tableName = "message_sticker")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StickerMessage extends OriginalMessage {
        @ForeignCollectionField(foreignFieldName = "stickerOf")
        public Collection<IntegerWrapper> shareEntities;

        @DatabaseField(foreign = true, foreignAutoRefresh = true)
        public StickerContent content;
        @DatabaseField
        public int version;

        public StickerMessage() {
            contentType = "sticker";
        }
    }

    @DatabaseTable(tableName = "message_poll", daoClass = PollMessageDaoImpl.class)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PollMessage extends OriginalMessage implements Commentable {
        @DatabaseField(foreign = true, foreignAutoRefresh = true)
        public PollContent content;

        @DatabaseField(persisterClass = CollectionLongConverter.class)
        public Collection<Long> shareEntities;

        @DatabaseField
        public long pollId;

        @DatabaseField
        public int commentCount;

        public PollMessage() {
            contentType = "poll";
        }

        @Override
        public int getCommentCount() {
            return commentCount;
        }

        @Override
        public String toString() {
            return "PollMessage{" +
                    "content=" + content +
                    ", shareEntities=" + shareEntities +
                    ", pollId=" + pollId +
                    ", commentCount=" + commentCount +
                    '}';
        }
    }

    @DatabaseTable(tableName = "message_commentsticker")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommentStickerMessage extends OriginalMessage {
        @ForeignCollectionField(foreignFieldName = "commentStickerOf")
        public Collection<IntegerWrapper> shareEntities;

        @DatabaseField(foreign = true, foreignAutoRefresh = true)
        public StickerContent content;
        @DatabaseField
        public int version;

        @DatabaseField
        public long pollId;

        public CommentStickerMessage() {
            contentType = "comment_sticker";
        }

        @Override
        public String toString() {
            return "CommentStickerMessage{" +
                    "shareEntities=" + shareEntities +
                    ", content=" + content +
                    ", version=" + version +
                    ", pollId=" + pollId +
                    '}';
        }
    }

    @DatabaseTable(tableName = "message_file_content")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FileContent {
        @DatabaseField
        public String title;
        @DatabaseField
        public String name;
        @DatabaseField
        public String type;
        @DatabaseField
        public String icon;
        @DatabaseField
        public String serverUrl;
        @DatabaseField
        public String filterType;
        @DatabaseField(id = true)
        public String fileUrl;
        @DatabaseField
        public String ext;
        @DatabaseField
        public long size;

        @DatabaseField
        public boolean externalShared;
        @DatabaseField
        public String externalUrl;
        @DatabaseField
        public String externalCode;

        @DatabaseField(foreign = true, foreignAutoRefresh = true)
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
                    ", externalShared=" + externalShared +
                    ", externalUrl='" + externalUrl + '\'' +
                    ", externalCode='" + externalCode + '\'' +
                    ", extraInfo=" + extraInfo +
                    '}';
        }
    }

    @DatabaseTable(tableName = "message_file_content_thumb")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ThumbnailUrls {

        @DatabaseField(generatedId = true)
        public long _id;

        @DatabaseField
        public String smallThumbnailUrl;
        @DatabaseField
        public String mediumThumbnailUrl;
        @DatabaseField
        public String largeThumbnailUrl;
        @DatabaseField
        public String thumbnailUrl;

        @DatabaseField
        public int width;
        @DatabaseField
        public int height;
        @DatabaseField
        public int orientation;

        @Override
        public String toString() {
            return "ThumbnailUrls{" +
                    "_id=" + _id +
                    ", smallThumbnailUrl='" + smallThumbnailUrl + '\'' +
                    ", mediumThumbnailUrl='" + mediumThumbnailUrl + '\'' +
                    ", largeThumbnailUrl='" + largeThumbnailUrl + '\'' +
                    ", width=" + width +
                    ", height=" + height +
                    ", orientation=" + orientation +
                    ", thumbnailUrl='" + thumbnailUrl + '\'' +
                    '}';
        }
    }

    @DatabaseTable(tableName = "message_info_create", daoClass = CreateEventDaoImpl.class)
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

    @DatabaseTable(tableName = "message_info_announce_create",
            daoClass = AnnounceCreateEventDaoImpl.class)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonDeserialize(using = JsonDeserializer.None.class)
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
            public long _id;

            @DatabaseField
            private long writerId;

            public long getWriterId() {
                return writerId;
            }

            public void setWriterId(long writerId) {
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
            private long writerId;

            public long getWriterId() {
                return writerId;
            }

            public void setWriterId(long writerId) {
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

    @DatabaseTable(tableName = "message_info_invite", daoClass = InviteEventDaoImpl.class)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonDeserialize(using = JsonDeserializer.None.class)
    public static class InviteEvent extends EventInfo {


        @DatabaseField
        public long invitorId;
        @ForeignCollectionField()
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
            private long inviteUserId;

            public long get_id() {
                return _id;
            }

            public void set_id(long _id) {
                this._id = _id;
            }

            public long getInviteUserId() {
                return inviteUserId;
            }

            public void setInviteUserId(long inviteUserId) {
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
        public long _id;
    }

    @DatabaseTable(tableName = "message_info_create_topic",
            daoClass = PublicCreateInfoDaoImpl.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class PublicCreateInfo extends CreateInfo {

        @DatabaseField
        @JsonProperty("ch_creatorId")
        public long creatorId;
        @DatabaseField(persisterClass = DateConverter.class)
        @JsonProperty("ch_createTime")
        public Date createTime;
        @DatabaseField
        @JsonProperty("ch_isDefault")
        public boolean isDefault;
        @JsonProperty("ch_members")
        @ForeignCollectionField()
        public Collection<IntegerWrapper> members;

        @DatabaseTable(tableName = "message_info_create_topic_member")
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
        @JsonDeserialize(using = PublicTopicCreateInfoDeserializer.class)
        public static class IntegerWrapper {
            @DatabaseField(generatedId = true)
            private long _id;
            @DatabaseField(foreign = true, foreignAutoRefresh = true)
            private PublicCreateInfo createInfo;
            @DatabaseField
            private long memberId;

            public long get_id() {
                return _id;
            }

            public void set_id(long _id) {
                this._id = _id;
            }

            public long getMemberId() {
                return memberId;
            }

            public void setMemberId(long memberId) {
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

    @DatabaseTable(tableName = "message_info_create_topic",
            daoClass = PrivateCreateInfoDaoImpl.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class PrivateCreateInfo extends CreateInfo {

        @DatabaseField
        @JsonProperty("pg_creatorId")
        public long creatorId;
        @DatabaseField(persisterClass = DateConverter.class)
        @JsonProperty("pg_createTime")
        public Date createTime;
        @DatabaseField
        @JsonProperty("pg_isDefault")
        public boolean isDefault;
        @JsonProperty("pg_members")
        @ForeignCollectionField()
        public Collection<IntegerWrapper> members;

        @DatabaseTable(tableName = "message_info_create_topic_member")
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
        @JsonDeserialize(using = PrivateTopicCreateInfoDeserializer.class)
        public static class IntegerWrapper {
            @DatabaseField(generatedId = true)
            private long _id;
            @DatabaseField(foreign = true, foreignAutoRefresh = true)
            private PrivateCreateInfo createInfo;
            @DatabaseField
            private long memberId;

            public long get_id() {
                return _id;
            }

            public void set_id(long _id) {
                this._id = _id;
            }

            public long getMemberId() {
                return memberId;
            }

            public void setMemberId(long memberId) {
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

    @DatabaseTable(tableName = "message_sticker_content")
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class StickerContent {
        @DatabaseField(id = true, useGetSet = true)
        public String _id;
        @DatabaseField
        public long groupId;
        @DatabaseField
        public String stickerId;
        @DatabaseField
        public String url;

        public String get_id() {
            return groupId + "_" + stickerId;
        }

        public void set_id(String _id) {
            this._id = _id;
        }
    }

    @DatabaseTable(tableName = "message_text_linkpreview")
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class LinkPreview {
        @DatabaseField(id = true)
        public String linkUrl;
        @DatabaseField
        public String description;
        @DatabaseField
        public String title;
        @DatabaseField
        public String imageUrl;
        @DatabaseField
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

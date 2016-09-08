package com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.dto;

import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.network.models.commonobject.StarredMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by tonyjs on 16. 3. 17..
 */
public class MentionMessage {
    private long teamId;
    private long roomId;
    private String roomName;
    private String roomType;
    private long linkId;
    private long messageId;
    private long writerId;
    private String writerName;
    private String writerProfileUrl;

    private String contentType;
    private String contentBody;
    private String contentTitle;
    private String contentExtensions;

    private List<MentionObject> mentions;
    private long feedbackId;
    private String feedbackType;
    private String feedbackTitle;
    private int commentCount;
    private Date messageCreatedAt;

    private Date createdAt;
    private long pollId;

    private MentionMessage(StarredMessage from,
                           String roomName,
                           String writerName, String writerProfileUrl) {
        this.teamId = from.getTeamId();
        this.roomId = from.getRoom() != null ? from.getRoom().id : -1;
        this.roomType = from.getRoom() != null ? from.getRoom().type : "";
        this.roomName = roomName;
        this.linkId = from.getLinkId();

        StarredMessage.Message message = from.getMessage();
        if (message != null) {
            this.messageId = message.id;
            this.writerId = message.writerId;

            this.contentType = message.contentType;
            this.contentBody = message.content.body;
            this.contentTitle = message.content.title;
            this.contentExtensions = message.content.ext;

            this.mentions = message.mentions;
            this.feedbackId = message.feedbackId;
            this.feedbackType = message.feedbackType;
            this.feedbackTitle = message.feedbackTitle;

            this.commentCount = message.commentCount;
            this.messageCreatedAt = message.createdAt;

            this.pollId = message.pollId;
        }
        this.writerName = writerName;
        this.writerProfileUrl = writerProfileUrl;

        createdAt = from.getCreatedAt();
    }

    private MentionMessage(ResMessages.Link link,
                           String roomType,
                           String roomName,
                           String writerName,
                           String photoUrl) {
        teamId = link.teamId;
        if (link.toEntity != null && !link.toEntity.isEmpty()) {
            roomId = link.toEntity.get(0);
        }
        this.roomType = roomType;
        this.roomName = roomName;
        this.linkId = link.id;
        this.messageId = link.messageId;
        this.writerId = link.message.writerId;
        this.feedbackType = link.feedbackType;

        if (link.message instanceof ResMessages.TextMessage) {
            ResMessages.TextMessage message = (ResMessages.TextMessage) link.message;
            contentType = message.contentType;
            contentBody = message.content.body;
            mentions = new ArrayList<>(message.mentions);

            this.messageCreatedAt = message.createTime;
        } else if (link.message instanceof ResMessages.CommentMessage) {
            ResMessages.CommentMessage message = (ResMessages.CommentMessage) link.message;
            this.contentType = message.contentType;
            this.contentBody = message.content.body;

            this.mentions = new ArrayList<>(message.mentions);
            this.feedbackId = message.feedbackId;
            if (link.feedback instanceof ResMessages.FileMessage) {
                this.feedbackTitle = ((ResMessages.FileMessage) link.feedback).content.title;
            } else if (link.feedback instanceof ResMessages.PollMessage) {
                ResMessages.PollMessage pollMessage = (ResMessages.PollMessage) link.feedback;
                this.feedbackTitle = pollMessage.content.body;
                this.pollId = pollMessage.pollId;
            }

            this.messageCreatedAt = message.createTime;
        }

        this.writerName = writerName;
        this.writerProfileUrl = photoUrl;
        createdAt = link.time;
    }

    public static MentionMessage createForMentions(ResMessages.Link link,
                                                   String roomType,
                                                   String roomName,
                                                   String writerName,
                                                   String photoUrl) {
        return new MentionMessage(link, roomType, roomName, writerName, photoUrl);
    }

    public static MentionMessage create(StarredMessage vo,
                                        String roomName,
                                        String writerName, String writerProfileUrl) {
        return new MentionMessage(vo, roomName, writerName, writerProfileUrl);
    }

    public long getTeamId() {
        return teamId;
    }

    public long getRoomId() {
        return roomId;
    }

    public String getRoomType() {
        return roomType;
    }

    public String getRoomName() {
        return roomName;
    }

    public long getLinkId() {
        return linkId;
    }

    public long getMessageId() {
        return messageId;
    }

    public long getWriterId() {
        return writerId;
    }

    public String getWriterName() {
        return writerName;
    }

    public String getWriterProfileUrl() {
        return writerProfileUrl;
    }

    public String getContentType() {
        return contentType;
    }

    public String getContentBody() {
        return contentBody;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public String getContentExtensions() {
        return contentExtensions;
    }

    public List<MentionObject> getMentions() {
        return mentions;
    }

    public long getFeedbackId() {
        return feedbackId;
    }

    public String getFeedbackTitle() {
        return feedbackTitle;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public Date getMessageCreatedAt() {
        return messageCreatedAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getFeedbackType() {
        return feedbackType;
    }

    @Override
    public String toString() {
        return "MentionMessage{" +
                "teamId=" + teamId +
                ", roomId=" + roomId +
                ", roomName='" + roomName + '\'' +
                ", roomType='" + roomType + '\'' +
                ", linkId=" + linkId +
                ", messageId=" + messageId +
                ", writerId=" + writerId +
                ", writerName='" + writerName + '\'' +
                ", writerProfileUrl='" + writerProfileUrl + '\'' +
                ", contentType='" + contentType + '\'' +
                ", contentBody='" + contentBody + '\'' +
                ", contentTitle='" + contentTitle + '\'' +
                ", contentExtensions='" + contentExtensions + '\'' +
                ", mentions=" + mentions +
                ", feedbackId=" + feedbackId +
                ", feedbackType=" + feedbackType +
                ", feedbackTitle='" + feedbackTitle + '\'' +
                ", commentCount=" + commentCount +
                ", messageCreatedAt=" + messageCreatedAt +
                ", createdAt=" + createdAt +
                '}';
    }


    public long getPollId() {
        return pollId;
    }
}

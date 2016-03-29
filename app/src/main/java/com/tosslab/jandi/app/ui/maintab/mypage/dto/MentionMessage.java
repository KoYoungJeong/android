package com.tosslab.jandi.app.ui.maintab.mypage.dto;

import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.network.models.commonobject.StarMentionedMessageObject;

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
    private String feedbackTitle;
    private int commentCount;
    private Date messageCreatedAt;

    private Date createdAt;

    private MentionMessage(StarMentionedMessageObject from,
                           String roomName,
                           String writerName, String writerProfileUrl) {
        this.teamId = from.getTeamId();
        this.roomId = from.getRoom() != null ? from.getRoom().id : -1;
        this.roomType = from.getRoom() != null ? from.getRoom().type : "";
        this.roomName = roomName;
        this.linkId = from.getLinkId();

        StarMentionedMessageObject.Message message = from.getMessage();
        if (message != null) {
            this.messageId = message.id;
            this.writerId = message.writerId;

            this.contentType = message.contentType;
            this.contentBody = message.content.body;
            this.contentTitle = message.content.title;
            this.contentExtensions = message.content.ext;

            this.mentions = message.mentions;
            this.feedbackId = message.feedbackId;
            this.feedbackTitle = message.feedbackTitle;

            this.commentCount = message.commentCount;
            this.messageCreatedAt = message.createdAt;
        }
        this.writerName = writerName;
        this.writerProfileUrl = writerProfileUrl;

        createdAt = from.getCreatedAt();
    }

    public static MentionMessage create(StarMentionedMessageObject vo,
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
}

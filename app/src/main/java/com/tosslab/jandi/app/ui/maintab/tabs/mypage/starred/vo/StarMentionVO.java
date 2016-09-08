package com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.vo;

import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.network.models.commonobject.StarredMessage;

import java.util.Date;
import java.util.List;

/**
 * Created by tee on 15. 7. 30..
 */
public class StarMentionVO {

    private int contentType;
    private long teamId;
    private int isStarred;
    private long linkId;
    private String writerName;
    private String writerPictureUrl;
    private String body;
    private long messageId;
    //for type is text
    private String roomName;
    private long roomId;
    private int roomType;
    private StarredMessage.Message.Content content;

    //for type is comment -  file
    private long fileId;
    private String fileName;

    private Date updatedAt;
    private List<MentionObject> mentions;
    private long writerId;
    private String feedbackType;
    private long pollId;

    public long getPollId() {
        return pollId;
    }

    public void setPollId(long pollId) {
        this.pollId = pollId;
    }

    public int getContentType() {
        return contentType;
    }

    public void setContentType(int contentType) {
        this.contentType = contentType;
    }

    public String getWriterName() {
        return writerName;
    }

    public void setWriterName(String writerName) {
        this.writerName = writerName;
    }

    public String getWriterPictureUrl() {
        return writerPictureUrl;
    }

    public void setWriterPictureUrl(String writerPictureUrl) {
        this.writerPictureUrl = writerPictureUrl;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<MentionObject> getMentions() {
        return mentions;
    }

    public void setMentions(List<MentionObject> mentions) {
        this.mentions = mentions;
    }

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public int getIsStarred() {
        return isStarred;
    }

    public void setIsStarred(int isStarred) {
        this.isStarred = isStarred;
    }

    public long getLinkId() {
        return linkId;
    }

    public void setLinkId(long linkId) {
        this.linkId = linkId;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public int getRoomType() {
        return roomType;
    }

    public void setRoomType(int roomType) {
        this.roomType = roomType;
    }

    public long getFileId() {
        return fileId;
    }

    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public StarredMessage.Message.Content getContent() {
        return content;
    }

    public void setContent(StarredMessage.Message.Content content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "StarMentionVO{" +
                "contentType=" + contentType +
                ", teamId=" + teamId +
                ", isStarred=" + isStarred +
                ", linkId=" + linkId +
                ", writerName='" + writerName + '\'' +
                ", writerPictureUrl='" + writerPictureUrl + '\'' +
                ", body='" + body + '\'' +
                ", messageId=" + messageId +
                ", roomName='" + roomName + '\'' +
                ", roomId=" + roomId +
                ", roomType=" + roomType +
                ", fileId=" + fileId +
                ", feedbackType=" + feedbackType +
                ", fileName='" + fileName + '\'' +
                ", updatedAt=" + updatedAt +
                ", mentions=" + mentions +
                '}';
    }

    public long getWriterId() {
        return writerId;
    }

    public void setWriterId(long writerId) {
        this.writerId = writerId;
    }

    public void setFeedbackType(String feedbackType) {
        this.feedbackType = feedbackType;
    }

    public String getFeedbackType() {
        return feedbackType;
    }

    public enum Type {

        Text(1), Comment(2), File(3);

        private final int num;

        Type(int num) {
            this.num = num;
        }

        public int getValue() {
            return num;
        }
    }
}

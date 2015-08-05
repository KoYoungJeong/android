package com.tosslab.jandi.app.ui.starmention.vo;

import com.tosslab.jandi.app.network.models.commonobject.MentionObject;

import java.util.Date;
import java.util.List;

/**
 * Created by tee on 15. 7. 30..
 */
public class StarMentionVO {

    private int contentType;
    private int teamId;
    private int isStarred;
    private int linkId;
    private String writerName;
    private String writerPictureUrl;
    private String content;
    private int imageResource;
    private int messageId;

    //for type is text
    private String roomName;
    private int roomId;
    private int roomType;

    //for type is comment -  file
    private int fileId;
    private String fileName;

    private Date updatedAt;
    private List<MentionObject> mentions;

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public int getTeamId() {
        return teamId;
    }

    public void setTeamId(int teamId) {
        this.teamId = teamId;
    }

    public int getIsStarred() {
        return isStarred;
    }

    public void setIsStarred(int isStarred) {
        this.isStarred = isStarred;
    }

    public int getLinkId() {
        return linkId;
    }

    public void setLinkId(int linkId) {
        this.linkId = linkId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getRoomType() {
        return roomType;
    }

    public void setRoomType(int roomType) {
        this.roomType = roomType;
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
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
                ", content='" + content + '\'' +
                ", imageResource=" + imageResource +
                ", messageId=" + messageId +
                ", roomName='" + roomName + '\'' +
                ", roomId=" + roomId +
                ", roomType=" + roomType +
                ", fileId=" + fileId +
                ", fileName='" + fileName + '\'' +
                ", updatedAt=" + updatedAt +
                ", mentions=" + mentions +
                '}';
    }

    public static enum Type {

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
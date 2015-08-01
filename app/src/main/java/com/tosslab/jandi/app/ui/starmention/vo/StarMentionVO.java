package com.tosslab.jandi.app.ui.starmention.vo;

import com.tosslab.jandi.app.network.models.commonobject.MentionObject;

import java.util.Date;
import java.util.List;

/**
 * Created by tee on 15. 7. 30..
 */
public class StarMentionVO {

    private int contentType;
    private String writerName;
    private String writerPictureUrl;
    private String content;

    //for type is text
    private String roomName;

    //for type is comment
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

    @Override
    public String toString() {
        return "StarMentionVO{" +
                "contentType=" + contentType +
                ", writerName='" + writerName + '\'' +
                ", writerPictureUrl='" + writerPictureUrl + '\'' +
                ", content='" + content + '\'' +
                ", roomName='" + roomName + '\'' +
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

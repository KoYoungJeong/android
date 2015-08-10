package com.tosslab.jandi.app.local.orm.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.tosslab.jandi.app.local.orm.dao.FileDatailDaoImpl;
import com.tosslab.jandi.app.network.models.ResMessages;

@DatabaseTable(tableName = "file_to_comment", daoClass = FileDatailDaoImpl.class)
public class FileDetail {

    @DatabaseField(id = true, useGetSet = true)
    private long id;
    @DatabaseField(foreign = true,
            foreignAutoRefresh = true,
            columnName = "fileId")
    private ResMessages.FileMessage file;
    @DatabaseField(columnName = "commentId",
            foreign = true,
            foreignAutoRefresh = true)
    private ResMessages.CommentMessage comment;
    @DatabaseField(columnName = "stickerId",
            foreign = true,
            foreignAutoRefresh = true)
    private ResMessages.CommentStickerMessage sticker;
    @DatabaseField
    private String commentType;

    public ResMessages.CommentMessage getComment() {
        return comment;
    }

    public void setComment(ResMessages.CommentMessage comment) {
        this.comment = comment;
    }

    public ResMessages.CommentStickerMessage getSticker() {
        return sticker;
    }

    public void setSticker(ResMessages.CommentStickerMessage sticker) {
        this.sticker = sticker;
    }

    public long getId() {
        return comment != null ? comment.id : sticker != null ? sticker.id : id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ResMessages.FileMessage getFile() {
        return file;
    }

    public void setFile(ResMessages.FileMessage file) {
        this.file = file;
    }

    public String getCommentType() {
        return commentType;
    }

    public void setCommentType(String commentType) {
        this.commentType = commentType;
    }

    public enum CommentType {
        TEXT, STICKER, NONE
    }
}

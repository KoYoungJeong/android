package com.tosslab.jandi.app.local.orm.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Steve SeongUg Jung on 15. 7. 23..
 */
@DatabaseTable(tableName = "message_ready_comment")
public class ReadyComment {

    @DatabaseField(id = true)
    private long fileId;
    @DatabaseField
    private String text;

    public ReadyComment() {
    }

    public ReadyComment(int fileId, String text) {
        this.fileId = fileId;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getFileId() {
        return fileId;
    }

    public void setFileId(long fileId) {
        this.fileId = fileId;
    }
}

package com.tosslab.jandi.app.local.orm.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.tosslab.jandi.app.local.orm.persister.DateConverter;
import com.tosslab.jandi.app.network.models.ResMessages;

import java.util.Date;

/**
 * Created by Steve SeongUg Jung on 15. 7. 23..
 */
@DatabaseTable(tableName = "message_sticker_recent")
public class RecentSticker {

    @DatabaseField(id = true, useGetSet = true)
    private String id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private ResMessages.StickerContent stickerContent;
    @DatabaseField(persisterClass = DateConverter.class)
    private Date lastDate;


    public ResMessages.StickerContent getStickerContent() {
        return stickerContent;
    }

    public void setStickerContent(ResMessages.StickerContent stickerContent) {
        this.stickerContent = stickerContent;
    }

    public Date getLastDate() {
        return lastDate;
    }

    public void setLastDate(Date lastDate) {
        this.lastDate = lastDate;
    }

    public String getId() {
        return stickerContent != null ? stickerContent.get_id() : id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

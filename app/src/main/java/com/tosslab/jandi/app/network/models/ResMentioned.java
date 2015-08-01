package com.tosslab.jandi.app.network.models;

import com.tosslab.jandi.app.network.models.commonobject.CursorObject;
import com.tosslab.jandi.app.network.models.commonobject.StarMentionedMessageObject;

import java.util.List;

/**
 * Created by tee on 15. 7. 29..
 */
public class ResMentioned {

    private CursorObject cursor;

    private List<StarMentionedMessageObject> records;

    public CursorObject getCursor() {
        return cursor;
    }

    public void setCursor(CursorObject cursor) {
        this.cursor = cursor;
    }

    public List<StarMentionedMessageObject> getRecords() {
        return records;
    }

    public void setRecords(List<StarMentionedMessageObject> records) {
        this.records = records;
    }

    @Override
    public String toString() {
        return "ResMentioned{" +
                "cursor=" + cursor.toString() +
                ", records=" + records.toString() +
                '}';
    }
}

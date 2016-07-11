package com.tosslab.jandi.app.local.orm.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by tonyjs on 16. 6. 24..
 */
@DatabaseTable(tableName = "message_ready_comment_for_poll")
public class ReadyCommentForPoll {

    @DatabaseField(id = true)
    private long pollId;
    @DatabaseField
    private String text;

    public ReadyCommentForPoll() {
    }

    public ReadyCommentForPoll(long pollId, String text) {
        this.pollId = pollId;
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getPollId() {
        return pollId;
    }

    public void setPollId(long pollId) {
        this.pollId = pollId;
    }
}

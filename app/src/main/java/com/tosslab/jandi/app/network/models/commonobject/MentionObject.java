package com.tosslab.jandi.app.network.models.commonobject;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.tosslab.jandi.app.local.orm.domain.SendMessage;
import com.tosslab.jandi.app.network.models.ResMessages;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Created by tee on 15. 7. 28..
 */

@DatabaseTable(tableName = "message_mention")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class MentionObject {

    @JsonIgnore
    @DatabaseField(generatedId = true)
    private long _id;
    @JsonIgnore
    @DatabaseField(foreign = true)
    private ResMessages.TextMessage textOf;
    @JsonIgnore
    @DatabaseField(foreign = true)
    private ResMessages.CommentMessage commentOf;
    @JsonIgnore
    @DatabaseField(foreign = true)
    private SendMessage sendMessageOf;
    @DatabaseField
    private int id;
    @DatabaseField
    private String type;
    @DatabaseField
    private int offset;
    @DatabaseField
    private int length;

    public MentionObject() {
    }

    public MentionObject(int id, String type, int offset, int length) {
        this.id = id;
        this.type = type;
        this.offset = offset;
        this.length = length;
    }

    public ResMessages.TextMessage getTextOf() {
        return textOf;
    }

    public void setTextOf(ResMessages.TextMessage textOf) {
        this.textOf = textOf;
    }

    public ResMessages.CommentMessage getCommentOf() {
        return commentOf;
    }

    public void setCommentOf(ResMessages.CommentMessage commentOf) {
        this.commentOf = commentOf;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public String toString() {
        return "MentionObject{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", offset=" + offset +
                ", length=" + length +
                '}';
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public SendMessage getSendMessageOf() {
        return sendMessageOf;
    }

    public void setSendMessageOf(SendMessage sendMessageOf) {
        this.sendMessageOf = sendMessageOf;
    }
}

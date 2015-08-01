package com.tosslab.jandi.app.network.models.commonobject;

/**
 * Created by tee on 15. 7. 28..
 */
public class MentionObject {

    private int id;
    private String type;
    private int offset;
    private int length;

    public MentionObject() {
    }

    public MentionObject(int id, String type, int offset, int length) {
        this.id = id;
        this.type = type;
        this.offset = offset;
        this.length = length;
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

}

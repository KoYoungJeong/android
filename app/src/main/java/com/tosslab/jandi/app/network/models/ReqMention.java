package com.tosslab.jandi.app.network.models;

/**
 * Created by tee on 15. 7. 28..
 */
public class ReqMention {

    private int id;
    private String type;
    private int offset;
    private int length;

    public ReqMention(int id, String type, int offset, int length) {
        this.id = id;
        this.type = type;
        this.offset = offset;
        this.length = length;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    @Override
    public String toString() {
        return "ReqMention{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", offset=" + offset +
                ", length=" + length +
                '}';
    }

}

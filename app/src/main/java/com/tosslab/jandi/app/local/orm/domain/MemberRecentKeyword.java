package com.tosslab.jandi.app.local.orm.domain;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "member_recent_keyword")
public class MemberRecentKeyword {
    @DatabaseField(generatedId = true)
    private long _id;
    @DatabaseField
    private String keyword;

    public MemberRecentKeyword() {
    }

    public MemberRecentKeyword(String keyword) {
        this.keyword = keyword;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}

package com.tosslab.jandi.app.ui.search.to;

/**
 * Created by Steve SeongUg Jung on 15. 3. 10..
 */
public class SearchKeyword {

    private final long _id;
    private final int type;
    private final String keyword;
    private final String initSound;

    public SearchKeyword(int type, String keyword) {
        this(-1, type, keyword, "");
    }

    public SearchKeyword(long _id, int type, String keyword, String initSound) {
        this._id = _id;
        this.type = type;
        this.keyword = keyword;
        this.initSound = initSound;
    }

    public long get_id() {
        return _id;
    }

    public int getType() {
        return type;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getInitSound() {
        return initSound;
    }
}

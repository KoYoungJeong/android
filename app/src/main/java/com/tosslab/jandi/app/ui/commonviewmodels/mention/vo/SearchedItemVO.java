package com.tosslab.jandi.app.ui.commonviewmodels.mention.vo;

/**
 * Created by tee on 15. 7. 21..
 */
public class SearchedItemVO {

    private String name;
    private long id;
    private String type;
    private int offset;
    private int length;
    private String smallProfileImageUrl;
    private boolean enabled;
    private boolean starred;
    private boolean bot;

    public String getName() {
        return name;
    }

    public SearchedItemVO setName(String name) {
        this.name = name;
        return this;
    }

    public long getId() {
        return id;
    }

    public SearchedItemVO setId(long id) {
        this.id = id;
        return this;
    }

    public String getType() {
        return type;
    }

    public SearchedItemVO setType(String type) {
        this.type = type;
        return this;
    }

    public int getOffset() {
        return offset;
    }

    public SearchedItemVO setOffset(int offset) {
        this.offset = offset;
        return this;
    }

    public int getLength() {
        return length;
    }

    public SearchedItemVO setLength(int length) {
        this.length = length;
        return this;
    }

    public String getSmallProfileImageUrl() {
        return smallProfileImageUrl;
    }

    public SearchedItemVO setSmallProfileImageUrl(String smallProfileImageUrl) {
        this.smallProfileImageUrl = smallProfileImageUrl;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public SearchedItemVO setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public boolean isStarred() {
        return starred;
    }

    public SearchedItemVO setStarred(boolean starred) {
        this.starred = starred;
        return this;
    }

    public SearchedItemVO setBot(boolean bot) {
        this.bot = bot;
        return this;
    }

    public boolean isBot() {
        return bot;
    }
}

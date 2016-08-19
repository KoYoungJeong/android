package com.tosslab.jandi.app.ui.search.main.object;

/**
 * Created by tee on 2016. 8. 18..
 */

public class SearchOneToOneRoomData extends SearchData {
    private long memberId;
    private String title;
    private String keyword;
    private String userProfileUrl;
    private boolean hasHalfLine;

    public long getMemberId() {
        return memberId;
    }

    public void setMemberId(long memberId) {
        this.memberId = memberId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserProfileUrl() {
        return userProfileUrl;
    }

    public void setUserProfileUrl(String userProfileUrl) {
        this.userProfileUrl = userProfileUrl;
    }

    public boolean hasHalfLine() {
        return hasHalfLine;
    }

    public void setHasHalfLine(boolean hasHalfLine) {
        this.hasHalfLine = hasHalfLine;
    }

    public static class Builder {

        private SearchOneToOneRoomData searchOneToOneRoomData;

        public Builder() {
            searchOneToOneRoomData = new SearchOneToOneRoomData();
        }

        public Builder setTitle(String title) {
            searchOneToOneRoomData.title = title;
            return this;
        }

        public Builder setMemberId(long memberId) {
            searchOneToOneRoomData.memberId = memberId;
            return this;
        }

        public Builder setKeyword(String keyword) {
            searchOneToOneRoomData.keyword = keyword;
            return this;
        }

        public Builder setUserProfileUrl(String userProfileUrl) {
            searchOneToOneRoomData.userProfileUrl = userProfileUrl;
            return this;
        }

        public Builder setHasHalfLine(boolean hasHalfLine) {
            searchOneToOneRoomData.hasHalfLine = hasHalfLine;
            return this;
        }

        public SearchOneToOneRoomData build() {
            return searchOneToOneRoomData;
        }
    }

}

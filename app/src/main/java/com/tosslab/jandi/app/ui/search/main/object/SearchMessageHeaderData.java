package com.tosslab.jandi.app.ui.search.main.object;

/**
 * Created by tee on 16. 7. 28..
 */
public class SearchMessageHeaderData extends SearchData {

    private boolean isShowProgress = false;
    private boolean isShowSearchedResultMessage = true;
    private int searchedMessageCount = 0;
    private boolean hasMore = false;
    private String roomName = "";
    private String memberName = "";

    public boolean isShowProgress() {
        return isShowProgress;
    }

    public boolean isShowSearchedResultMessage() {
        return isShowSearchedResultMessage;
    }

    public int getSearchedMessageCount() {
        return searchedMessageCount;
    }

    public boolean hasMore() {
        return hasMore;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getMemberName() {
        return memberName;
    }

    public static class Builder {
        private SearchMessageHeaderData searchMessageHeaderData;

        public Builder() {
            this.searchMessageHeaderData = new SearchMessageHeaderData();
        }

        public Builder setShowProgress(boolean isShowProgress) {
            searchMessageHeaderData.isShowProgress = isShowProgress;
            return this;
        }

        public Builder setShowSearchedResultMessage(boolean isShowSearchedResultMessage) {
            searchMessageHeaderData.isShowSearchedResultMessage = isShowSearchedResultMessage;
            return this;
        }

        public Builder setSearchedMessageCount(int searchedMessageCount) {
            searchMessageHeaderData.searchedMessageCount = searchedMessageCount;
            return this;
        }

        public Builder setHasMore(boolean hasMore) {
            searchMessageHeaderData.hasMore = hasMore;
            return this;
        }

        public Builder setType(int type) {
            searchMessageHeaderData.type = type;
            return this;
        }

        public Builder setRoomName(String roomName) {
            searchMessageHeaderData.roomName = roomName;
            return this;
        }

        public Builder setMemberName(String memberName) {
            searchMessageHeaderData.memberName = memberName;
            return this;
        }

        public SearchMessageHeaderData build() {
            return searchMessageHeaderData;
        }

    }
}

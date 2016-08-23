package com.tosslab.jandi.app.ui.search.main.object;

/**
 * Created by tee on 16. 7. 29..
 */
public class SearchHistoryData extends SearchData {

    private String keyword;

    public String getKeyword() {
        return keyword;
    }

    public static class Builder {

        private SearchHistoryData searchHistoryData;

        public Builder() {
            searchHistoryData = new SearchHistoryData();
            searchHistoryData.setType(SearchData.ITEM_TYPE_HISTORY_ITEM);
        }

        public Builder setKeyword(String keyword) {
            searchHistoryData.keyword = keyword;
            return this;
        }

        public SearchHistoryData build() {
            return searchHistoryData;
        }
    }

}

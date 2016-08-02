package com.tosslab.jandi.app.ui.search.main_temp.presenter;

import com.tosslab.jandi.app.ui.search.main_temp.adapter.SearchAdapterDataModel;

import java.util.List;

/**
 * Created by tee on 16. 7. 25..
 */
public interface SearchPresenter {

    void sendSearchQuery(String keyword);

    void setSearchAdapterDataModel(SearchAdapterDataModel searchAdapterDataModel);

    void setChangeIsShowUnjoinedTopic(boolean isShowUnjoinedTopic);

    void sendMoreResults();

    void sendSearchHistory();

    void upsertKeywordHistory(String keyword);

    List<String> getOldQueryList(String keyword);

    void onDeleteaAllHistoryItem();

    void onDeleteaHistoryItemByKeyword(String keyword);

    interface View {

        void refreshSearchedAll();

        void refreshHistory();

        void showMoreProgressBar();

        void dismissMoreProgressBar();

    }
}

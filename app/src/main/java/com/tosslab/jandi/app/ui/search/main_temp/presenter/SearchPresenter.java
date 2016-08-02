package com.tosslab.jandi.app.ui.search.main_temp.presenter;

import com.tosslab.jandi.app.ui.search.main_temp.adapter.SearchAdapterDataModel;

/**
 * Created by tee on 16. 7. 25..
 */
public interface SearchPresenter {

    void sendSearchQuery(String keyword, boolean isShowUnjoinedTopic);

    void setSearchAdapterDataModel(SearchAdapterDataModel searchAdapterDataModel);

    void sendSearchQueryOnlyTopicRoom(String keyword, boolean isShowUnjoinedTopic);

    interface View {

        void refreshAll();
    }
}

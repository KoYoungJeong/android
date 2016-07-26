package com.tosslab.jandi.app.ui.search.main_temp.presenter;

import com.tosslab.jandi.app.ui.search.main_temp.adapter.SearchAdapterDataModel;
import com.tosslab.jandi.app.ui.search.main_temp.model.SearchModel;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchTopicRoomData;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by tee on 16. 7. 25..
 */
public class SearchPresenterImpl implements SearchPresenter {

    @Inject
    SearchModel searchModel;

    @Inject
    SearchPresenter.View view;

    private SearchAdapterDataModel searchAdapterDataModel;

    @Inject
    public SearchPresenterImpl() {
    }

    public void setSearchAdapterDataModel(SearchAdapterDataModel searchAdapterDataModel) {
        this.searchAdapterDataModel = searchAdapterDataModel;
    }

    public void sendSearchQuery(String keyword, boolean isShowUnjoinedTopic) {

        List<SearchTopicRoomData> topicRoomDatas = searchModel.getSearchedTopics(keyword, isShowUnjoinedTopic);
        searchAdapterDataModel.setSearchTopicRoomDatas(topicRoomDatas);


        view.refreshAll();
    }

    public void sendSearchQueryOnlyTopicRoom(String keyword, boolean isShowUnjoinedTopic) {
        List<SearchTopicRoomData> topicRoomDatas = searchModel.getSearchedTopics(keyword, isShowUnjoinedTopic);
        searchAdapterDataModel.setSearchTopicRoomDatas(topicRoomDatas);
        view.refreshAll();
    }

}
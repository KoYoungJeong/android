package com.tosslab.jandi.app.ui.search.main_temp.adapter;

import com.tosslab.jandi.app.ui.search.main_temp.object.SearchHistoryData;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchMessageData;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchMessageHeaderData;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchTopicRoomData;

import java.util.List;

/**
 * Created by tee on 16. 7. 25..
 */
public interface SearchAdapterDataModel {
    void setSearchTopicRoomDatas(List<SearchTopicRoomData> searchTopicRoomDatas);

    void setSearchMessageDatas(List<SearchMessageData> searchMessageDatas);

    void clearSearchTopicRoomDatas();

    void clearSearchMessageDatas();

    void setMessageHeaderData(SearchMessageHeaderData searchMessageHeaderData);

    SearchAdapter.MoreState getMoreState();

    void setMoreState(SearchAdapter.MoreState moreState);

    void addSearchMessageDatas(List<SearchMessageData> searchMessageDatas);

    void setSearchHistoryDatas(List<SearchHistoryData> searchHistoryDatas);
}

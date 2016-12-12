package com.tosslab.jandi.app.ui.search.main.adapter;

import com.tosslab.jandi.app.ui.search.main.object.SearchHistoryData;
import com.tosslab.jandi.app.ui.search.main.object.SearchMessageData;
import com.tosslab.jandi.app.ui.search.main.object.SearchMessageHeaderData;
import com.tosslab.jandi.app.ui.search.main.object.SearchOneToOneRoomData;
import com.tosslab.jandi.app.ui.search.main.object.SearchTopicRoomData;

import java.util.List;

/**
 * Created by tee on 16. 7. 25..
 */
public interface SearchAdapterDataModel {
    void setSearchTopicRoomDatas(List<SearchTopicRoomData> searchTopicRoomDatas);

    void setSearchMessageDatas(List<SearchMessageData> searchMessageDatas);

    void clearSearchMessageDatas();

    void setMessageHeaderData(SearchMessageHeaderData searchMessageHeaderData);

    void addSearchMessageDatas(List<SearchMessageData> searchMessageDatas);

    void setSearchHistoryDatas(List<SearchHistoryData> searchHistoryDatas);

    void setSearchOneToOneRoomDatas(List<SearchOneToOneRoomData> searchOneToOneRoomDatas);

    void setOnlyMessageMode(boolean onlyMessageMode);

    boolean isHistoryMode();

    void setLoading(boolean loading);

    void setGuest(boolean guest);
}

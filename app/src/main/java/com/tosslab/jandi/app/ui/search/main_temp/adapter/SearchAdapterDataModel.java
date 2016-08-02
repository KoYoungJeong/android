package com.tosslab.jandi.app.ui.search.main_temp.adapter;

import com.tosslab.jandi.app.ui.search.main_temp.object.SearchMessageData;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchTopicRoomData;

import java.util.List;

/**
 * Created by tee on 16. 7. 25..
 */
public interface SearchAdapterDataModel {
    void setSearchTopicRoomDatas(List<SearchTopicRoomData> searchTopicRoomDatas);

    void setSearchMessageDatas(List<SearchMessageData> searchMessageDatas);
}

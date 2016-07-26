package com.tosslab.jandi.app.ui.search.main_temp.adapter;

import com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder.RoomHeaderViewHolder;

/**
 * Created by tee on 16. 7. 25..
 */
public interface SearchAdapterViewModel {

    void refreshAll();

    void refreshTopicInfos();

    void setOnCheckChangeListener(RoomHeaderViewHolder.OnCheckChangeListener onCheckChangeListener);
}

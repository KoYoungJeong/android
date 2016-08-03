package com.tosslab.jandi.app.ui.search.main_temp.adapter;

import com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder.HistoryHeaderViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder.HistoryItemViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder.MessageItemViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder.RoomHeaderViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder.RoomItemViewHolder;

/**
 * Created by tee on 16. 7. 25..
 */
public interface SearchAdapterViewModel {

    void refreshSearchedAll();

    void setOnCheckChangeListener(RoomHeaderViewHolder.OnCheckChangeListener onCheckChangeListener);

    void setOnRequestMoreMessage(SearchAdapter.OnRequestMoreMessage onRequestMoreMessage);

    void refreshHistory();

    void setOnDeleteAllHistory(
            HistoryHeaderViewHolder.OnDeleteAllHistory onDeleteAllHistory);

    void setOnDeleteHistoryListener(
            HistoryItemViewHolder.OnDeleteHistoryListener onDeleteHistoryListener);

    void setOnSelectHistoryListener(
            HistoryItemViewHolder.OnSelectHistoryListener onSelectHistoryListener);

    void setOnClickTopicListener(
            RoomItemViewHolder.OnClickTopicListener onClickTopicListener);

    void setOnClickMessageListener(
            MessageItemViewHolder.OnClickMessageListener onClickMessageListener);

}

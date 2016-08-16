package com.tosslab.jandi.app.ui.search.main.adapter;

import com.tosslab.jandi.app.ui.search.main.adapter.viewholder.HistoryHeaderViewHolder;
import com.tosslab.jandi.app.ui.search.main.adapter.viewholder.HistoryItemViewHolder;
import com.tosslab.jandi.app.ui.search.main.adapter.viewholder.MessageHeaderViewHolder;
import com.tosslab.jandi.app.ui.search.main.adapter.viewholder.MessageItemViewHolder;
import com.tosslab.jandi.app.ui.search.main.adapter.viewholder.RoomHeaderViewHolder;
import com.tosslab.jandi.app.ui.search.main.adapter.viewholder.RoomItemViewHolder;

/**
 * Created by tee on 16. 7. 25..
 */
public interface SearchAdapterViewModel {

    void refreshSearchedAll();

    void refreshSearchOnlyMessage();

    void setOnCheckChangeListener(RoomHeaderViewHolder.OnCheckChangeListener onCheckChangeListener);

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

    void setOnClickRoomSelectionButtonListener(
            MessageHeaderViewHolder.OnClickRoomSelectionButtonListener onClickRoomSelectionButtonListener);

    void setOnClickMemberSelectionButtonListener(
            MessageHeaderViewHolder.OnClickMemberSelectionButtonListener onClickMemberSelectionButtonListener);

}

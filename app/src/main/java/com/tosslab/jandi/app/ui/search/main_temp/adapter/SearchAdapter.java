package com.tosslab.jandi.app.ui.search.main_temp.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder.HistoryHeaderViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder.HistoryItemViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder.HistoryNoItemViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder.MessageHeaderViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder.MessageItemViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder.NoMessageItemViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder.NoRoomItemViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder.RoomHeaderViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder.RoomItemViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchData;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchHistoryData;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchMessageData;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchMessageHeaderData;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchTopicRoomData;
import com.tosslab.jandi.app.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by tee on 16. 7. 21..
 */
public class SearchAdapter extends RecyclerView.Adapter<BaseViewHolder>
        implements SearchAdapterDataModel, SearchAdapterViewModel {

    private boolean isMessageItemFold = false;
    private boolean isRoomItemFold = false;

    private List<SearchTopicRoomData> searchTopicRoomDatas = new ArrayList<>();
    private List<SearchMessageData> searchMessageDatas = new ArrayList<>();
    private SearchMessageHeaderData searchMessageHeaderData = null;
    private List<SearchHistoryData> searchHistoryDatas = new ArrayList<>();

    private List<SearchData> datas = new ArrayList<>();

    private RoomHeaderViewHolder.OnCheckChangeListener onCheckChangeListener;

    private MoreState moreState = MoreState.Idle;

    private OnRequestMoreMessage onRequestMoreMessage;

    private boolean isHistoryMode = false;
    private HistoryHeaderViewHolder.OnDeleteAllHistory onDeleteAllHistory;
    private HistoryItemViewHolder.OnDeleteHistoryListener onDeleteHistoryListener;
    private HistoryItemViewHolder.OnSelectHistoryListener onSelectHistoryListener;
    private RoomItemViewHolder.OnClickTopicListener onClickTopicListener;
    private MessageItemViewHolder.OnClickMessageListener onClickMessageListener;
    private MessageHeaderViewHolder.OnClickRoomSelectionButtonListener onClickRoomSelectionButtonListener;
    private MessageHeaderViewHolder.OnClickMemberSelectionButtonListener onClickMemberSelectionButtonListener;

    @Override
    public void setSearchTopicRoomDatas(List<SearchTopicRoomData> searchTopicRoomDatas) {
        this.searchTopicRoomDatas.clear();
        this.searchTopicRoomDatas = searchTopicRoomDatas;
    }

    @Override
    public void setSearchMessageDatas(List<SearchMessageData> searchMessageDatas) {
        this.searchMessageDatas.clear();
        this.searchMessageDatas = searchMessageDatas;
    }

    @Override
    public void addSearchMessageDatas(List<SearchMessageData> searchMessageDatas) {
        this.searchMessageDatas.addAll(searchMessageDatas);
    }

    @Override
    public void setMessageHeaderData(SearchMessageHeaderData searchMessageHeaderData) {
        this.searchMessageHeaderData = searchMessageHeaderData;
    }

    @Override
    public void setSearchHistoryDatas(List<SearchHistoryData> searchHistoryDatas) {
        this.searchHistoryDatas = searchHistoryDatas;
    }

    public int getSearchTopicCnt() {
        return searchTopicRoomDatas.size();
    }

    private void makeAllDatas() {
        isHistoryMode = false;
        datas.clear();
        makeTopicDatas();
        makeMessageDatas();
    }

    private void makeHistoryDatas() {
        isHistoryMode = true;
        datas.clear();

        SearchMessageHeaderData searchMessageHeaderDataForHistory =
                new SearchMessageHeaderData.Builder()
                        .setHasMore(false)
                        .setShowProgress(false)
                        .setShowSearchedResultMessage(false)
                        .setType(SearchData.ITEM_TYPE_MESSAGE_HEADER_FOR_HISTORY)
                        .build();
        datas.add(searchMessageHeaderDataForHistory);

        SearchData searchHistoryHeaderData = new SearchData();
        searchHistoryHeaderData.setType(SearchData.ITEM_TYPE_HISTORY_HEADER);
        datas.add(searchHistoryHeaderData);

        if (searchHistoryDatas.size() > 0) {
            Observable.from(searchHistoryDatas)
                    .map(searchHistoryData ->
                            datas.add(searchHistoryData)
                    ).subscribe();
        } else {
            SearchData searchHistoryNoData = new SearchData();
            searchHistoryNoData.setType(SearchData.ITEM_TYPE_NO_HISTORY_ITEM);
            datas.add(searchHistoryNoData);
        }

    }

    private void makeMessageDatas() {
        if (searchMessageHeaderData != null) {
            searchMessageHeaderData.setType(SearchData.ITEM_TYPE_MESSAGE_HEADER);
            datas.add(searchMessageHeaderData);
        }
        if (searchMessageDatas.size() > 0) {
            Observable.from(searchMessageDatas)
                    .map(searchMessageData ->
                            datas.add(searchMessageData)
                    ).subscribe();
        } else {
            SearchData data = new SearchData();
            data.setType(SearchData.ITEM_TYPE_NO_MESSAGE_ITEM);
            datas.add(data);
        }
    }

    private void makeTopicDatas() {
        if (searchTopicRoomDatas.size() > 0) {
            SearchData headerData = new SearchData();
            headerData.setType(SearchData.ITEM_TYPE_ROOM_HEADER);
            datas.add(headerData);
            Observable.from(searchTopicRoomDatas)
                    .map(searchTopicRoomData -> {
                                searchTopicRoomData.setType(SearchData.ITEM_TYPE_ROOM_ITEM);
                                datas.add(searchTopicRoomData);
                                return searchTopicRoomData;
                            }
                    ).subscribe();
        } else {
            SearchData data = new SearchData();
            data.setType(SearchData.ITEM_TYPE_NO_ROOM_ITEM);
            datas.add(data);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return datas.get(position).getType();
    }


    @Override
    public int getItemCount() {
        return datas.size();
    }

    @Override
    public BaseViewHolder<SearchData> onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case SearchData.ITEM_TYPE_MESSAGE_HEADER:
                return MessageHeaderViewHolder.newInstance(parent);
            case SearchData.ITEM_TYPE_MESSAGE_ITEM:
                MessageItemViewHolder messageItemViewHolder = MessageItemViewHolder.newInstance(parent);
                if (onClickMessageListener != null) {
                    messageItemViewHolder.setOnClickMessageListener(onClickMessageListener);
                }
                return messageItemViewHolder;
            case SearchData.ITEM_TYPE_NO_MESSAGE_ITEM:
                return NoMessageItemViewHolder.newInstance(parent);
            case SearchData.ITEM_TYPE_ROOM_HEADER:
                RoomHeaderViewHolder roomHeaderViewHolder = RoomHeaderViewHolder.newInstance(parent);
                if (onCheckChangeListener != null) {
                    roomHeaderViewHolder.setSetOnCheckChangeListener(onCheckChangeListener);
                }
                return roomHeaderViewHolder;
            case SearchData.ITEM_TYPE_ROOM_ITEM:
                RoomItemViewHolder roomItemViewHolder = RoomItemViewHolder.newInstance(parent);
                if (onClickTopicListener != null) {
                    roomItemViewHolder.setOnClickTopicListener(onClickTopicListener);
                }
                return roomItemViewHolder;
            case SearchData.ITEM_TYPE_NO_ROOM_ITEM:
                return NoRoomItemViewHolder.newInstance(parent);
            case SearchData.ITEM_TYPE_HISTORY_HEADER:
                HistoryHeaderViewHolder historyHeaderViewHolder = HistoryHeaderViewHolder.newInstance(parent);
                if (onDeleteAllHistory != null) {
                    historyHeaderViewHolder.setOnDeleteAllHistory(onDeleteAllHistory);
                }
                return historyHeaderViewHolder;
            case SearchData.ITEM_TYPE_HISTORY_ITEM:
                HistoryItemViewHolder historyItemViewHolder = HistoryItemViewHolder.newInstance(parent);
                if (onSelectHistoryListener != null) {
                    historyItemViewHolder.setOnSelectHistoryListener(onSelectHistoryListener);
                }
                if (onDeleteHistoryListener != null) {
                    historyItemViewHolder.setOnDeleteHistoryListener(onDeleteHistoryListener);
                }
                return historyItemViewHolder;
            case SearchData.ITEM_TYPE_NO_HISTORY_ITEM:
                return HistoryNoItemViewHolder.newInstance(parent);
            case SearchData.ITEM_TYPE_MESSAGE_HEADER_FOR_HISTORY:
                return MessageHeaderViewHolder.newInstance(parent);
        }
        return RoomHeaderViewHolder.newInstance(parent);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        setViewConfiguration(holder, position);
        holder.onBindView(datas.get(position));

        // message scroll
        if (!isHistoryMode
                && position > getItemCount() - 3
                && moreState == MoreState.Idle
                && searchMessageHeaderData.hasMore()) {
            moreState = MoreState.Loading;
            onRequestMoreMessage.onRequestMoreMessage();
        }
    }

    private void setViewConfiguration(RecyclerView.ViewHolder holder, int position) {
        if (isMessageItemFold) {
            if (getItemViewType(position) == SearchData.ITEM_TYPE_MESSAGE_HEADER) {
                holder.itemView.getLayoutParams().height = 0;
            }

            if (getItemViewType(position) == SearchData.ITEM_TYPE_MESSAGE_ITEM) {
                holder.itemView.getLayoutParams().height = 0;
            }

            if (getItemViewType(position) == SearchData.ITEM_TYPE_NO_MESSAGE_ITEM) {
                holder.itemView.getLayoutParams().height = 0;
            }
        } else {
            if (getItemViewType(position) == SearchData.ITEM_TYPE_MESSAGE_HEADER) {
                holder.itemView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }

            if (getItemViewType(position) == SearchData.ITEM_TYPE_MESSAGE_ITEM) {
                holder.itemView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }

            if (getItemViewType(position) == SearchData.ITEM_TYPE_NO_MESSAGE_ITEM) {
                holder.itemView.getLayoutParams().height = (int) UiUtils.getPixelFromDp(75);
            }
        }

        if (isRoomItemFold) {
            if (getItemViewType(position) == SearchData.ITEM_TYPE_ROOM_HEADER) {
                holder.itemView.getLayoutParams().height = 0;
            }

            if (getItemViewType(position) == SearchData.ITEM_TYPE_ROOM_ITEM) {
                holder.itemView.getLayoutParams().height = 0;
            }

            if (getItemViewType(position) == SearchData.ITEM_TYPE_NO_ROOM_ITEM) {
                holder.itemView.getLayoutParams().height = 0;
            }

        } else {
            if (getItemViewType(position) == SearchData.ITEM_TYPE_ROOM_HEADER) {
                holder.itemView.getLayoutParams().height = (int) UiUtils.getPixelFromDp(48);
            }

            if (getItemViewType(position) == SearchData.ITEM_TYPE_ROOM_ITEM) {
                holder.itemView.getLayoutParams().height = (int) UiUtils.getPixelFromDp(75);
            }

            if (getItemViewType(position) == SearchData.ITEM_TYPE_NO_ROOM_ITEM) {
                holder.itemView.getLayoutParams().height = (int) UiUtils.getPixelFromDp(75);
            }
        }
    }

    public void onClickHeader(long headerId, boolean isFold) {
        if (headerId == 1) {
            isMessageItemFold = isFold;
        } else {
            isRoomItemFold = isFold;
        }
        notifyItemRangeChanged(0, getItemCount());
    }

    @Override
    public void refreshSearchedAll() {
        makeAllDatas();
        notifyDataSetChanged();
    }

    @Override
    public void refreshHistory() {
        makeHistoryDatas();
        notifyDataSetChanged();
    }

    @Override
    public void setOnCheckChangeListener(RoomHeaderViewHolder.OnCheckChangeListener onCheckChangeListener) {
        this.onCheckChangeListener = onCheckChangeListener;
    }

    @Override
    public void clearSearchTopicRoomDatas() {
        searchTopicRoomDatas.clear();
    }

    @Override
    public void clearSearchMessageDatas() {
        searchMessageDatas.clear();
    }

    @Override
    public MoreState getMoreState() {
        return moreState;
    }

    @Override
    public void setMoreState(MoreState moreState) {
        this.moreState = moreState;
    }

    public void setOnRequestMoreMessage(OnRequestMoreMessage onRequestMoreMessage) {
        this.onRequestMoreMessage = onRequestMoreMessage;
    }

    @Override
    public void setOnDeleteAllHistory(
            HistoryHeaderViewHolder.OnDeleteAllHistory onDeleteAllHistory) {
        this.onDeleteAllHistory = onDeleteAllHistory;
    }

    @Override
    public void setOnDeleteHistoryListener(
            HistoryItemViewHolder.OnDeleteHistoryListener onDeleteHistoryListener) {
        this.onDeleteHistoryListener = onDeleteHistoryListener;
    }

    @Override
    public void setOnSelectHistoryListener(
            HistoryItemViewHolder.OnSelectHistoryListener onSelectHistoryListener) {
        this.onSelectHistoryListener = onSelectHistoryListener;
    }

    @Override
    public void setOnClickTopicListener(RoomItemViewHolder.OnClickTopicListener onClickTopicListener) {
        this.onClickTopicListener = onClickTopicListener;
    }

    @Override
    public void setOnClickMessageListener(MessageItemViewHolder.OnClickMessageListener onClickMessageListener) {
        this.onClickMessageListener = onClickMessageListener;
    }

    @Override
    public void setOnClickRoomSelectionButtonListener(
            MessageHeaderViewHolder.OnClickRoomSelectionButtonListener onClickRoomSelectionButtonListener) {
        this.onClickRoomSelectionButtonListener = onClickRoomSelectionButtonListener;
    }

    @Override
    public void setOnClickMemberSelectionButtonListener(
            MessageHeaderViewHolder.OnClickMemberSelectionButtonListener onClickMemberSelectionButtonListener) {
        this.onClickMemberSelectionButtonListener = onClickMemberSelectionButtonListener;
    }

    public enum MoreState {
        Idle, Loading
    }

    public interface OnRequestMoreMessage {
        void onRequestMoreMessage();
    }

}

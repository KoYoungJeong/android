package com.tosslab.jandi.app.ui.search.main_temp.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder.MessageHeaderViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder.MessageItemViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder.NoMessageItemViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder.NoRoomItemViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder.RoomHeaderViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder.RoomItemViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchData;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchMessageData;
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

    private List<SearchData> datas = new ArrayList<>();

    private RoomHeaderViewHolder.OnCheckChangeListener onCheckChangeListener;

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

    public int getSearchTopicCnt() {
        return searchTopicRoomDatas.size();
    }

    public void makeAllDatas() {
        datas.clear();
        makeTopicDatas();
        makeMessageDatas();
    }

    private void makeMessageDatas() {
        if (searchMessageDatas.size() > 0) {
            SearchData data = new SearchData();
            data.setType(SearchData.ITEM_TYPE_MESSAGE_HEADER);
            datas.add(new SearchData());
            Observable.from(searchMessageDatas)
                    .map(searchMessageData -> {
                                searchMessageData.setType(SearchData.ITEM_TYPE_MESSAGE_ITEM);
                                datas.add(searchMessageData);
                                return searchMessageData;
                            }
                    ).subscribe();
        } else {
            SearchData data = new SearchData();
            data.setType(SearchData.ITEM_TYPE_NO_MESSAGE_ITEM);
            datas.add(data);
        }
    }

    private void makeTopicDatas() {
        if (searchTopicRoomDatas.size() > 0) {
            SearchData data = new SearchData();
            data.setType(SearchData.ITEM_TYPE_ROOM_HEADER);
            datas.add(data);
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
                return MessageItemViewHolder.newInstance(parent);
            case SearchData.ITEM_TYPE_NO_MESSAGE_ITEM:
                return NoMessageItemViewHolder.newInstance(parent);
            case SearchData.ITEM_TYPE_ROOM_HEADER:
                RoomHeaderViewHolder roomHeaderViewHolder = RoomHeaderViewHolder.newInstance(parent);
                roomHeaderViewHolder.setSetOnCheckChangeListener(onCheckChangeListener);
                return roomHeaderViewHolder;
            case SearchData.ITEM_TYPE_ROOM_ITEM:
                return RoomItemViewHolder.newInstance(parent);
            case SearchData.ITEM_TYPE_NO_ROOM_ITEM:
                return NoRoomItemViewHolder.newInstance(parent);
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
                holder.itemView.getLayoutParams().height = (int) UiUtils.getPixelFromDp(96);
            }

            if (getItemViewType(position) == SearchData.ITEM_TYPE_MESSAGE_ITEM) {
                holder.itemView.getLayoutParams().height = (int) UiUtils.getPixelFromDp(140);
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
    public void refreshAll() {
        makeAllDatas();
        notifyDataSetChanged();
    }

    @Override
    public void refreshTopicInfos() {

    }

    @Override
    public void setOnCheckChangeListener(RoomHeaderViewHolder.OnCheckChangeListener onCheckChangeListener) {
        this.onCheckChangeListener = onCheckChangeListener;
    }
}

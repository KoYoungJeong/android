package com.tosslab.jandi.app.ui.search.main_temp.adapter;

import android.view.ViewGroup;

import com.eowise.recyclerview.stickyheaders.StickyHeadersAdapter;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder.SearchStickyHeaderViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchData;

/**
 * Created by tee on 16. 7. 22..
 */
public class SearchStickyHeaderAdapter implements StickyHeadersAdapter<SearchStickyHeaderViewHolder> {

    private SearchAdapter searchAdapter;

    private boolean isMessageItemFold = false;
    private boolean isRoomItemFold = false;

    public SearchStickyHeaderAdapter(SearchAdapter searchStickyAdapter) {
        this.searchAdapter = searchStickyAdapter;
    }

    @Override
    public long getHeaderId(int position) {
        int itemType = searchAdapter.getItemViewType(position);
        switch (itemType) {
            case SearchData.ITEM_TYPE_MESSAGE_HEADER:
            case SearchData.ITEM_TYPE_MESSAGE_ITEM:
            case SearchData.ITEM_TYPE_NO_MESSAGE_ITEM:
                return 1;
            case SearchData.ITEM_TYPE_ROOM_HEADER:
            case SearchData.ITEM_TYPE_ROOM_ITEM:
            case SearchData.ITEM_TYPE_NO_ROOM_ITEM:
                return 2;
        }
        return -1;
    }

    @Override
    public SearchStickyHeaderViewHolder onCreateViewHolder(ViewGroup parent) {
        return SearchStickyHeaderViewHolder.newInstance(parent);
    }

    @Override
    public void onBindViewHolder(SearchStickyHeaderViewHolder searchStickyHeaderViewHolder, int position) {
        int itemType = searchAdapter.getItemViewType(position);
        switch (itemType) {
            case SearchData.ITEM_TYPE_MESSAGE_HEADER:
            case SearchData.ITEM_TYPE_MESSAGE_ITEM:
            case SearchData.ITEM_TYPE_NO_MESSAGE_ITEM:
                searchStickyHeaderViewHolder.setType(SearchStickyHeaderViewHolder.TYPE_MESSAGE);
                searchStickyHeaderViewHolder.setFoldIcon(isMessageItemFold);
                break;
            case SearchData.ITEM_TYPE_ROOM_HEADER:
            case SearchData.ITEM_TYPE_ROOM_ITEM:
            case SearchData.ITEM_TYPE_NO_ROOM_ITEM:
                searchStickyHeaderViewHolder.setType(SearchStickyHeaderViewHolder.TYPE_ROOM);
                searchStickyHeaderViewHolder.setCount(searchAdapter.getSearchTopicCnt());
                searchStickyHeaderViewHolder.setFoldIcon(isRoomItemFold);
                break;
        }
        searchStickyHeaderViewHolder.onBindView(new Object());
    }

    public void setMessageItemFold(boolean messageItemFold) {
        isMessageItemFold = messageItemFold;
    }

    public void setRoomItemFold(boolean roomItemFold) {
        isRoomItemFold = roomItemFold;
    }

}
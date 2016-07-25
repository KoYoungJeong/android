package com.tosslab.jandi.app.ui.search.main_temp.adapter;

import android.view.ViewGroup;

import com.eowise.recyclerview.stickyheaders.StickyHeadersAdapter;
import com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder.SearchStickyHeaderViewHolder;

/**
 * Created by tee on 16. 7. 22..
 */
public class SearchStickyHeaderAdapter implements StickyHeadersAdapter<SearchStickyHeaderViewHolder> {

    private SearchAdapter searchStickyAdapter;


    public SearchStickyHeaderAdapter(SearchAdapter searchStickyAdapter) {
        this.searchStickyAdapter = searchStickyAdapter;
    }

    @Override
    public long getHeaderId(int position) {
        int itemType = searchStickyAdapter.getItemViewType(position);
        switch (itemType) {
            case SearchAdapter.ITEM_TYPE_MESSAGE_HEADER:
            case SearchAdapter.ITEM_TYPE_MESSAGE_ITEM:
                return 1;
            case SearchAdapter.ITEM_TYPE_ROOM_HEADER:
            case SearchAdapter.ITEM_TYPE_ROOM_ITEM:
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
        int itemType = searchStickyAdapter.getItemViewType(position);
        switch (itemType) {
            case SearchAdapter.ITEM_TYPE_MESSAGE_HEADER:
            case SearchAdapter.ITEM_TYPE_MESSAGE_ITEM:
                searchStickyHeaderViewHolder.setType(SearchStickyHeaderViewHolder.TYPE_MESSAGE);
                break;
            case SearchAdapter.ITEM_TYPE_ROOM_HEADER:
            case SearchAdapter.ITEM_TYPE_ROOM_ITEM:
                searchStickyHeaderViewHolder.setType(SearchStickyHeaderViewHolder.TYPE_ROOM);
                break;
        }

        searchStickyHeaderViewHolder.onBindView(new Object());
    }

}

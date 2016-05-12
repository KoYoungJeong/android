package com.tosslab.jandi.app.ui.message.v2.viewmodel;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.tosslab.jandi.app.ui.message.v2.adapter.MessageListAdapterModel;

public class LinkRecyclerViewManager {
    private final LinearLayoutManager layoutManager;
    private MessageListAdapterModel adapterModel;
    private long firstLinkId = -1;
    private long lastLinkId = -1;
    private int firstItemTop;
    private int cachedLastItemPosition;
    private int cachedItemCount;

    public LinkRecyclerViewManager(RecyclerView recyclerView, MessageListAdapterModel adapterModel) {
        this.adapterModel = adapterModel;
        this.layoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
    }

    public void updateFirstVisibleItem() {
        int position = layoutManager.findFirstVisibleItemPosition();
        if (position >= 0) {
            firstLinkId = adapterModel.getItem(position).id;
            if (layoutManager.getChildCount() > 0) {
                firstItemTop = layoutManager.getChildAt(0).getTop();
            }
        }
    }

    public void updateLastVisibleItem() {
        cachedLastItemPosition = layoutManager.findLastVisibleItemPosition();
        cachedItemCount = adapterModel.getCount();
        if (cachedLastItemPosition >= 0) {
            lastLinkId = adapterModel.getItem(cachedLastItemPosition).id;
        }
    }

    public void scrollToCachedFirst() {
        if (lastLinkId > 0) {
            int position = adapterModel.indexOfLinkId(lastLinkId);
            layoutManager.scrollToPosition(position);
        }
    }

    public boolean isScrollInMiddleAsLastStatus() {
        return cachedLastItemPosition >= 0 && cachedLastItemPosition < cachedItemCount - 1;

    }
}

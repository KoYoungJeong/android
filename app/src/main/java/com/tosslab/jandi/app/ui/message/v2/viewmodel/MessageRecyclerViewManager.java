package com.tosslab.jandi.app.ui.message.v2.viewmodel;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.tosslab.jandi.app.ui.message.v2.adapter.MessageListAdapterModel;

public class MessageRecyclerViewManager {
    private final LinearLayoutManager layoutManager;
    private MessageListAdapterModel adapterModel;
    private long firstLinkId = -1;
    private long lastLinkId = -1;
    private int firstItemTop;
    private int cachedLastItemPosition;
    private int cachedItemCount;

    public MessageRecyclerViewManager(RecyclerView recyclerView, MessageListAdapterModel adapterModel) {
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

    public void scrollToLast() {
        if (adapterModel.getCount() > 0) {
            layoutManager.scrollToPosition(adapterModel.getCount() - 1);
        }
    }

    public void scrollToLinkId(long linkId) {
        int position = adapterModel.indexOfLinkId(linkId);
        if (position >= 0) {
            layoutManager.scrollToPosition(position);
        }
    }

    public boolean isScrollInMiddleAsLastStatus() {
        return cachedLastItemPosition >= 0 && cachedLastItemPosition < cachedItemCount - 1;

    }

    public void scrollToLinkId(long lastReadLinkId, int top) {
        int position = adapterModel.indexOfLinkId(lastReadLinkId);

        if (position > 0) {
            position = Math.min(adapterModel.getCount() - 1, position + 1);
            layoutManager.scrollToPositionWithOffset(position, top);
        } else if (position < 0) {
            layoutManager.scrollToPosition(adapterModel.getCount() - 1);
        }
    }
}

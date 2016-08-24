package com.tosslab.jandi.app.ui.starred.adapter.view;

import com.tosslab.jandi.app.network.models.commonobject.StarredMessage;

/**
 * Created by tonyjs on 2016. 8. 9..
 */
public interface StarredListDataView {
    void notifyDataSetChanged();

    void setOnLoadMoreCallback(OnLoadMoreCallback onLoadMoreCallback);

    void setOnItemClickListener(OnItemClickListener onItemClickListener);

    void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener);

    interface OnItemClickListener {
        void onItemClick(StarredMessage message);
    }

    interface OnItemLongClickListener {
        boolean onItemLongClick(long messageId);
    }

    interface OnLoadMoreCallback {
        void onLoadMore(long messageId);
    }
}

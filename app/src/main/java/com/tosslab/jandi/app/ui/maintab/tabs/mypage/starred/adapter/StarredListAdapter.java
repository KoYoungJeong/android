package com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.adapter;

import android.support.annotation.Nullable;
import android.view.ViewGroup;

import com.tosslab.jandi.app.network.models.commonobject.StarredMessage;
import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.adapter.model.StarredListDataModel;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.adapter.view.StarredListDataView;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.adapter.viewholder.StarredCommentViewHolder;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.adapter.viewholder.StarredFileViewHolder;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.adapter.viewholder.StarredMessageViewHolder;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.adapter.viewholder.StarredPollViewHolder;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by tee on 15. 7. 29..
 */
public class StarredListAdapter extends MultiItemRecyclerAdapter
        implements StarredListDataModel, StarredListDataView {

    private static final int LOAD_MORE_OFFSET = 4;

    private static final int VIEW_TYPE_TEXT_MESSAGE = 0;
    private static final int VIEW_TYPE_FILE_MESSAGE = 1;
    private static final int VIEW_TYPE_POLL_MESSAGE = 2;
    private static final int VIEW_TYPE_COMMENT_MESSAGE = 3;

    private long loadMoreOffset;

    private OnLoadMoreCallback onLoadMoreCallback;
    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_TEXT_MESSAGE) {
            return StarredMessageViewHolder.newInstance(parent);
        } else if (viewType == VIEW_TYPE_FILE_MESSAGE) {
            return StarredFileViewHolder.newInstance(parent);
        } else if (viewType == VIEW_TYPE_POLL_MESSAGE) {
            return StarredPollViewHolder.newInstance(parent);
        } else if (viewType == VIEW_TYPE_COMMENT_MESSAGE) {
            return StarredCommentViewHolder.newInstance(parent);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        if (onItemClickListener != null
                && getItem(position) instanceof StarredMessage) {
            final StarredMessage starredMessage = getItem(position);
            holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(starredMessage));
        }

        if (onItemLongClickListener != null
                && getItem(position) instanceof StarredMessage) {
            final StarredMessage starredMessage = getItem(position);
            holder.itemView.setOnLongClickListener(v ->
                    onItemLongClickListener.onItemLongClick(starredMessage.getMessage().id));
        }

        loadMoreIfNeed(position);
    }

    private void loadMoreIfNeed(int position) {
        if (onLoadMoreCallback == null) {
            return;
        }

        if (position == getItemCount() - LOAD_MORE_OFFSET) {
            StarredMessage lastItem = getLastStarredMessage();
            if (lastItem == null) {
                return;
            }

            long starredId = lastItem.getStarredId();

            if (starredId == loadMoreOffset) {
                return;
            }

            loadMoreOffset = starredId;
            onLoadMoreCallback.onLoadMore(starredId);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public void clear() {
        super.clear();
    }

    @Override
    public List<Row<?>> getStarredListRows(List<StarredMessage> records) {
        List<Row<?>> rows = new ArrayList<>();
        if (records == null || records.isEmpty()) {
            return rows;
        }
        Observable.from(records)
                .concatMap(starredMessage -> Observable.just(getMessageRow(starredMessage)))
                .collect(() -> rows, List::add)
                .subscribe();
        return rows;
    }

    @Override
    public void removeByMessageId(long messageId) {
        for (int i = getItemCount() - 1; i >= 0; i--) {
            if (!(getItem(i) instanceof StarredMessage)) {
                continue;
            }

            StarredMessage message = getItem(i);
            if (message.getMessage().id == messageId) {
                remove(i);
                break;
            }
        }
    }

    @Override
    public StarredMessage findMessageById(long messageId) {
        return Observable.from(getRows())
                .map(Row::getItem)
                .ofType(StarredMessage.class)
                .takeFirst(starredMessage -> starredMessage.getMessage().id == messageId)
                .toBlocking()
                .firstOrDefault(new StarredMessage());
    }

    @Override
    public List<Row<?>> getStarredMessageRow(StarredMessage message) {
        List<Row<?>> rows = new ArrayList<>();
        if (message == null || message.getStarredId() <= 0) {
            return rows;
        }

        rows.add(getMessageRow(message));
        return rows;
    }

    @Override
    public boolean isEmpty() {
        return getRows() == null || getRows().size() <= 0;
    }

    private Row<?> getMessageRow(StarredMessage starredMessage) {
        int itemViewType = VIEW_TYPE_TEXT_MESSAGE;

        String contentType = starredMessage.getMessage().contentType;
        if ("text".equals(contentType)) {
            itemViewType = VIEW_TYPE_TEXT_MESSAGE;
        } else if ("comment".equals(contentType)) {
            itemViewType = VIEW_TYPE_COMMENT_MESSAGE;
        } else if ("file".equals(contentType)) {
            itemViewType = VIEW_TYPE_FILE_MESSAGE;
        } else if ("poll".equals(contentType)) {
            itemViewType = VIEW_TYPE_POLL_MESSAGE;
        }
        return Row.create(starredMessage, itemViewType);
    }

    @Override
    public void setOnLoadMoreCallback(OnLoadMoreCallback onLoadMoreCallback) {
        this.onLoadMoreCallback = onLoadMoreCallback;
    }

    @Nullable
    public StarredMessage getLastStarredMessage() {
        for (int i = getItemCount() - 1; i >= 0; i--) {
            if (getItem(i) instanceof StarredMessage) {
                return getItem(i);
            }
        }
        return null;
    }

}

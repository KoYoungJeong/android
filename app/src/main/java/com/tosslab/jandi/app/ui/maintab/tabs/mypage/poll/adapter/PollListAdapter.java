package com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.adapter;

import android.view.ViewGroup;

import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.adapter.model.PollListDataModel;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.adapter.view.PollListDataView;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.poll.adapter.viewholder.PollViewHolder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rx.Observable;

/**
 * Created by tonyjs on 16. 6. 28..
 */
public class PollListAdapter extends MultiItemRecyclerAdapter
        implements PollListDataModel, PollListDataView {

    public static final int VIEW_TYPE_POLL = 0;

    private static final int DEFAULT_LOAD_MORE_OFFSET = 5;

    private OnPollClickListener onPollClickListener;
    private OnLoadMoreCallback onLoadMoreCallback;
    private long loadMoreOffset;

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return PollViewHolder.newInstance(parent);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        if (getItemViewType(position) == VIEW_TYPE_POLL
                && onPollClickListener != null) {
            holder.itemView.setOnClickListener(v -> {
                onPollClickListener.onPollClick(getItem(position));
            });
        }

        loadMoreIfNeed(position);
    }

    public void setOnPollClickListener(OnPollClickListener onPollClickListener) {
        this.onPollClickListener = onPollClickListener;
    }

    public void setOnLoadMoreCallback(OnLoadMoreCallback onLoadMoreCallback) {
        this.onLoadMoreCallback = onLoadMoreCallback;
    }

    private void loadMoreIfNeed(int position) {
        if (onLoadMoreCallback == null) {
            return;
        }

        int itemCount = getItemCount();

        if (position == itemCount - DEFAULT_LOAD_MORE_OFFSET) {
            Poll lastItem = getItem(itemCount - 1);
            if (lastItem == null) {
                return;
            }

            if ("created".equals(lastItem.getStatus())) {
                return;
            }

            long pollId = lastItem.getId();
            if (pollId == loadMoreOffset) {
                return;
            }

            loadMoreOffset = pollId;
            onLoadMoreCallback.onLoadMore(lastItem.getFinishedAt());
        }
    }

    public void clearLoadMoreOffset() {
        loadMoreOffset = 0;
    }

    @Override
    public void addPolls(List<Poll> polls) {
        if (polls == null || polls.isEmpty()) {
            return;
        }

        List<Row<?>> rows = new ArrayList<>();
        Observable.from(polls)
                .subscribe(
                        poll -> rows.add(Row.create(poll, VIEW_TYPE_POLL)),
                        Throwable::printStackTrace,
                        () -> addRows(rows));
    }

    @Override
    public void addPoll(int position, Poll poll) {
        addRow(position, Row.create(poll, VIEW_TYPE_POLL));
    }

    @Override
    public int getIndexOfFirstFinishedPoll() {
        for (int i = 0; i < getItemCount(); i++) {
            Object item = getItem(i);
            if (!(item instanceof Poll)) {
                continue;
            }

            String status = ((Poll) item).getStatus();
            if (!("created".equals(status))) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getIndexById(long pollId) {
        for (int i = 0; i < getItemCount(); i++) {
            Object item = getItem(i);
            if (!(item instanceof Poll)) {
                continue;
            }

            long id = ((Poll) item).getId();
            if (id == pollId) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void removePollByIndex(int index) {
        remove(index);
    }

    @Override
    public void removePollByIdAndStats(long pollId, String status) {
        Observable.from(getRows())
                .filter(row -> {
                    Object item = row.getItem();
                    if (!(item instanceof Poll)) {
                        return false;
                    }
                    Poll poll = (Poll) item;
                    return poll.getId() == pollId
                            && status.equals(poll.getStatus());
                })
                .takeFirst(row -> {
                    if (row != null) {
                        try {
                            remove(row);
                            return true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return false;
                })
                .subscribe();
    }

    @Override
    public void setPoll(int index, Poll poll) {
        setRow(index, Row.create(poll, VIEW_TYPE_POLL));
    }

    @Override
    public void clearPoll() {
        clear();
    }

    @Override
    public int size() {
        return getItemCount();
    }

    @Override
    public Poll getPoll(int position) {
        return getItem(position);
    }

    public interface OnPollClickListener {
        void onPollClick(Poll poll);
    }

    public interface OnLoadMoreCallback {
        void onLoadMore(Date date);
    }
}

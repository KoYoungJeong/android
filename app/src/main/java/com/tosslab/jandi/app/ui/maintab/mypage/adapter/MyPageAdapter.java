package com.tosslab.jandi.app.ui.maintab.mypage.adapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.maintab.mypage.adapter.viewholder.MentionMessageViewHolder;
import com.tosslab.jandi.app.ui.maintab.mypage.dto.MentionMessage;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tonyjs on 16. 3. 15..
 */
public class MyPageAdapter extends RecyclerView.Adapter<BaseViewHolder<MentionMessage>> {

    private static final int LOAD_MORE_OFFSET = 3;

    private List<MentionMessage> mentionMessageList = new ArrayList<>();

    private OnMentionClickListener onMentionClickListener;

    private OnLoadMoreCallback onLoadMoreCallback;
    private long enqueueLoadMoreRequestMessageId;

    @Override
    public BaseViewHolder<MentionMessage> onCreateViewHolder(ViewGroup parent, int viewType) {
        return MentionMessageViewHolder.newInstance(parent);
    }

    public void addAll(List<MentionMessage> mentionMessageList) {
        this.mentionMessageList.addAll(mentionMessageList);
    }

    @Nullable
    public MentionMessage getItem(int position) {
        if (position < mentionMessageList.size()) {
            return mentionMessageList.get(position);
        }

        return null;
    }

    @Override
    public int getItemCount() {
        return mentionMessageList.size();
    }

    @Override
    public void onBindViewHolder(BaseViewHolder<MentionMessage> holder, int position) {
        holder.onBindView(getItem(position));

        holder.itemView.setOnClickListener(v -> {
            if (onMentionClickListener != null) {
                onMentionClickListener.onMentionClick(getItem(position));
            }
        });

        loadMoreIfNeed(position);
    }

    public void setOnMentionClickListener(OnMentionClickListener onMentionClickListener) {
        this.onMentionClickListener = onMentionClickListener;
    }

    public void setOnLoadMoreCallback(OnLoadMoreCallback onLoadMoreCallback) {
        this.onLoadMoreCallback = onLoadMoreCallback;
    }

    private void loadMoreIfNeed(int position) {
        if (onLoadMoreCallback == null) {
            return;
        }

        int itemCount = getItemCount();

        if (position == itemCount - LOAD_MORE_OFFSET) {
            MentionMessage lastItem = getItem(itemCount - 1);
            if (lastItem == null) {
                return;
            }

            long messageId = lastItem.getMessageId();

            if (messageId == enqueueLoadMoreRequestMessageId) {
                return;
            }

            enqueueLoadMoreRequestMessageId = messageId;
            onLoadMoreCallback.onLoadMore(messageId);
        }
    }

    public void clear() {
        mentionMessageList.clear();
    }

    public void remove(int index) {
        mentionMessageList.remove(index);
    }

    public interface OnMentionClickListener {
        void onMentionClick(MentionMessage mention);
    }

    public interface OnLoadMoreCallback {
        void onLoadMore(long messageId);
    }
}

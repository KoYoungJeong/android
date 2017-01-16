package com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.adapter;

import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.adapter.model.MentionListDataModel;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.adapter.view.MentionListDataView;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.adapter.viewholder.MentionMessageViewHolder;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.mention.dto.MentionMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tonyjs on 16. 3. 15..
 */
public class MentionListAdapter extends RecyclerView.Adapter<BaseViewHolder<MentionMessage>>
        implements MentionListDataModel, MentionListDataView {

    private static final int LOAD_MORE_OFFSET = 3;

    private List<MentionMessage> mentionMessageList = new ArrayList<>();

    private OnMentionClickListener onMentionClickListener;
    private OnMentionLongClickListener onMentionLongClickListener;

    private OnLoadMoreCallback onLoadMoreCallback;
    private long loadMoreOffset;
    private long lastReadMessageId;

    @Override
    public BaseViewHolder<MentionMessage> onCreateViewHolder(ViewGroup parent, int viewType) {
        return MentionMessageViewHolder.newInstance(parent);
    }

    @Override
    public void addAll(List<MentionMessage> mentionMessageList) {
        this.mentionMessageList.addAll(mentionMessageList);
    }

    @Nullable
    @Override
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
        MentionMessage mentionMessage = getItem(position);
        if (mentionMessage == null) {
            return;
        }

        boolean hasNextItem = getItemCount() - 1 != position;

        if (hasNextItem &&
                (mentionMessageList.get(position).getMessageCreatedAt().getMonth()
                        == mentionMessageList.get(position + 1).getMessageCreatedAt().getMonth()) &&
                (mentionMessageList.get(position).getMessageCreatedAt().getDate()
                        == mentionMessageList.get(position + 1).getMessageCreatedAt().getDate())) {
            ((MentionMessageViewHolder) holder).setHalfDivider(true);
        } else {
            ((MentionMessageViewHolder) holder).setHalfDivider(false);
        }

        holder.onBindView(mentionMessage);

        holder.itemView.setOnClickListener(v -> {
            if (onMentionClickListener != null) {
                onMentionClickListener.onMentionClick(getItem(position));
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onMentionLongClickListener != null) {
                onMentionLongClickListener.onMentionLongClick(getItem(position));
                return true;
            }

            return false;
        });

        if (lastReadMessageId > 0 && mentionMessage.getMessageId() > lastReadMessageId) {
            int background = holder.itemView.getResources().getColor(R.color.rgb_00abe8_10);
            holder.itemView.setBackgroundColor(background);
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }

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

            if (messageId == loadMoreOffset) {
                return;
            }

            loadMoreOffset = messageId;
            onLoadMoreCallback.onLoadMore(messageId);
        }
    }

    public void clearLoadMoreOffset() {
        loadMoreOffset = 0;
    }

    @Override
    public void setLastReadMessageId(long messageId) {
        lastReadMessageId = messageId;
    }

    @Override
    public void clear() {
        mentionMessageList.clear();
    }

    @Override
    public void remove(int index) {
        mentionMessageList.remove(index);
    }

    @Override
    public int indexOfLink(long linkId) {
        int size = getItemCount();
        MentionMessage item;
        for (int idx = 0; idx < size; idx++) {
            item = getItem(idx);
            if (item != null && item.getLinkId() == linkId) {
                return idx;
            }
        }
        return -1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void addAll(int position, List<MentionMessage> mentionMessages) {
        mentionMessageList.addAll(position, mentionMessages);
    }

    @Override
    public void add(int position, MentionMessage mentionMessage) {
        mentionMessageList.add(position, mentionMessage);
    }

    public MentionListAdapter setOnMentionLongClickListener(OnMentionLongClickListener onMentionLongClickListener) {
        this.onMentionLongClickListener = onMentionLongClickListener;
        return this;
    }

    public interface OnMentionClickListener {
        void onMentionClick(MentionMessage mention);
    }

    public interface OnMentionLongClickListener {
        void onMentionLongClick(MentionMessage mention);
    }

    public interface OnLoadMoreCallback {
        void onLoadMore(long messageId);
    }
}

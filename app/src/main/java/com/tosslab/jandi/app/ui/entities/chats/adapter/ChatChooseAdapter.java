package com.tosslab.jandi.app.ui.entities.chats.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.entities.chats.domain.DisableDummyItem;
import com.tosslab.jandi.app.ui.entities.chats.domain.EmptyChatChooseItem;
import com.tosslab.jandi.app.ui.members.adapter.searchable.viewholder.EmptySearchedMemberViewHolder;
import com.tosslab.jandi.app.ui.members.adapter.searchable.viewholder.MemberViewHolder;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatChooseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements ChatChooseAdapterDataView, ChatChooseAdapterDataModel {

    private static final int TYPE_QUERY_EMPTY = 2;
    private static final int TYPE_DISABLED = 1;
    private static final int TYPE_NORMAL = 0;
    private Context context;
    private List<ChatChooseItem> chatChooseItems;
    private OnRecyclerItemClickListener onRecyclerItemClickListener;

    public ChatChooseAdapter(Context context) {
        this.context = context;
        chatChooseItems = new ArrayList<>();
    }

    @Override
    public boolean isEmpty() {
        return chatChooseItems.isEmpty();
    }

    @Override
    public int getCount() {
        return chatChooseItems.size();
    }

    @Override
    public ChatChooseItem getItem(int position) {
        return chatChooseItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return getCount();
    }


    @Override
    public void add(ChatChooseItem chatChooseItem) {
        chatChooseItems.add(chatChooseItem);
    }

    @Override
    public void addAll(List<ChatChooseItem> chatListWithoutMe) {
        chatChooseItems.addAll(chatListWithoutMe);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == TYPE_NORMAL) {
            MemberViewHolder viewholder = MemberViewHolder.createForChatChooseItem(parent);
            viewholder.setIsTeamMemberList(true);
            return viewholder;
        } else if (viewType == TYPE_QUERY_EMPTY) {
            return EmptySearchedMemberViewHolder.newInstance(parent);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_disabled_folding, parent, false);
            return new DisableFoldingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int itemViewType = getItemViewType(position);
        if (itemViewType == TYPE_NORMAL) {
            ((MemberViewHolder) holder).setProfileImageClickable(true);
            ((MemberViewHolder) holder).onBindView(getItem(position));
        } else if (itemViewType == TYPE_QUERY_EMPTY) {
            ((EmptySearchedMemberViewHolder) holder).onBindView(((EmptyChatChooseItem) getItem(position)).getQuery());
        }

        holder.itemView.setOnClickListener(v -> {
            if (onRecyclerItemClickListener != null) {
                onRecyclerItemClickListener.onItemClick(holder.itemView, ChatChooseAdapter.this, position);
            }
        });

        if (holder instanceof MemberViewHolder) {
            MemberViewHolder memberViewHolder = (MemberViewHolder)holder;
            if (position == getItemCount() - 1) {
                memberViewHolder.showFullDivider();
            } else {
                memberViewHolder.showHalfDivider();
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatChooseItem item = getItem(position);
        if (item instanceof DisableDummyItem) {
            return TYPE_DISABLED;
        } else if (item instanceof EmptyChatChooseItem) {
            return TYPE_QUERY_EMPTY;
        } else {
            return TYPE_NORMAL;
        }
    }

    @Override
    public void clear() {
        chatChooseItems.clear();
    }

    @Override
    public void remove(ChatChooseItem chatChooseItem) {
        chatChooseItems.remove(chatChooseItem);
    }

    @Override
    public void refresh() {
        notifyDataSetChanged();
    }

    public void setOnRecyclerItemClickListener(OnRecyclerItemClickListener onRecyclerItemClickListener) {
        this.onRecyclerItemClickListener = onRecyclerItemClickListener;
    }

    static class DisableFoldingViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_disabled_folding_title)
        public TextView tvTitle;
        @Bind(R.id.iv_disabled_folding_icon)
        public ImageView ivIcon;

        public DisableFoldingViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}

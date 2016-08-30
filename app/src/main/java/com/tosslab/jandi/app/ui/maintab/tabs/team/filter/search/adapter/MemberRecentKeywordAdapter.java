package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search.domain.MemberRecentSearchKeyword;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.search.domain.MemberSearchKeyword;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MemberRecentKeywordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_EMPTY = 2;
    public static final int TYPE_DEFAULT = 1;
    public static final int TYPE_HEADER = 0;

    List<MemberSearchKeyword> keywords;

    private OnDeleteItem onDeleteItem;
    private OnDeleteAll onDeleteAll;
    private OnRecyclerItemClickListener itemClickListener;

    public MemberRecentKeywordAdapter() {
        keywords = new ArrayList<>();
    }

    public void setOnDeleteItem(OnDeleteItem onDeleteItem) {
        this.onDeleteItem = onDeleteItem;
    }

    public void setOnDeleteAll(OnDeleteAll onDeleteAll) {
        this.onDeleteAll = onDeleteAll;
    }

    public void setItemClickListener(OnRecyclerItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void addAll(List<MemberSearchKeyword> keywords) {
        this.keywords.addAll(keywords);
    }

    public void clear() {
        keywords.clear();
    }

    public MemberSearchKeyword getActualItem(int position) {
        return keywords.get(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case TYPE_HEADER:
                return HeaderViewHolder.create(parent);
            default:
            case TYPE_EMPTY:
                return EmptyViewHolder.create(parent);
            case TYPE_DEFAULT:
                return KeywordViewHolder.create(parent);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int itemViewType = getItemViewType(position);

        switch (itemViewType) {
            case TYPE_HEADER:
                HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
                if (getItemCount() > 1) {
                    int nextViewType = getItemViewType(position + 1);
                    if (nextViewType == TYPE_DEFAULT) {
                        headerViewHolder.tvDelete.setVisibility(View.VISIBLE);
                    } else {
                        headerViewHolder.tvDelete.setVisibility(View.GONE);
                    }
                } else {
                    headerViewHolder.tvDelete.setVisibility(View.GONE);
                }

                headerViewHolder.tvDelete.setOnClickListener(v -> {
                    if (onDeleteAll != null) {
                        onDeleteAll.onDeleteAll();
                    }
                });

                break;
            case TYPE_DEFAULT:
                MemberSearchKeyword item = getActualItem(position - 1);
                KeywordViewHolder keywordViewHolder = (KeywordViewHolder) holder;
                keywordViewHolder.tvSearchKeyword.setText(((MemberRecentSearchKeyword) item).getKeyword());
                keywordViewHolder.ivDeleteButton.setOnClickListener(v -> {
                    if (onDeleteItem != null) {
                        onDeleteItem.onDeleteItem(position - 1);
                    }
                });
                break;
            default:
                break;
        }

        if (itemViewType == TYPE_DEFAULT) {
            holder.itemView.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(v, MemberRecentKeywordAdapter.this, position - 1);
                }
            });
        } else {
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else {
            return getActualItem(position - 1).getType();
        }
    }

    @Override
    public int getItemCount() {
        // 검색어 + 헤더
        return keywords.size() + 1;
    }

    public int getActualItemCount() {
        return keywords.size();
    }

    public void add(MemberSearchKeyword keyword) {
        keywords.add(keyword);
    }

    public void remove(long id) {
        for (int idx = 0, size = keywords.size(); idx < size; idx++) {
            if (getActualItem(idx) instanceof MemberRecentSearchKeyword) {
                MemberRecentSearchKeyword item = (MemberRecentSearchKeyword) getActualItem(idx);
                if (item.getId() == id) {
                    keywords.remove(idx);
                    return;
                }
            }
        }
    }

    public interface OnDeleteAll {
        void onDeleteAll();
    }

    public interface OnDeleteItem {
        void onDeleteItem(int position);
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tv_member_search_header_delete)
        TextView tvDelete;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        static HeaderViewHolder create(ViewGroup parent) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.item_member_search_header, parent, false);
            return new HeaderViewHolder(view);
        }
    }

    static class KeywordViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tv_search_keyword)
        TextView tvSearchKeyword;

        @Bind(R.id.iv_delete_button)
        ImageView ivDeleteButton;

        public KeywordViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        static KeywordViewHolder create(ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View itemView = inflater.inflate(R.layout.item_search_history_item, parent, false);
            return new KeywordViewHolder(itemView);
        }
    }

    static class EmptyViewHolder extends RecyclerView.ViewHolder {

        public EmptyViewHolder(View itemView) {
            super(itemView);
        }

        static EmptyViewHolder create(ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_search_history_no_item, parent, false);
            return new EmptyViewHolder(view);
        }
    }

}

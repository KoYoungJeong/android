package com.tosslab.jandi.app.ui.base.adapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tonyjs on 16. 1. 19..
 * 리스트 내에서 다양한 객체들이 보여져야 할 때 사용
 */
public abstract class MultiItemRecyclerAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private List<Row<?>> rows = new ArrayList<>();

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        holder.onBindView(getItem(position));
    }

    @SuppressWarnings("unchecked")
    public synchronized <ITEM> ITEM getItem(int position) {
        return (ITEM) rows.get(position).getItem();
    }

    public synchronized void addRow(int position, Row<?> row) {
        if (getItemCount() - 1 < position) {
            return;
        }
        this.rows.add(position, row);
    }

    public synchronized void addRow(Row<?> row) {
        this.rows.add(row);
    }

    public synchronized void addRows(List<Row<?>> rows) {
        this.rows.addAll(rows);
    }

    public synchronized void setRow(int position, Row<?> row) {
        if (getItemCount() - 1 < position) {
            this.rows.add(position, row);
        }
        this.rows.set(position, row);
    }

    public synchronized void remove(int position) {
        if (getItemCount() - 1 < position) {
            return;
        }
        this.rows.remove(position);
    }

    public synchronized List<Row<?>> getRows() {
        return rows;
    }

    public synchronized void setRows(List<Row<?>> rows) {
        this.rows.clear();
        this.rows.addAll(rows);
    }

    @Nullable
    public Row getRow(int position) {
        if (position >= 0 && rows.size() > position) {
            return rows.get(position);
        }
        return null;
    }

    public synchronized void addRows(int position, List<Row<?>> rows) {
        this.rows.addAll(position, rows);
    }

    public synchronized void remove(Row<?> row) {
        this.rows.remove(row);
    }

    public synchronized void clear() {
        this.rows.clear();
    }

    @Override
    public synchronized int getItemCount() {
        return rows.size();
    }

    @Override
    public synchronized int getItemViewType(int position) {
        int viewType = rows.get(position).getItemViewType();
        return viewType;
    }

    @Override
    public void onViewDetachedFromWindow(BaseViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.onViewDetachedFromWindow();
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public static class Row<ITEM> {
        private ITEM item;
        private int itemViewType;

        public Row(ITEM item, int itemViewType) {
            this.item = item;
            this.itemViewType = itemViewType;
        }

        public static <ITEM> Row<ITEM> create(ITEM item, int itemViewType) {
            return new Row<>(item, itemViewType);
        }

        public ITEM getItem() {
            return item;
        }

        public int getItemViewType() {
            return itemViewType;
        }
    }
}
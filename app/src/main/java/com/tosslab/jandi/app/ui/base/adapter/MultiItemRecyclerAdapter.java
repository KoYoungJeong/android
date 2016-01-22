package com.tosslab.jandi.app.ui.base.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

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
    public <ITEM> ITEM getItem(int position) {
        return (ITEM) rows.get(position).getItem();
    }

    public void addRow(Row<?> row) {
        this.rows.add(row);
    }

    public void addRows(List<Row<?>> rows) {
        this.rows.addAll(rows);
    }

    public void setRow(int position, Row<?> row) {
        if (getItemCount() - 1 < position) {
            this.rows.add(position, row);
        }
        this.rows.set(position, row);
    }

    public void setRows(List<Row<?>> rows) {
        this.rows.clear();
        this.rows.addAll(rows);
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    @Override
    public int getItemViewType(int position) {
        int viewType = rows.get(position).getItemViewType();
        return viewType;
    }

    public static class Row<ITEM> {
        private ITEM item;
        private int itemViewType;

        public Row(ITEM item, int itemViewType) {
            this.item = item;
            this.itemViewType = itemViewType;
        }

        public ITEM getItem() {
            return item;
        }

        public int getItemViewType() {
            return itemViewType;
        }
    }

}

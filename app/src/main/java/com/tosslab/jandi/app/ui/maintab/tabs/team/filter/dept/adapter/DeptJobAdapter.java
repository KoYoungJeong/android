package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.adapter;


import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.domain.DeptJob;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DeptJobAdapter extends RecyclerView.Adapter<DeptJobAdapter.DeptJobViewHolder>
        implements DeptJobDataModel, DeptJobDataView {
    private List<DeptJob> names;
    private OnRecyclerItemClickListener onRecyclerItemClickListener;
    private boolean selectMode = false;

    public DeptJobAdapter(boolean selectMode) {
        this.selectMode = selectMode;
        names = new ArrayList<>();
    }

    @Override
    public DeptJobViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dept_job, parent, false);

        return new DeptJobViewHolder(view);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(DeptJobViewHolder holder, int position) {
        DeptJob item = getItem(position);
        holder.tvTitle.setText(item.getName(), TextView.BufferType.SPANNABLE);
        holder.tvCount.setText(String.valueOf(item.getCount()));

        if (!selectMode && (position < getItemCount() - 1)) {
            DeptJob nextItem = getItem(position + 1);
            if (TextUtils.equals(item.getHeader(), nextItem.getHeader())) {
                holder.vDivider.setVisibility(View.VISIBLE);
            } else {
                holder.vDivider.setVisibility(View.INVISIBLE);
            }
        } else {
            holder.vDivider.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onRecyclerItemClickListener != null) {
                onRecyclerItemClickListener.onItemClick(v, DeptJobAdapter.this, position);
            }
        });
    }

    @Override
    public DeptJob getItem(int position) {
        return names.get(position);
    }

    @Override
    public int getSize() {
        return getItemCount();
    }

    @Override
    public void refresh() {
        notifyDataSetChanged();
    }

    @Override
    public void setOnItemClick(OnRecyclerItemClickListener onRecyclerItemClickListener) {

        this.onRecyclerItemClickListener = onRecyclerItemClickListener;
    }

    @Override
    public void add(DeptJob item) {
        names.add(item);
    }

    @Override
    public void addAll(List<DeptJob> items) {
        names.addAll(items);
    }

    @Override
    public void clear() {
        names.clear();
    }

    @Override
    public int getItemCount() {
        return names.size();
    }

    static class DeptJobViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tv_dept_job_title)
        TextView tvTitle;

        @Bind(R.id.tv_dept_job_count)
        TextView tvCount;

        @Bind(R.id.v_dept_job_divider)
        View vDivider;

        public DeptJobViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}

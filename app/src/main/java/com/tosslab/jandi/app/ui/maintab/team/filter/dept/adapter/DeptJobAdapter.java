package com.tosslab.jandi.app.ui.maintab.team.filter.dept.adapter;


import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DeptJobAdapter extends RecyclerView.Adapter<DeptJobAdapter.DeptJobViewHolder>
        implements DeptJobDataModel, DeptJobDataView {
    private List<Pair<String, String>> names;
    private String keyword;
    private OnRecyclerItemClickListener onRecyclerItemClickListener;

    public DeptJobAdapter() {
        names = new ArrayList<>();
    }

    @Override
    public DeptJobViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dept_job, parent, false);

        return new DeptJobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DeptJobViewHolder holder, int position) {
        Pair<String, String> item = getItem(position);
        holder.tvTitle.setText(item.first);

        holder.itemView.setOnClickListener(v -> {
            if (onRecyclerItemClickListener != null) {
                onRecyclerItemClickListener.onItemClick(v, DeptJobAdapter.this, position);
            }
        });
    }

    @Override
    public Pair<String, String> getItem(int position) {
        return names.get(position);
    }

    @Override
    public void setKeyword(String keyword) {
        this.keyword = keyword;
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
    public void add(Pair<String, String> item) {
        names.add(item);
    }

    @Override
    public void addAll(List<Pair<String, String>> items) {
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

        public DeptJobViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}

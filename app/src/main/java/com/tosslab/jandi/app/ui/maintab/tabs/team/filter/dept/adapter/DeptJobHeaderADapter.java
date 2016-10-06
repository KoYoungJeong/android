package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eowise.recyclerview.stickyheaders.StickyHeadersAdapter;
import com.tosslab.jandi.app.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DeptJobHeaderADapter implements StickyHeadersAdapter<DeptJobHeaderADapter.ViewHolder> {

    private final DeptJobDataModel deptJobDataModel;

    public DeptJobHeaderADapter(DeptJobDataModel deptJobDataModel) {
        this.deptJobDataModel = deptJobDataModel;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team_member_header, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {

        String second = deptJobDataModel.getItem(position).getHeader();
        viewHolder.tvTitle.setText(second);
    }

    @Override
    public long getHeaderId(int position) {
        return deptJobDataModel.getItem(position).getHeader().hashCode();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tv_team_member_header_message)
        TextView tvTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}

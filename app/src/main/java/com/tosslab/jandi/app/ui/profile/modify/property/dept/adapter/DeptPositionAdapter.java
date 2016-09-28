package com.tosslab.jandi.app.ui.profile.modify.property.dept.adapter;

import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.profile.modify.property.dept.DeptPositionActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tee on 16. 7. 5..
 */
public class DeptPositionAdapter extends RecyclerView.Adapter {

    // first : value , second : is new value
    private List<Pair<String, Boolean>> datas = new ArrayList<>();

    private String keyword = "";

    private OnItemClickListener onItemClickListener;

    private String mode = DeptPositionActivity.EXTRA_JOB_TITLE_MODE;

    public void setDatas(List<Pair<String, Boolean>> datas) {
        this.datas = datas;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_department_jobtitle, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ViewHolder viewHolder = (ViewHolder) holder;
        String data = datas.get(position).first;

        if (data == null || data.equals("")) {
            viewHolder.getTvName().setText("");
            viewHolder.getTvNewAdd().setVisibility(View.GONE);
            return;
        }

        if (mode.equals(DeptPositionActivity.EXTRA_JOB_TITLE_MODE)) {
            viewHolder.getTvNewAdd().setText(R.string.jandi_insert_new_job_title);
        } else if (mode.equals(DeptPositionActivity.EXTRA_DEPARTMENT_MODE)) {
            viewHolder.getTvNewAdd().setText(R.string.jandi_insert_new_department);
        }

        if (!datas.get(position).second) {
            SpannableStringBuilder messageStringBuilder = new SpannableStringBuilder(data);
            if (keyword != null && keyword.length() > 0) {
                messageStringBuilder.setSpan(
                        new ForegroundColorSpan(0xFF00A2E2),
                        data.toLowerCase().indexOf(keyword.toLowerCase()),
                        data.toLowerCase().indexOf(keyword.toLowerCase()) + keyword.length(),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            viewHolder.getTvName().setText(messageStringBuilder);
            viewHolder.getTvName().setTextColor(
                    JandiApplication.getContext().getResources().getColor(R.color.dark_gray));
            viewHolder.getTvNewAdd().setVisibility(View.GONE);
            viewHolder.contentView.setOnClickListener(
                    v -> onItemClickListener.onItemClick(datas.get(position).first));
        } else {
            viewHolder.getTvName().setText(datas.get(position).first);
            viewHolder.getTvName().setTextColor(0xFF00A2E2);
            viewHolder.getTvNewAdd().setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void dataRefresh() {
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(String item);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private View contentView;
        private TextView tvName;
        private TextView tvNewAdd;

        public ViewHolder(View itemView) {
            super(itemView);
            contentView = itemView;
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvNewAdd = (TextView) itemView.findViewById(R.id.tv_new_added);
        }

        public TextView getTvName() {
            return tvName;
        }

        public TextView getTvNewAdd() {
            return tvNewAdd;
        }
    }

}

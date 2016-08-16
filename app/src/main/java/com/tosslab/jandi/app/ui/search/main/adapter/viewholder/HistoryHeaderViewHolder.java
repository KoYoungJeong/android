package com.tosslab.jandi.app.ui.search.main.adapter.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.search.main.object.SearchData;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tee on 16. 7. 29..
 */
public class HistoryHeaderViewHolder extends BaseViewHolder<SearchData> {

    @Bind(R.id.tv_all_delete_button)
    TextView tvAllDeleteButton;

    private OnDeleteAllHistory onDeleteAllHistory;

    public HistoryHeaderViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public static HistoryHeaderViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_search_history_header, parent, false);
        return new HistoryHeaderViewHolder(itemView);
    }

    @Override
    public void onBindView(SearchData searchData) {
        if (onDeleteAllHistory != null) {
            tvAllDeleteButton.setOnClickListener(v -> onDeleteAllHistory.onDeleteAllHistory());
        }
    }

    public void setOnDeleteAllHistory(OnDeleteAllHistory onDeleteAllHistory) {
        this.onDeleteAllHistory = onDeleteAllHistory;
    }

    public interface OnDeleteAllHistory {
        void onDeleteAllHistory();
    }


}

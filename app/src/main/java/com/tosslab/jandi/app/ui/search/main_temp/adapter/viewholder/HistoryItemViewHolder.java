package com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchData;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchHistoryData;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tee on 16. 7. 29..
 */
public class HistoryItemViewHolder extends BaseViewHolder<SearchData> {

    @Bind(R.id.tv_search_keyword)
    TextView tvSearchKeyword;

    @Bind(R.id.iv_delete_button)
    ImageView ivDeleteButton;

    private OnSelectHistoryListener onSelectHistoryListener;
    private OnDeleteHistoryListener onDeleteOldQueryListener;

    public HistoryItemViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public static HistoryItemViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_search_history_item, parent, false);
        return new HistoryItemViewHolder(itemView);
    }

    @Override
    public void onBindView(SearchData searchData) {
        SearchHistoryData searchHistoryData = (SearchHistoryData) searchData;
        tvSearchKeyword.setText(searchHistoryData.getKeyword());

        if (onSelectHistoryListener != null) {
            itemView.setOnClickListener(v ->
                    onSelectHistoryListener.onSelectHistory(searchHistoryData.getKeyword()));
        }

        if (onDeleteOldQueryListener != null) {
            ivDeleteButton.setOnClickListener(v ->
                    onDeleteOldQueryListener.onDeleteHistory(searchHistoryData.getKeyword()));
        }
    }

    public void setOnDeleteHistoryListener(OnDeleteHistoryListener onDeleteOldQueryListener) {
        this.onDeleteOldQueryListener = onDeleteOldQueryListener;
    }

    public void setOnSelectHistoryListener(OnSelectHistoryListener onSelectHistoryListener) {
        this.onSelectHistoryListener = onSelectHistoryListener;
    }

    public interface OnDeleteHistoryListener {
        void onDeleteHistory(String keyword);
    }

    public interface OnSelectHistoryListener {
        void onSelectHistory(String keyword);
    }


}

package com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchData;
import com.tosslab.jandi.app.ui.search.main_temp.object.SearchMessageHeaderData;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tee on 16. 7. 21..
 */
public class MessageHeaderViewHolder extends BaseViewHolder<SearchData> {

    @Bind(R.id.progress_init_loading)
    ProgressBar progressInitLoading;

    @Bind(R.id.tv_searched_result_message)
    TextView tvSearchMessage;

    @Bind(R.id.vg_searched_result_message)
    ViewGroup vgSearchedResultMessage;

    public MessageHeaderViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public static MessageHeaderViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_search_message_header, parent, false);
        return new MessageHeaderViewHolder(itemView);
    }

    @Override
    public void onBindView(SearchData data) {
        SearchMessageHeaderData searchMessageHeaderData = (SearchMessageHeaderData) data;

        if (searchMessageHeaderData.isShowSearchedResultMessage()) {
            vgSearchedResultMessage.setVisibility(View.VISIBLE);
        } else {
            vgSearchedResultMessage.setVisibility(View.GONE);
        }

        if (searchMessageHeaderData.isShowProgress()) {
            progressInitLoading.setVisibility(View.VISIBLE);
            tvSearchMessage.setText("검색 중 입니다.");
        } else {
            progressInitLoading.setVisibility(View.GONE);
            tvSearchMessage.setText(
                    searchMessageHeaderData.getSearchedMessageCount() + "개의 메세지 검색 결과가 있습니다.");
        }
    }

}
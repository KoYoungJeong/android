package com.tosslab.jandi.app.ui.search.main_temp.adapter.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tee on 16. 7. 21..
 */
public class SearchStickyHeaderViewHolder extends BaseViewHolder {

    public static final int TYPE_ROOM = 0x01;
    public static final int TYPE_MESSAGE = 0x02;
    @Bind(R.id.tv_sticky_title)
    TextView tvStickyTitle;
    @Bind(R.id.tv_sticky_count)
    TextView tvStickyCount;
    @Bind(R.id.iv_folder_icon)
    ImageView ivFolderIcon;

    private int type = TYPE_ROOM;

    public SearchStickyHeaderViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public static SearchStickyHeaderViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_search_sticky_header, parent, false);
        return new SearchStickyHeaderViewHolder(itemView);
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public void onBindView(Object o) {
        if (type == TYPE_ROOM) {
            tvStickyTitle.setText("대화방");
        } else {
            tvStickyTitle.setText("메세지");
            tvStickyCount.setVisibility(View.GONE);
        }
    }

}

package com.tosslab.jandi.app.ui.search.main.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tee on 16. 7. 21..
 */
public class SearchStickyHeaderViewHolder extends RecyclerView.ViewHolder {

    public static final int TYPE_ROOM = 0x01;
    public static final int TYPE_MESSAGE = 0x02;

    @Bind(R.id.tv_sticky_title)
    TextView tvStickyTitle;
    @Bind(R.id.tv_sticky_count)
    TextView tvStickyCount;
    @Bind(R.id.iv_folder_icon)
    ImageView ivFolderIcon;

    View itemView;

    private int type;

    private int count = 0;

    private boolean isFold = false;

    public SearchStickyHeaderViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.itemView = itemView;
    }

    public static SearchStickyHeaderViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_search_sticky_header, parent, false);
        return new SearchStickyHeaderViewHolder(itemView);
    }

    public void setFoldIcon(boolean isFold) {
        this.isFold = isFold;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void onBindView(Object o) {
        if (type == TYPE_ROOM) {
            tvStickyTitle.setText(
                    JandiApplication.getContext().getString(R.string.jandi_title_topic_room)
            );
            if (count > 0) {
                tvStickyCount.setVisibility(View.VISIBLE);
                tvStickyCount.setText(String.valueOf(count));
            } else {
                tvStickyCount.setVisibility(View.GONE);
            }
            ivFolderIcon.setVisibility(View.VISIBLE);
            if (isFold) {
                ivFolderIcon.setImageDrawable(JandiApplication.getContext()
                        .getResources().getDrawable(R.drawable.title_collepse_down));
            } else {
                ivFolderIcon.setImageDrawable(JandiApplication.getContext()
                        .getResources().getDrawable(R.drawable.title_collepse));
            }
        } else if (type == TYPE_MESSAGE) {
            tvStickyTitle.setText(
                    JandiApplication.getContext().getString(R.string.jandi_title_message)
            );
            tvStickyCount.setVisibility(View.GONE);
            ivFolderIcon.setVisibility(View.GONE);
        }
    }

}

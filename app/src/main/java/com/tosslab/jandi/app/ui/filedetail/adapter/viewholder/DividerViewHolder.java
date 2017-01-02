package com.tosslab.jandi.app.ui.filedetail.adapter.viewholder;

import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.utils.UiUtils;

/**
 * Created by tonyjs on 16. 1. 28..
 */
public class DividerViewHolder extends BaseViewHolder<ResMessages.OriginalMessage> {

    public static DividerViewHolder newInstance(ViewGroup parent) {
        View itemView = new View(parent.getContext());

        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = (int) UiUtils.getPixelFromDp(1);

        ViewGroup.MarginLayoutParams layoutParams = new ViewGroup.MarginLayoutParams(width, height);
        itemView.setLayoutParams(layoutParams);

        int color = parent.getResources().getColor(R.color.jandi_file_search_item_divider);
        itemView.setBackgroundColor(color);
        return new DividerViewHolder(itemView);
    }

    private DividerViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void onBindView(ResMessages.OriginalMessage originalMessage) {
    }
}

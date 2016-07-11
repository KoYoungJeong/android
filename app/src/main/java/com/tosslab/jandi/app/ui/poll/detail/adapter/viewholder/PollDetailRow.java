package com.tosslab.jandi.app.ui.poll.detail.adapter.viewholder;

import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;

/**
 * Created by tonyjs on 16. 6. 23..
 */
public class PollDetailRow<ITEM> extends MultiItemRecyclerAdapter.Row<ITEM> {

    private PollDetailRow(ITEM item, int itemViewType) {
        super(item, itemViewType);
    }

    public static <ITEM> PollDetailRow<ITEM> create(ITEM item, int itemViewType) {
        return new PollDetailRow<>(item, itemViewType);
    }

}

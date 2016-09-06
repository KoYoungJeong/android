package com.tosslab.jandi.app.ui.maintab.navigation.adapter.viewholder;

import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;

/**
 * Created by tonyjs on 16. 6. 23..
 */
public class TeamRow<ITEM> extends MultiItemRecyclerAdapter.Row<ITEM> {

    private TeamRow(ITEM item, int itemViewType) {
        super(item, itemViewType);
    }

    public static <ITEM> TeamRow<ITEM> create(ITEM item, int itemViewType) {
        return new TeamRow<>(item, itemViewType);
    }

}

package com.tosslab.jandi.app.views.listeners;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by tee on 15. 8. 31..
 */
public interface OnExpandableGroupItemLongClickListener {
    boolean onItemClick(View view, RecyclerView.Adapter adapter, int groupPosition);
}

package com.tosslab.jandi.app.views.listeners;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by tee on 15. 9. 1..
 */
public interface OnRecyclerItemWithTypeCLickListener {
    void onItemClick(View view, RecyclerView.Adapter adapter, int position, int type);
}

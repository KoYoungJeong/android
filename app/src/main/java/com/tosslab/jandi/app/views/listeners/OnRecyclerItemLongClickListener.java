package com.tosslab.jandi.app.views.listeners;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Steve SeongUg Jung on 15. 3. 17..
 */
public interface OnRecyclerItemLongClickListener {
    boolean onItemClick(View view, RecyclerView.Adapter adapter, int position);
}

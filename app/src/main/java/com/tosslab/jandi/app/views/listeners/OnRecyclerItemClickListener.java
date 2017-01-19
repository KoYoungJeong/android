package com.tosslab.jandi.app.views.listeners;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public interface OnRecyclerItemClickListener {
    void onItemClick(View view, RecyclerView.Adapter adapter, int position);
}

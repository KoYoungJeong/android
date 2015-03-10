package com.tosslab.jandi.app.ui.search.messages.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * Created by Steve SeongUg Jung on 15. 3. 10..
 */
public class MessageSearchResultAdapter extends RecyclerView.Adapter {
    private final Context context;

    public MessageSearchResultAdapter(Context context) {
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}

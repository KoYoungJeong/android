package com.tosslab.jandi.app.ui.maintab.topic.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eowise.recyclerview.stickyheaders.StickyHeadersAdapter;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.maintab.topic.domain.Topic;

/**
 * Created by Steve SeongUg Jung on 15. 7. 7..
 */
public class TopicRecyclerStickyHeaderAdapter implements StickyHeadersAdapter {

    private final Context context;
    private TopicRecyclerAdapter topicRecyclerAdapter;

    public TopicRecyclerStickyHeaderAdapter(Context context, TopicRecyclerAdapter topicRecyclerAdapter) {
        this.context = context;
        this.topicRecyclerAdapter = topicRecyclerAdapter;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_entity_title, parent, false);

        TopicHeaderViewHolder viewHolder = new TopicHeaderViewHolder(view);
        viewHolder.textViewTitle = (TextView) view.findViewById(R.id.txt_entity_list_title);

        return viewHolder;
    }

    @Override
    public long getHeaderId(int position) {
        return topicRecyclerAdapter.getItem(position).isJoined() ? 1 : 2;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Topic item = topicRecyclerAdapter.getItem(position);

        TopicHeaderViewHolder viewHolder = (TopicHeaderViewHolder) holder;

        if (item.isJoined()) {
            viewHolder.textViewTitle.setText(R.string.jandi_entity_joined_topic);
        } else {
            viewHolder.textViewTitle.setText(R.string.jandi_entity_unjoined_topic);
        }
    }


    static class TopicHeaderViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewTitle;

        public TopicHeaderViewHolder(View itemView) {
            super(itemView);
        }
    }
}

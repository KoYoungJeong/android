package com.tosslab.jandi.app.ui.search.messages.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessageSearch;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 3. 10..
 */
public class MessageSearchResultAdapter extends RecyclerView.Adapter {
    private final Context context;
    private List<ResMessageSearch.SearchRecord> records;

    public MessageSearchResultAdapter(Context context) {
        this.context = context;
        records = new ArrayList<ResMessageSearch.SearchRecord>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_message_search, parent, false);

        return new MessageSearchViewHolder(view);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        ResMessageSearch.SearchRecord item = getItem(position);

        MessageSearchViewHolder viewHolder = (MessageSearchViewHolder) holder;
        viewHolder.topicNameTextView.setText(item.getSearchEntityInfo().getName());
        viewHolder.dateTextView.setText(item.getCurrentRecord().getDate().toString());

        viewHolder.prevTextView.setText(item.getPrevRecord().getText());
        viewHolder.currentTextView.setText(item.getCurrentRecord().getText());
        viewHolder.nextTextView.setText(item.getNextRecord().getText());
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public ResMessageSearch.SearchRecord getItem(int position) {
        return records.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void add(ResMessageSearch.SearchRecord searchRecord) {
        records.add(searchRecord);
    }

    public void clear() {
        records.clear();
    }

    public void addAll(List<ResMessageSearch.SearchRecord> searchRecords) {
        records.addAll(searchRecords);
    }

    private static class MessageSearchViewHolder extends RecyclerView.ViewHolder {

        TextView topicNameTextView;
        TextView dateTextView;
        TextView prevTextView;
        TextView currentTextView;
        TextView nextTextView;


        public MessageSearchViewHolder(View itemView) {
            super(itemView);

            topicNameTextView = (TextView) itemView.findViewById(R.id.txt_message_search_topic_name);
            dateTextView = (TextView) itemView.findViewById(R.id.txt_message_search_topic_date);
            prevTextView = (TextView) itemView.findViewById(R.id.txt_message_search_first);
            currentTextView = (TextView) itemView.findViewById(R.id.txt_message_search_second);
            nextTextView = (TextView) itemView.findViewById(R.id.txt_message_search_third);
        }
    }

}

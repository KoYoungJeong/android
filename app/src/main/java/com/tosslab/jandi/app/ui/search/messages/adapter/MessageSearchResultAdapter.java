package com.tosslab.jandi.app.ui.search.messages.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessageSearch;
import com.tosslab.jandi.app.ui.search.messages.adapter.strategy.TextStrategy;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 3. 10..
 */
public class MessageSearchResultAdapter extends RecyclerView.Adapter {
    private final Context context;
    private List<ResMessageSearch.SearchRecord> records;
    private HeaderItem headerItem;
    private OnRecyclerItemClickListener onRecyclerItemClickListener;

    public MessageSearchResultAdapter(Context context) {
        this.context = context;
        records = new ArrayList<ResMessageSearch.SearchRecord>();
    }

    public void setOnRecyclerItemClickListener(OnRecyclerItemClickListener onRecyclerItemClickListener) {
        this.onRecyclerItemClickListener = onRecyclerItemClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == 1) {

            View view = LayoutInflater.from(context).inflate(R.layout.item_message_search, parent, false);

            return new MessageSearchViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_search_header, parent, false);
            return new SearchHeaderViewHolder(view);
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (position > 0) {

            ResMessageSearch.SearchRecord item = getItem(position);
            MessageSearchViewHolder viewHolder = (MessageSearchViewHolder) holder;

            ResMessageSearch.SearchEntityInfo searchEntityInfo = item.getSearchEntityInfo();
            ResMessageSearch.Record currentRecord = item.getCurrentRecord();
            ResMessageSearch.Record prevRecord = item.getPrevRecord();
            ResMessageSearch.Record nextRecord = item.getNextRecord();

            if (searchEntityInfo != null) {
                viewHolder.topicNameTextView.setText(searchEntityInfo.getName());
            }

            if (prevRecord != null) {
                viewHolder.prevTextView.setVisibility(View.VISIBLE);
                viewHolder.prevTextView.setText(TextStrategy.getSubSearchString(context, prevRecord, viewHolder.prevTextView));
            } else {
                viewHolder.prevTextView.setVisibility(View.GONE);
            }

            if (currentRecord != null) {
                viewHolder.dateTextView.setText(DateTransformator.getTimeString(currentRecord.getLastDate()));
                viewHolder.currentTextView.setText(TextStrategy.getCurrentSearchString(context, currentRecord, ((HeaderItem) getItem(0)).getQuery()));
            }

            if (nextRecord != null) {
                viewHolder.nextTextView.setVisibility(View.VISIBLE);
                viewHolder.nextTextView.setText(TextStrategy.getSubSearchString(context, nextRecord, viewHolder.nextTextView));
            } else {
                viewHolder.nextTextView.setVisibility(View.GONE);
            }
        } else {
            SearchHeaderViewHolder searchHeaderViewHolder = (SearchHeaderViewHolder) holder;
            HeaderItem item = (HeaderItem) getItem(position);
            if (item.isLoading) {
                searchHeaderViewHolder.textView.setText(Html.fromHtml(context.getString(R.string.jandi_search_ing, item.getQuery())));
                searchHeaderViewHolder.progressBar.setVisibility(View.VISIBLE);
            } else {
                searchHeaderViewHolder.textView.setText(Html.fromHtml(context.getString(R.string.jandi_search_result_summary, item.getQuery(), item.getCount())));
                searchHeaderViewHolder.progressBar.setVisibility(View.GONE);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (onRecyclerItemClickListener != null) {
                onRecyclerItemClickListener.onItemClick(v, MessageSearchResultAdapter.this, position);
            }
        });
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

    public void setQueryKeyword(String query, int totalCount, boolean isFinish) {
        ResMessageSearch.SearchRecord item1 = getItemCount() > 0 ? getItem(0) : null;

        if (!(item1 instanceof HeaderItem)) {
            item1 = new HeaderItem();
            add(0, item1);
        }

        HeaderItem item = (HeaderItem) item1;
        item.setQuery(query);
        item.setCount(totalCount);
        item.setLoading(!isFinish);

    }

    private void add(int index, ResMessageSearch.SearchRecord headerItem) {
        records.add(index, headerItem);
    }

    public void addHeader(String query) {
        HeaderItem headerItem;
        if (getItemCount() > 0 && getItem(0) instanceof HeaderItem) {
            headerItem = ((HeaderItem) getItem(0));
            headerItem.setQuery(query);
        } else {
            headerItem = new HeaderItem();
            headerItem.setQuery(query);
        }

        add(0, headerItem);
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

    private static class HeaderItem extends ResMessageSearch.SearchRecord {
        private String query;
        private int count;
        private boolean isLoading = true;

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public boolean isLoading() {
            return isLoading;
        }

        public void setLoading(boolean isLoading) {
            this.isLoading = isLoading;
        }
    }

    private static class SearchHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ProgressBar progressBar;


        public SearchHeaderViewHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(R.id.txt_message_search_result);
            progressBar = (ProgressBar) view.findViewById(R.id.progress_message_search_result);
        }
    }
}

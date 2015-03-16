package com.tosslab.jandi.app.lists.files;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.events.files.RefreshOldFileEvent;
import com.tosslab.jandi.app.network.models.ResMessages;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 7. 5..
 */
@EBean
public class SearchedFileItemListAdapter extends RecyclerView.Adapter {
    List<ResMessages.FileMessage> searedFiles;

    @RootContext
    Context mContext;

    MoreState moreState = MoreState.Idle;
    OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @AfterInject
    void initAdapter() {
        searedFiles = new ArrayList<ResMessages.FileMessage>();
    }

    public void clearAdapter() {
        searedFiles.clear();
        notifyDataSetChanged();
    }

    public void clearAdapterWithoutNotify() {
        searedFiles.clear();
    }

    public void insert(List<ResMessages.OriginalMessage> files) {
        for (ResMessages.OriginalMessage message : files) {
            searedFiles.add((ResMessages.FileMessage) message);
        }
    }

    public void setNoMoreLoad() {
        moreState = MoreState.NoMore;
    }

    public void setReadyMore() {
        moreState = MoreState.Idle;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(SearchedFileItemView_.build(mContext));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SearchedFileItemView searchedFileItemView = (SearchedFileItemView) holder.itemView;

        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(v, SearchedFileItemListAdapter.this, position);
            }
        });

        searchedFileItemView.bind(getItem(position));

        if (position == getItemCount() - 1 && moreState == MoreState.Idle) {
            moreState = MoreState.Loading;

            EventBus.getDefault().post(new RefreshOldFileEvent());
        }
    }

    public ResMessages.FileMessage getItem(int position) {
        return searedFiles.get(position);
    }

    @Override
    public int getItemCount() {
        return searedFiles.size();
    }

    private enum MoreState {
        Idle, Loading, NoMore
    }

    public interface OnItemClickListener {
        void onItemClick(View view, RecyclerView.Adapter adapter, int position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}

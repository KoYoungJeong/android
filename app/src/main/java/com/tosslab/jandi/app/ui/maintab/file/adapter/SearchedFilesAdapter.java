package com.tosslab.jandi.app.ui.maintab.file.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.RefreshOldFileEvent;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.maintab.file.adapter.viewholder.SearchedFilesViewHolder;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class SearchedFilesAdapter extends RecyclerView.Adapter
        implements SearchedFilesAdapterModel, SearchedFilesAdapterView {

    List<ResMessages.FileMessage> searedFiles;

    MoreState moreState = MoreState.Idle;

    OnRecyclerItemClickListener onRecyclerItemClickListener;

    public void setOnRecyclerItemClickListener(OnRecyclerItemClickListener onRecyclerItemClickListener) {
        this.onRecyclerItemClickListener = onRecyclerItemClickListener;
    }

    @Override
    public void setList(ArrayList<ResMessages.FileMessage> list) {
        this.searedFiles = list;
    }

    @Override
    public void clearListView() {
        searedFiles.clear();
        notifyDataSetChanged();
    }

    @Override
    public void clearList() {
        searedFiles.clear();
    }

    @Override
    public void refreshListView() {
        notifyDataSetChanged();
    }

    @Override
    public void add(List<ResMessages.OriginalMessage> files) {
        for (ResMessages.OriginalMessage message : files) {
            searedFiles.add((ResMessages.FileMessage) message);
        }
    }

    @Override
    public void setNoMoreLoad() {
        moreState = MoreState.NoMore;
    }

    @Override
    public void setReadyMore() {
        moreState = MoreState.Idle;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.
                from(JandiApplication.getContext()).inflate(R.layout.item_searched_file, parent, false);
        return new SearchedFilesViewHolder(convertView, parent.getContext());
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SearchedFilesViewHolder SearchedFileViewHolder = (SearchedFilesViewHolder) holder;

        holder.itemView.setOnClickListener(v -> {
            if (onRecyclerItemClickListener != null) {
                onRecyclerItemClickListener.onItemClick(v, SearchedFilesAdapter.this, position);
            }
        });

        SearchedFileViewHolder.bind(getItem(position));

        if (position == getItemCount() - 1 && moreState == MoreState.Idle) {
            moreState = MoreState.Loading;

            EventBus.getDefault().post(new RefreshOldFileEvent());
        }
    }

    @Override
    public ResMessages.FileMessage getItem(int position) {
        return searedFiles.get(position);
    }

    @Override
    public int getItemCount() {
        return searedFiles.size();
    }

    @Override
    public int findPositionByFileId(long fileId) {
        int itemCount = getItemCount();
        for (int idx = 0; idx < itemCount; ++idx) {
            if (getItem(idx).id == fileId) {
                return idx;
            }
        }
        return -1;
    }

    @Override
    public void remove(int position) {
        searedFiles.remove(position);
    }

    private enum MoreState {
        Idle, Loading, NoMore
    }

}

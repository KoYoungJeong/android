package com.tosslab.jandi.app.ui.maintab.tabs.topic.views.folderlist.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.tosslab.jandi.app.network.models.start.Folder;

import java.util.List;

/**
 * Created by tee on 16. 1. 26..
 */
public class TopicFolderChooseAdapter extends TopicFolderMainAdapter {

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        if (holder.getItemViewType() == TYPE_FOLDER_LIST) {
            FolderAdapterViewHolder viewHolder = (FolderAdapterViewHolder) holder;
            viewHolder.btRemoveFolder.setVisibility(View.GONE);
            viewHolder.btRenameFolder.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(view -> {
            if (onRecyclerItemClickListener != null) {
                onRecyclerItemClickListener.onItemClick(holder.itemView, TopicFolderChooseAdapter.this,
                        position, holder.getItemViewType());
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 2) {
            if (folderId != -1) {
                return TYPE_REMOVE_FROM_FOLDER;
            }
        } else if (position == getItemCount() - 1) {
            return TYPE_MAKE_NEW_FOLDER;
        }
        return TYPE_FOLDER_LIST;
    }

    @Override
    protected void addDummyFolders() {
        if (folderId != -1) {
            folders.add(new Folder());
        }
        folders.add(new Folder());
    }
}

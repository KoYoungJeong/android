package com.tosslab.jandi.app.ui.maintab.topic.views.folderlist.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

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

}

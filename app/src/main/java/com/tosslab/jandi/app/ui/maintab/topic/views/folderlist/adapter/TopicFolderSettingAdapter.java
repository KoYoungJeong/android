package com.tosslab.jandi.app.ui.maintab.topic.views.folderlist.adapter;

import android.support.v7.widget.RecyclerView;

import com.tosslab.jandi.app.network.models.start.Folder;

import java.util.Collections;

/**
 * Created by tee on 15. 8. 30..
 */
public class TopicFolderSettingAdapter extends TopicFolderMainAdapter {

    private OnRemoveFolderListener onRemoveFolderListener;
    private OnRenameFolderListener onRenameFolderListener;
    private OnFolderSeqChangeLisener onFolderSeqChangeLisener;

    @Override
    protected void addDummyFolders() {
        folders.add(new Folder());
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return TYPE_MAKE_NEW_FOLDER;
        }
        return TYPE_FOLDER_LIST;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder.getItemViewType() == TYPE_FOLDER_LIST) {
            FolderAdapterViewHolder viewHolder = (FolderAdapterViewHolder) holder;
            Folder resFolder = getItem(position);
            viewHolder.btRemoveFolder.setOnClickListener(v -> {
                onRemoveFolderListener.onRemove(resFolder.getId());
            });
            viewHolder.btRenameFolder.setOnClickListener(v -> {
                onRenameFolderListener.onRename(resFolder.getId(), resFolder.getName(), resFolder.getSeq());
            });
        } else if (holder.getItemViewType() == TYPE_MAKE_NEW_FOLDER) {
            holder.itemView.setOnClickListener(view -> {
                if (onRecyclerItemClickListener != null) {
                    onRecyclerItemClickListener.onItemClick(holder.itemView, TopicFolderSettingAdapter.this,
                            position, holder.getItemViewType());
                }
            });
        }
    }

    public void setOnRemoveFolderListener(OnRemoveFolderListener onRemoveFolderListener) {
        this.onRemoveFolderListener = onRemoveFolderListener;
    }

    public void setOnRenameFolderListener(OnRenameFolderListener onRenameFolderListener) {
        this.onRenameFolderListener = onRenameFolderListener;
    }

    public void setOnFolderSeqChangeLisener(OnFolderSeqChangeLisener onFolderSeqChangeLisener) {
        this.onFolderSeqChangeLisener = onFolderSeqChangeLisener;
    }

    public void swap(int firstPosition, int secondPosition) {
        if (getItemViewType(secondPosition) == TYPE_FOLDER_LIST) {
            Collections.swap(folders, firstPosition, secondPosition);
            notifyItemMoved(firstPosition, secondPosition);
        }
    }

    public void sendChangeSeq(long folderId, int seq) {
        onFolderSeqChangeLisener.onSeqChanged(folderId, seq);
    }

    public interface OnRemoveFolderListener {
        void onRemove(long folderId);
    }

    public interface OnRenameFolderListener {
        void onRename(long folderId, String name, int seq);
    }

    public interface OnFolderSeqChangeLisener {
        void onSeqChanged(long folderId, int seq);
    }

}

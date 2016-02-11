package com.tosslab.jandi.app.ui.maintab.topic.views.folderlist.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResFolder;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemWithTypeCLickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tee on 16. 1. 26..
 */
public class TopicFolderMainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_REMOVE_FROM_FOLDER = 1;
    public static final int TYPE_MAKE_NEW_FOLDER = 2;
    public static final int TYPE_FOLDER_LIST = 0;
    protected OnRecyclerItemWithTypeCLickListener onRecyclerItemClickListener;
    List<ResFolder> folders = null;
    private long folderId = -1;

    public List<ResFolder> getFolders() {
        if (folders != null) {
            return folders;
        }
        return new ArrayList<>();
    }

    public void clear() {
        getFolders().clear();
    }

    public void addAll(List<ResFolder> folders) {
        this.folders = folders;

        //for dummy items (remove from/ make folder)
        if (folderId != -1) {
            folders.add(new ResFolder());
        }
        folders.add(new ResFolder());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_REMOVE_FROM_FOLDER) {
            return new ExtraItemViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_folder_choose_remove, parent, false));
        } else if (viewType == TYPE_MAKE_NEW_FOLDER) {
            return new ExtraItemViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_folder_choose_new_folder, parent, false));
        }
        return new FolderAdapterViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_choose_folder, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_FOLDER_LIST) {
            holder.itemView.setBackgroundResource(R.color.white);
            FolderAdapterViewHolder viewHolder = (FolderAdapterViewHolder) holder;
            viewHolder.tvChooseFolder.setText(getFolders().get(position).name);
        }
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
    public int getItemCount() {
        return getFolders().size();
    }

    public ResFolder getItemById(int position) {
        return folders.get(position);
    }

    public void setFolderId(long folderId) {
        this.folderId = folderId;
    }

    public void setOnRecyclerItemClickListener(OnRecyclerItemWithTypeCLickListener onRecyclerItemClickListener) {
        this.onRecyclerItemClickListener = onRecyclerItemClickListener;
    }

    static class FolderAdapterViewHolder extends RecyclerView.ViewHolder {
        public TextView tvChooseFolder;
        public Button btRemoveFolder;
        public Button btRenameFolder;

        public FolderAdapterViewHolder(View itemView) {
            super(itemView);
            tvChooseFolder = (TextView) itemView.findViewById(R.id.tv_choose_folder);
            btRemoveFolder = (Button) itemView.findViewById(R.id.bt_delete_folder);
            btRenameFolder = (Button) itemView.findViewById(R.id.bt_rename_folder);
        }
    }

    static class ExtraItemViewHolder extends RecyclerView.ViewHolder {
        public ExtraItemViewHolder(View itemView) {
            super(itemView);
        }
    }

}

package com.tosslab.jandi.app.ui.maintab.topics.views.choosefolderlist.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResFolder;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemWithTypeCLickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tee on 15. 8. 30..
 */
public class TopicFolderChooseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_REMOVE_FROM_FOLDER = 1;
    public static final int TYPE_MAKE_NEW_FOLDER = 2;
    public static final int TYPE_FOLDER_LIST = 0;

    List<ResFolder> folders = null;

    private OnRecyclerItemWithTypeCLickListener onRecyclerItemClickListener;

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

        //for dummy items (remove / make folder)
        folders.add(new ResFolder());
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

        return new FolderChooseAdapterViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_choose_folder, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_FOLDER_LIST) {
            FolderChooseAdapterViewHolder viewHolder = (FolderChooseAdapterViewHolder) holder;
            viewHolder.tvChooseFolder.setText(getFolders().get(position).name);
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
            return TYPE_REMOVE_FROM_FOLDER;
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

    public void setOnRecyclerItemClickListener(OnRecyclerItemWithTypeCLickListener onRecyclerItemClickListener) {
        this.onRecyclerItemClickListener = onRecyclerItemClickListener;
    }

    static class FolderChooseAdapterViewHolder extends RecyclerView.ViewHolder {
        public TextView tvChooseFolder;

        public FolderChooseAdapterViewHolder(View itemView) {
            super(itemView);
            tvChooseFolder = (TextView) itemView.findViewById(R.id.tv_choose_folder);
        }
    }

    static class ExtraItemViewHolder extends RecyclerView.ViewHolder {
        public ExtraItemViewHolder(View itemView) {
            super(itemView);
        }
    }

}

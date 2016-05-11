package com.tosslab.jandi.app.ui.profile.defaultimage.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tee on 16. 1. 7..
 */
public class ProfileSelectorAdapter<VH extends RecyclerView.ViewHolder, T> extends RecyclerView.Adapter<VH> {

    protected List<T> items = new ArrayList<>();
    protected int selectedPosition = 0;
    protected OnRecyclerItemClickListener onRecyclerItemClickListener;

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile_selector, parent, false);
        return (VH) new ItemViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemViewHolder viewHolder = (ItemViewHolder) holder;

        viewHolder.ivImageBox.setSelected(selectedPosition == position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public T getSelectedItem() {
        return items.get(selectedPosition);
    }

    public void setOnRecyclerItemClickListener(OnRecyclerItemClickListener onRecyclerItemClickListener) {
        this.onRecyclerItemClickListener = onRecyclerItemClickListener;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImageBox;

        public ItemViewHolder(View itemView) {
            super(itemView);
            ivImageBox = (ImageView) itemView.findViewById(R.id.iv_image_box);
        }
    }

}
